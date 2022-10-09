package com.aaron.compose.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import androidx.paging.PagingState
import com.aaron.compose.architecture.BaseResult
import com.aaron.compose.defaults.Defaults

/**
 * 构建基于整型页码的 Pager
 */
fun <I : BaseResult, O : Any> buildReadablePager(
    config: AppPagingConfig = AppPagingConfig(),
    initialPage: Int = 1,
    successCode: Int = Defaults.SuccessCode,
    onTransform: suspend (result: I) -> List<O>,
    onRequest: suspend (pageKey: Int, pageSize: Int) -> I
): Pager<Int, O> = buildBaseReadablePager(
    config = config,
    initialKey = initialPage
) { params ->
    val initialLoadSize = config.initialLoadSize
    val pageKey = params.key ?: initialPage
    val pageSize = params.loadSize
    val response = onRequest(pageKey, pageSize)
    if (response.code == successCode) {
        val list = onTransform(response)
        val prevKey = when (pageKey == initialPage) {
            true -> null
            else -> pageKey - 1
        }
        var nextKey: Int? = when (pageKey == initialPage) {
            true -> (initialLoadSize / pageSize) + 1
            else -> pageKey + 1
        }
        if (!config.enableLoadMore) {
            nextKey = null
        }
        LoadResult.Page(list, prevKey, nextKey)
    } else {
        LoadResult.Error(AppPagingException(response.code, response.msg ?: "None"))
    }
}

/**
 * 构建最基本的 Pager
 */
fun <K : Any, V : Any> buildBaseReadablePager(
    config: AppPagingConfig = AppPagingConfig(),
    initialKey: K? = null,
    onLoad: suspend (params: LoadParams<K>) -> LoadResult<K, V>
): Pager<K, V> = Pager(
    config = PagingConfig(
        pageSize = config.pageSize,
        prefetchDistance = config.prefetchDistance,
        enablePlaceholders = config.enablePlaceholders,
        initialLoadSize = config.initialLoadSize,
        maxSize = config.maxSize
    ),
    initialKey = initialKey
) {
    object : PagingSource<K, V>() {
        override suspend fun load(params: LoadParams<K>): LoadResult<K, V> {
            val minRequestTimeMillis = config.minRequestTimeMillis
            val startTime = System.currentTimeMillis()
            return try {
                val result = onLoad(params)
                makeSureTime(startTime, minRequestTimeMillis)
                result
            } catch (ex: Exception) {
                makeSureTime(startTime, minRequestTimeMillis)
                LoadResult.Error(ex)
            }
        }

        override fun getRefreshKey(state: PagingState<K, V>): K? {
            return null
        }
    }
}