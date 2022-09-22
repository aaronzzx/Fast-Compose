package com.aaron.compose.ktx

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aaron.compose.architecture.BasePagingResult
import com.aaron.compose.architecture.BaseResult
import com.aaron.compose.architecture.ViewState
import com.aaron.compose.architecture.ViewStateFlow
import com.aaron.compose.architecture.paging.LoadResult
import com.aaron.compose.architecture.paging.PageConfig
import com.aaron.compose.architecture.paging.PageData
import com.aaron.compose.architecture.paging.PageException
import com.aaron.compose.defaults.Defaults.SuccessCode
import kotlinx.coroutines.launch

/**
 * 使用 Flow 发射 ViewState
 */
fun <T : BaseResult> ViewModel.emit(
    observer: ViewStateFlow<T>,
    successCode: Int = SuccessCode,
    request: suspend () -> T
) {
    viewModelScope.launch {
        runCatching {
            observer.emit(ViewState.Loading)
            request()
        }.onSuccess { result ->
            if (result.code == successCode) {
                observer.emit(ViewState.Success(result))
            } else {
                observer.emit(ViewState.Failure(result.code, result.msg))
            }
        }.onFailure { exception ->
            observer.emit(ViewState.Error(exception))
        }
    }
}

/**
 * 分页
 */
fun <V> ViewModel.buildPageData(
    initialPage: Int,
    successCode: Int = SuccessCode,
    pageConfig: PageConfig = PageConfig(),
    lazyLoad: Boolean = false,
    onRequest: suspend (page: Int, pageSize: Int) -> BasePagingResult<V>
): PageData<Int, V> = buildMappingPageData(
    initialPage = initialPage,
    successCode = successCode,
    pageConfig = pageConfig,
    lazyLoad = lazyLoad,
    onRequest = onRequest,
    onMapping = { it }
)

/**
 * 分页，可以转换数据
 */
fun <V, R> ViewModel.buildMappingPageData(
    initialPage: Int,
    successCode: Int = SuccessCode,
    pageConfig: PageConfig = PageConfig(),
    lazyLoad: Boolean = false,
    onRequest: suspend (page: Int, pageSize: Int) -> BasePagingResult<V>,
    onMapping: suspend (data: List<V>) -> List<R>
): PageData<Int, R> = PageData(
    coroutineScope = viewModelScope,
    config = pageConfig,
    lazyLoad = lazyLoad
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