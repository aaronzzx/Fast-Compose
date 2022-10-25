package com.aaron.compose.paging

import android.util.Log
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/9/20
 */
@Stable
class PageData<K, V>(
    private val coroutineScope: CoroutineScope,
    private val config: PageConfig = PageConfig(),
    lazyLoad: Boolean = false,
    private val onRequest: suspend (params: LoadParams<K>) -> LoadResult<K, V>
) {

    companion object {
        private const val Debug = true
        private const val TAG = "PageData"

        private fun log(msg: String) {
            if (Debug) {
                Log.d(TAG, msg)
            }
        }
    }

    var page: Int by mutableStateOf(1)
        private set

    val data: SnapshotStateList<V> = mutableStateListOf()

    val itemCount: Int get() = data.size

    val loadState: CombinedLoadState by mutableStateOf(CombinedLoadState())

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

    internal operator fun get(index: Int): V {
        // 判断是否触发加载
        val prefetchDistance = config.prefetchDistance
        if (prefetchDistance > 0 && (itemCount - 1) - index == prefetchDistance) {
            loadMore()
        }
        return data[index]
    }

    internal fun peek(index: Int): V {
        return data[index]
    }

    private fun isLoadEnd(): Boolean {
        val config = config
        return nextKey == null || !config.enableLoadMore || page >= config.maxPage
    }

    fun refresh() {
        tryLaunch(LoadType.Refresh) {
            refreshImpl()
        }
    }

    private suspend fun refreshImpl() {
        val loadState = loadState
        loadState.refresh = LoadState.Loading
        log("refresh-start: ${loadState.refresh}")
        when (val result = requestData(LoadType.Refresh, null)) {
            is LoadResult.Page -> {
                val dataList = result.data
                val nextKey = result.nextKey
                this.page = 1
                this.nextKey = nextKey
                loadState.refresh = LoadState.Idle(false)
                loadState.loadMore = LoadState.Idle(isLoadEnd())
                with(data) {
                    clear()
                    addAll(dataList)
                }
            }
            is LoadResult.Error -> {
                val throwable = result.throwable
                loadState.refresh = LoadState.Error(throwable)
                if (loadState.loadMore is LoadState.Waiting) {
                    loadState.loadMore = LoadState.Idle(isLoadEnd())
                }
            }
        }
        log("refresh-start: ${loadState.refresh}")
    }

    fun loadMore() {
        val loadMore = loadState.loadMore
        if (loadMore is LoadState.Idle && loadMore.noMoreData) {
            // 没有更多数据了
            return
        } else if (loadMore is LoadState.Error) {
            // 加载更多时出错，等待手动重试
            return
        }
        tryLaunch(LoadType.LoadMore) {
            loadMoreImpl()
        }
    }

    private suspend fun loadMoreImpl() {
        val loadState = loadState
        loadState.loadMore = LoadState.Loading
        log("loadMore-start: ${loadState.loadMore}")
        when (val result = requestData(LoadType.LoadMore, nextKey)) {
            is LoadResult.Page -> {
                val dataList = result.data
                val nextKey = result.nextKey
                this.page++
                this.nextKey = nextKey
                loadState.loadMore = LoadState.Idle(isLoadEnd())
                data.addAll(dataList)
            }
            is LoadResult.Error -> {
                val throwable = result.throwable
                loadState.loadMore = LoadState.Error(throwable)
            }
        }
        log("loadMore-end: ${loadState.loadMore}")
    }

    fun retry() {
        if (loadState.loadMore is LoadState.Error) {
            tryLaunch(LoadType.LoadMore) {
                loadMoreImpl()
            }
        }
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
        // 刷新操作必须覆盖加载更多，因为这时候加载更多没意义
        if (loadType == LoadType.Refresh) {
            curLoadJob?.cancel()
            curLoadJob = null
        }
        if (this.loadType != LoadType.Idle) {
            // 加载更多必须等待刷新完成
            if (loadType == LoadType.LoadMore && this.loadType == LoadType.Refresh) {
                loadState.loadMore = LoadState.Waiting
            }
            return
        }
        this.loadType = loadType
        curLoadJob = coroutineScope.launch {
            block()
        }.also {
            it.invokeOnCompletion {
                this.loadType = LoadType.Idle
                curLoadJob = null
            }
        }
    }
}