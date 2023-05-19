package com.aaron.compose.paging

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.aaron.compose.safestate.SafeStateList
import com.aaron.compose.safestate.safeStateListOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * 用于 Compose 的分页逻辑处理容器。
 *
 * @author aaronzzxup@gmail.com
 * @since 2022/9/20
 */
@Stable
class PageData<K, V>(
    internal val coroutineScope: CoroutineScope,
    val config: PageConfig = PageConfig(),
    val lazyLoad: Boolean = false,
    private val invokeCompletion: (suspend PageData<K, V>.(LoadResult<K, V>) -> Unit)? = null,
    private val onRequest: suspend PageData<K, V>.(params: LoadParams<K>) -> LoadResult<K, V>
) {

    /**
     * 是否成功加载过数据，即使加载失败
     */
    var isInitialized: Boolean by mutableStateOf(false)
        private set

    var page: Int by mutableStateOf(1)
        private set

    val data: SafeStateList<V> = safeStateListOf()

    val itemCount: Int get() = data.size

    val loadState: CombinedLoadState = CombinedLoadState()

    /**
     * 用于标识当前正在进行的操作，每次操作完成后都将回到 Idle 状态
     */
    private var loadType: LoadType = LoadType.Idle

    /**
     * 下一页的 key
     */
    private var nextKey: K? = null

    /**
     * 当前正在执行的任务
     */
    private var curLoadJob: Job? = null

    init {
        if (!lazyLoad) {
            refresh()
        }
    }

    @PublishedApi
    internal operator fun get(index: Int): V {
        // 判断是否触发加载
        val prefetchDistance = config.prefetchDistance
        if (prefetchDistance > 0 && itemCount - index == prefetchDistance) {
            loadMore()
        }
        return data[index]
    }

    @PublishedApi
    internal fun peek(index: Int): V {
        return data[index]
    }

    private fun isLoadEnd(): Boolean {
        val config = config
        return nextKey == null || !config.enableLoadMore || page >= config.maxPage
    }

    fun refresh(
        onSuccess: ((List<V>) -> Unit)? = null,
        onFailure: ((Throwable) -> Unit)? = null
    ) {
        tryLaunch(LoadType.Refresh) {
            refreshImpl()
                .onSuccess {
                    onSuccess?.invoke(it)
                }
                .onFailure {
                    onFailure?.invoke(it)
                }
        }
    }

    suspend fun refreshSuspend(): Result<List<V>> = suspendCoroutine { cont ->
        refresh(
            onSuccess = {
                cont.resume(Result.success(it))
            },
            onFailure = {
                cont.resume(Result.failure(it))
            }
        )
    }

    private suspend fun refreshImpl(): Result<List<V>> {
        val loadState = loadState
        loadState.refresh = LoadState.Loading
        val result = requestData(LoadType.Refresh, null)
        val pageList: List<V>
        when (result) {
            is LoadResult.Page -> {
                val dataList = result.data
                pageList = dataList
                val nextKey = result.nextKey
                this.page = 1
                this.nextKey = nextKey
                loadState.refresh = LoadState.Idle(false, true)
                loadState.loadMore = LoadState.Idle(isLoadEnd(), false)
                with(data.editInternal()) {
                    clear()
                    addAll(dataList)
                }
            }
            is LoadResult.Error -> {
                pageList = emptyList()
                val throwable = result.throwable
                loadState.refresh = when {
                    throwable is CancellationException
                            && loadType == LoadType.Refresh
                            && curLoadJob?.isActive == true -> LoadState.Loading
                    else -> LoadState.Error(throwable)
                }
                if (loadState.loadMore is LoadState.Waiting) {
                    loadState.loadMore = LoadState.Idle(isLoadEnd(), false)
                }
            }
        }
        isInitialized = true
        invokeCompletion?.invoke(this, result)
        return if (result is LoadResult.Error) {
            Result.failure(result.throwable)
        } else {
            Result.success(pageList)
        }
    }

    fun loadMore(
        onSuccess: ((List<V>) -> Unit)? = null,
        onFailure: ((Throwable) -> Unit)? = null,
        retryIfCurrentError: Boolean = false
    ) {
        val loadMore = loadState.loadMore
        if (loadMore is LoadState.Idle && loadMore.noMoreData) {
            // 没有更多数据了
            return
        } else if (loadMore is LoadState.Error) {
            // 加载更多时出错，等待手动重试
            if (retryIfCurrentError) retry(onSuccess, onFailure)
            return
        }
        tryLaunch(LoadType.LoadMore) {
            loadMoreImpl()
                .onSuccess {
                    onSuccess?.invoke(it)
                }
                .onFailure {
                    onFailure?.invoke(it)
                }
        }
    }

    suspend fun loadMoreSuspend(
        retryIfCurrentError: Boolean = false
    ): Result<List<V>> = suspendCoroutine { cont ->
        loadMore(
            onSuccess = {
                cont.resume(Result.success(it))
            },
            onFailure = {
                cont.resume(Result.failure(it))
            },
            retryIfCurrentError = retryIfCurrentError
        )
    }

    private suspend fun loadMoreImpl(): Result<List<V>> {
        val loadState = loadState
        loadState.loadMore = LoadState.Loading
        val result = requestData(LoadType.LoadMore, nextKey)
        val pageList: List<V>
        when (result) {
            is LoadResult.Page -> {
                val dataList = result.data
                pageList = dataList
                val nextKey = result.nextKey
                this.page++
                this.nextKey = nextKey
                loadState.loadMore = LoadState.Idle(isLoadEnd(), true)
                data.editInternal().addAll(dataList)
            }
            is LoadResult.Error -> {
                pageList = emptyList()
                val throwable = result.throwable
                loadState.loadMore = LoadState.Error(throwable)
            }
        }
        invokeCompletion?.invoke(this, result)
        return if (result is LoadResult.Error) {
            Result.failure(result.throwable)
        } else {
            Result.success(pageList)
        }
    }

    fun retry(
        onSuccess: ((List<V>) -> Unit)? = null,
        onFailure: ((Throwable) -> Unit)? = null
    ) {
        if (loadState.loadMore is LoadState.Error) {
            tryLaunch(LoadType.LoadMore) {
                loadMoreImpl()
                    .onSuccess {
                        onSuccess?.invoke(it)
                    }
                    .onFailure {
                        onFailure?.invoke(it)
                    }
            }
        }
    }

    suspend fun retrySuspend(): Result<List<V>> = suspendCoroutine { cont ->
        retry(
            onSuccess = {
                cont.resume(Result.success(it))
            },
            onFailure = {
                cont.resume(Result.failure(it))
            }
        )
    }

    private suspend fun requestData(actualLoadType: LoadType, nextKey: K?): LoadResult<K, V> {
        val config = config
        return try {
            val params = when (actualLoadType) {
                LoadType.Refresh -> LoadParams.Refresh(nextKey, config)
                LoadType.LoadMore -> LoadParams.LoadMore(nextKey, config)
                else -> error("Illegal LoadType: $actualLoadType")
            }
            onRequest(params)
        } catch (exception: Exception) {
            if (config.printError) {
                exception.printStackTrace()
            }
            LoadResult.Error(exception)
        }
    }

    private fun tryLaunch(loadType: LoadType, block: suspend () -> Unit) {
        if (loadType == LoadType.Refresh) {
            if (curLoadJob?.isActive == true) {
                curLoadJob?.cancel()
            }
        } else if (loadType == LoadType.LoadMore) {
            if (this.loadType == LoadType.Refresh) {
                loadState.loadMore = LoadState.Waiting
                return
            } else if (this.loadType == LoadType.LoadMore) {
                return
            }
        }
        this.loadType = loadType
        coroutineScope.launch {
            block()
        }.also {
            curLoadJob = it
            it.invokeOnCompletion {
                if (curLoadJob?.isCompleted == true) {
                    this.loadType = LoadType.Idle
                }
            }
        }
    }
}