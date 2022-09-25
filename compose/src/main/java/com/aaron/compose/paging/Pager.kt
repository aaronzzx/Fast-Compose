package com.aaron.compose.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.aaron.compose.architecture.BasePagingResult
import kotlinx.coroutines.delay

/**
 * 页码为 Int 的 Paging
 */
fun <O : Any, I : BasePagingResult<O>> intPager(
    successCode: Int,
    config: AppPagingConfig = AppPagingConfig(),
    initialPage: Int = 1,
    spanCount: Int = 1,
    printError: Boolean = false,
    onResult: ((I) -> Unit)? = null,
    onError: (() -> Unit)? = null,
    remoteMediator: RemoteMediator<Int, O>? = null,
    onRequest: suspend (page: Int, pageSize: Int) -> I
): Pager<Int, O> = appPager(
    config = config,
    initialKey = initialPage,
    printError = printError,
    onRefreshKey = { prevKey, nextKey ->
        prevKey?.plus(1) ?: nextKey?.minus(1)
    },
    onError = onError,
    remoteMediator = remoteMediator
) {
    val everyPageSize = config.pageSize
    val initPageSize = config.initialLoadSize

    val currentPage = it.key ?: initialPage
    val pageSize = if (currentPage == initialPage) initPageSize else everyPageSize
    val result = onRequest.invoke(currentPage, pageSize)
    onResult?.invoke(result)

    if (result.code == successCode) {
        val responseList = result.data

        val preKey: Int? = when (currentPage == initialPage) {
            true -> null
            else -> currentPage.minus(1)
        }
        var nextKey: Int? = when (currentPage == initialPage) {
            true -> (initPageSize / everyPageSize).plus(1)
            else -> currentPage.plus(1)
        }

        if (responseList.size * spanCount < everyPageSize
            || !config.enableLoadMore
            || currentPage >= config.maxPage
        ) {
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
}

/**
 * 通用构建 Paging
 */
fun <K : Any, V : Any> appPager(
    config: AppPagingConfig = AppPagingConfig(),
    initialKey: K? = null,
    printError: Boolean = false,
    onRefreshKey: ((prevKey: K?, nextKey: K?) -> K?)? = null,
    onError: (() -> Unit)? = null,
    remoteMediator: RemoteMediator<K, V>? = null,
    onRequest: suspend (PagingSource.LoadParams<K>) -> PagingSource.LoadResult<K, V>
): Pager<K, V> = Pager(
    config = PagingConfig(
        config.pageSize,
        initialLoadSize = config.initialLoadSize,
        prefetchDistance = config.prefetchDistance,
        maxSize = config.maxSize,
        enablePlaceholders = config.enablePlaceholders
    ),
    initialKey = initialKey,
    remoteMediator = remoteMediator
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