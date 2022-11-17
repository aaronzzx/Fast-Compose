package com.aaron.compose.paging

import com.aaron.compose.base.BasePagingResult
import com.aaron.compose.defaults.Defaults
import kotlinx.coroutines.CoroutineScope

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/11/12
 */
interface PagingScope {

    /**
     * 分页
     */
    fun <V> CoroutineScope.buildPageData(
        initialPage: Int,
        successCode: Int = Defaults.SuccessCode,
        pageConfig: PageConfig = PageConfig(),
        lazyLoad: Boolean = false,
        invokeCompletion: (suspend PageData<Int, V>.(LoadResult<Int, V>) -> Unit)? = null,
        onRequest: suspend (page: Int, pageSize: Int) -> BasePagingResult<V>
    ): PageData<Int, V> = buildMappingPageData(
        initialPage = initialPage,
        successCode = successCode,
        pageConfig = pageConfig,
        lazyLoad = lazyLoad,
        invokeCompletion = invokeCompletion,
        onMapping = { it },
        onRequest = onRequest
    )

    /**
     * 分页，可以转换数据
     */
    fun <V, R> CoroutineScope.buildMappingPageData(
        initialPage: Int,
        successCode: Int = Defaults.SuccessCode,
        pageConfig: PageConfig = PageConfig(),
        lazyLoad: Boolean = false,
        invokeCompletion: (suspend PageData<Int, R>.(LoadResult<Int, R>) -> Unit)? = null,
        onMapping: suspend (data: List<V>) -> List<R>,
        onRequest: suspend (page: Int, pageSize: Int) -> BasePagingResult<V>
    ): PageData<Int, R> = PageData(
        coroutineScope = this,
        config = pageConfig,
        lazyLoad = lazyLoad,
        invokeCompletion = invokeCompletion
    ) { params ->
        val pageSize = params.pageSize
        val initialSize = params.initialSize

        val curPage = params.key ?: initialPage
        val curPageSize = if (curPage == initialPage) initialSize else pageSize

        val result = onRequest(curPage, curPageSize)
        if (result.code == successCode) {
            val dataList = result.data
            var nextKey: Int? = when (curPage == initialPage) {
                true -> (initialSize / pageSize).plus(1)
                else -> curPage.plus(1)
            }
            if (dataList.size < pageSize) {
                nextKey = null
            }
            val mapped = onMapping(dataList)
            LoadResult.Page(mapped, nextKey)
        } else {
            LoadResult.Error(PageException(result.code, result.msg))
        }
    }
}