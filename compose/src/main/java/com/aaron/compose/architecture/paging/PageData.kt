package com.aaron.compose.architecture.paging

import android.util.Log
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    val count: Int get() = data.size

    val loadState: CombinedLoadState by mutableStateOf(CombinedLoadState())

    private var loadType: LoadType = LoadType.Idle

    private var nextKey: K? = null

    private var curLoadJob: Job? = null

    init {
        if (!lazyLoad) {
            refresh()
        }
    }

    internal operator fun get(index: Int): V {
        // 判断是否触发加载
        val prefetchDistance = config.prefetchDistance
        if (prefetchDistance > 0 && (count - 1) - index == prefetchDistance) {
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
        nextKey = null
        log("refresh-start: ${loadState.refresh}")
        when (val result = requestData(LoadType.Refresh)) {
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
        when (val result = requestData(LoadType.LoadMore)) {
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
        val loadState = loadState
        if (loadState.refresh is LoadState.Error) {
            tryLaunch(LoadType.Refresh) {
                refreshImpl()
            }
        } else if (loadState.loadMore is LoadState.Error) {
            tryLaunch(LoadType.LoadMore) {
                loadMoreImpl()
            }
        }
    }

    private suspend fun requestData(actualLoadType: LoadType): LoadResult<K, V> {
        val nextKey = nextKey
        val config = config

        val startRequestTime = System.currentTimeMillis()
        return try {
            val params = when (actualLoadType) {
                LoadType.Refresh -> LoadParams.Refresh(nextKey, config)
                LoadType.LoadMore -> LoadParams.LoadMore(nextKey, config)
                else -> error("Illegal LoadType: $actualLoadType")
            }
            val result = onRequest(params)
            val requestTimeCost = System.currentTimeMillis() - startRequestTime
            val delayTime = config.minRequestTimeMillis - requestTimeCost
            if (delayTime > 0) {
                delay(delayTime)
            }
            result
        } catch (exception: Exception) {
            if (config.printError) {
                exception.printStackTrace()
            }
            val requestTimeCost = System.currentTimeMillis() - startRequestTime
            val delayTime = config.minRequestTimeMillis - requestTimeCost
            if (delayTime > 0) {
                delay(delayTime)
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
            }
        }
    }
}