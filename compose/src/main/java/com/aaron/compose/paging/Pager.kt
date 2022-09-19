package com.aaron.compose.paging

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.aaron.compose.architecture.BaseResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

fun <R : BaseResult, I : Any> pagerOnFlow(
    successCode: Int,
    config: PagerConfig = PagerConfig(),
    spanCount: Int = 1,
    onTransform: (result: R?) -> List<I>?,
    onRequest: suspend (page: Int, config: Int) -> R
): Flow<PagingData<I>> = Pager(config, 1) {
    val currentPage = it.key ?: 1
    val result = onRequest.invoke(currentPage, if (currentPage == 1) config.initialLoadSize else config.pageSize)
    if (result.code == successCode) {
        val responseList = onTransform.invoke(result) ?: emptyList()
        Log.e("ssk2", "responseList.size=${responseList.size}")

        val everyPageSize = config.pageSize
        val initPageSize = config.initialLoadSize
        val preKey = if (currentPage == 1) null else currentPage.minus(1)
        var nextKey: Int? = if (currentPage == 1) {
            (initPageSize / everyPageSize).plus(1)
        } else {
            currentPage.plus(1)
        }

        if (responseList.size * spanCount < everyPageSize || !config.enableLoadMore) {
            nextKey = null
        }

        PagingSource.LoadResult.Page(
            data = responseList,
            prevKey = preKey,
            nextKey = nextKey
        )
    } else {
        PagingSource.LoadResult.Error(PagingException(result.code, result.msg))
    }
}.flow

fun <K : Any, V : Any> Pager(
    config: PagerConfig = PagerConfig(),
    initialKey: K? = null,
    printError: Boolean = false,
    onRefreshKey: ((prevKey: K?, nextKey: K?) -> K?)? = null,
    onError: (() -> Unit)? = null,
    onRequest: suspend (PagingSource.LoadParams<K>) -> PagingSource.LoadResult<K, V>
): Pager<K, V> = Pager(
    config = PagingConfig(
        config.pageSize,
        initialLoadSize = config.initialLoadSize,
        prefetchDistance = config.prefetchDistance,
        maxSize = config.maxSize,
        enablePlaceholders = config.enablePlaceholders
    ),
    initialKey = initialKey
) {
    object : PagingSource<K, V>() {
        override suspend fun load(params: LoadParams<K>): LoadResult<K, V> {
            val startRequestTime = System.currentTimeMillis()
            return try {
                val result = onRequest.invoke(params)
                val requestTimeCost = System.currentTimeMillis() - startRequestTime
                val delayTime = config.minRequestTimeMillis - requestTimeCost
                if (delayTime > 0) {
                    delay(delayTime)
                }
                result
            } catch (e: Exception) {
                if (printError) {
                    e.printStackTrace()
                }
                val requestTimeCost = System.currentTimeMillis() - startRequestTime
                val delayTime = config.minRequestTimeMillis - requestTimeCost
                if (delayTime > 0) {
                    delay(delayTime)
                }
                onError?.invoke()
                LoadResult.Error(e)
            }
        }

        override fun getRefreshKey(state: PagingState<K, V>): K? {
            // Try to find the page key of the closest page to anchorPosition, from
            // either the prevKey or the nextKey, but you need to handle nullability
            // here:
            //  * prevKey == null -> anchorPage is the first page.
            //  * nextKey == null -> anchorPage is the last page.
            //  * both prevKey and nextKey null -> anchorPage is the initial page, so
            //    just return null.
            return state.anchorPosition?.let { anchorPosition ->
                val anchorPage = state.closestPageToPosition(anchorPosition)
                // anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
                onRefreshKey?.invoke(anchorPage?.prevKey, anchorPage?.nextKey)
            }
        }
    }
}

class PagingException(val code: Int, val msg: String?) : Exception("$code, ${msg ?: "Unknown"}")