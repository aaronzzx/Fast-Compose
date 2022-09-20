package com.aaron.compose.architecture.paging

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/9/20
 */
@Stable
class PagingData<K, V>(
    private val coroutineScope: CoroutineScope,
    private val config: PagingConfig = PagingConfig(),
    private val onRequest: suspend (page: K?, config: PagingConfig) -> LoadResult<K, V>
) {

    companion object {
        private const val Debug = true
        private const val TAG = "PagingData"

        private fun log(msg: String) {
            if (Debug) {
                Log.d(TAG, msg)
            }
        }
    }

    var page: K? = null
        private set

    val data: SnapshotStateList<V> = mutableStateListOf()

    val count: Int get() = data.size

    val lastIndex: Int get() = count - 1

    val isEmpty: Boolean get() = count <= 0

    val isNotEmpty: Boolean get() = !isEmpty

    val loadState: CombinedLoadState by mutableStateOf(CombinedLoadState())

    private var loadType: LoadType = LoadType.Idle

    init {
        refresh()
    }

    internal operator fun get(index: Int): V {
        // 判断是否触发加载
        val prefetchDistance = config.prefetchDistance
        if (prefetchDistance > 0 && lastIndex - index == prefetchDistance) {
            loadMore()
        }
        return data[index]
    }

    internal fun peek(index: Int): V {
        return data[index]
    }

    fun refresh() {
        tryLaunch(LoadType.Refresh) {
            refreshImpl()
        }
    }

    private suspend fun refreshImpl() {
        val loadState = loadState
        loadState.refresh = LoadState.Loading
        page = null
        log("refresh-start: ${loadState.refresh}")
        when (val result = requestData()) {
            is LoadResult.Page -> {
                val dataList = result.data
                val nextPage = result.nextPage
                page = nextPage
                loadState.refresh = LoadState.Idle(false)
                loadState.loadMore = LoadState.Idle(nextPage == null)
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
        when (val result = requestData()) {
            is LoadResult.Page -> {
                val dataList = result.data
                val nextPage = result.nextPage
                page = nextPage
                loadState.loadMore = LoadState.Idle(nextPage == null)
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
        tryLaunch(LoadType.Retry) {
            val loadState = loadState
            if (loadState.refresh is LoadState.Error) {
                refreshImpl()
            } else if (loadState.loadMore is LoadState.Error) {
                loadMoreImpl()
            }
        }
    }

    private suspend fun requestData(): LoadResult<K, V> {
        val page = page
        val config = config

        val startRequestTime = System.currentTimeMillis()
        return try {
            val result = onRequest(page, config)
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
        if (this.loadType != LoadType.Idle) {
            return
        }
        this.loadType = loadType
        coroutineScope.launch {
            block()
        }.invokeOnCompletion {
            this.loadType = LoadType.Idle
        }
    }
}

fun <K, V> LazyListScope.items(
    pagingData: PagingData<K, V>,
    key: ((item: V) -> Any)? = null,
    contentType: ((item: V) -> Any?)? = null,
    itemContent: @Composable LazyItemScope.(item: V) -> Unit
) {
    items(
        count = pagingData.count,
        key = if (key == null) null else { index ->
            val item = pagingData.peek(index)
            if (item == null) {
                PagingDataKey(index)
            } else {
                key(item)
            }
        },
        contentType = { index ->
            contentType?.invoke(pagingData.peek(index))
        }
    ) { index ->
        itemContent(pagingData[index])
    }
}

fun <K, V> LazyListScope.itemsIndexed(
    pagingData: PagingData<K, V>,
    key: ((index: Int, item: V) -> Any)? = null,
    contentType: ((index: Int, item: V) -> Any?)? = null,
    itemContent: @Composable LazyItemScope.(index: Int, item: V) -> Unit
) {
    items(
        count = pagingData.count,
        key = if (key == null) null else { index ->
            val item = pagingData.peek(index)
            if (item == null) {
                PagingDataKey(index)
            } else {
                key(index, item)
            }
        },
        contentType = { index ->
            contentType?.invoke(index, pagingData.peek(index))
        }
    ) { index ->
        itemContent(index, pagingData[index])
    }
}

fun <K, V> LazyGridScope.items(
    pagingData: PagingData<K, V>,
    key: ((item: V) -> Any)? = null,
    contentType: ((item: V) -> Any?)? = null,
    span: (LazyGridItemSpanScope.(item: V) -> GridItemSpan)? = null,
    itemContent: @Composable LazyGridItemScope.(item: V) -> Unit
) {
    items(
        count = pagingData.count,
        key = if (key == null) null else { index ->
            val item = pagingData.peek(index)
            if (item == null) {
                PagingDataKey(index)
            } else {
                key(item)
            }
        },
        span = if (span == null) null else { index ->
            span(pagingData.peek(index))
        },
        contentType = { index ->
            contentType?.invoke(pagingData.peek(index))
        }
    ) { index ->
        itemContent(pagingData[index])
    }
}

fun <K, V> LazyGridScope.itemsIndexed(
    pagingData: PagingData<K, V>,
    key: ((index: Int, item: V) -> Any)? = null,
    contentType: ((index: Int, item: V) -> Any?)? = null,
    span: (LazyGridItemSpanScope.(index: Int, item: V) -> GridItemSpan)? = null,
    itemContent: @Composable LazyGridItemScope.(index: Int, item: V) -> Unit
) {
    items(
        count = pagingData.count,
        key = if (key == null) null else { index ->
            val item = pagingData.peek(index)
            if (item == null) {
                PagingDataKey(index)
            } else {
                key(index, item)
            }
        },
        span = if (span == null) null else { index ->
            span(index, pagingData.peek(index))
        },
        contentType = { index ->
            contentType?.invoke(index, pagingData.peek(index))
        }
    ) { index ->
        itemContent(index, pagingData[index])
    }
}

private data class PagingDataKey(private val index: Int) : Parcelable {

    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<PagingDataKey> =
            object : Parcelable.Creator<PagingDataKey> {
                override fun createFromParcel(parcel: Parcel) =
                    PagingDataKey(parcel.readInt())

                override fun newArray(size: Int) = arrayOfNulls<PagingDataKey?>(size)
            }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(index)
    }

    override fun describeContents(): Int {
        return 0
    }
}