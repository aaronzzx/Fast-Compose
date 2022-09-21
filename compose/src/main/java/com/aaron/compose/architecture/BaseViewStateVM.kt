package com.aaron.compose.architecture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aaron.compose.architecture.paging.LoadResult
import com.aaron.compose.architecture.paging.PageConfig
import com.aaron.compose.architecture.paging.PageData
import com.aaron.compose.architecture.paging.PageException
import kotlinx.coroutines.launch

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/9/18
 */
abstract class BaseViewStateVM : ViewModel() {

    companion object {
        var DefaultSuccessCode = 200
    }

    protected fun <T : BaseResult> emit(
        observer: ViewStateFlow<T>,
        successCode: Int = DefaultSuccessCode,
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

    protected fun <V> buildPager(
        initialPage: Int,
        spanCount: Int = 1,
        successCode: Int = DefaultSuccessCode,
        pageConfig: PageConfig = PageConfig(),
        onRequest: suspend (page: Int, pageSize: Int) -> BasePagingResult<V>
    ): PageData<Int, V> = PageData(
        coroutineScope = viewModelScope,
        config = pageConfig
    ) { page, config ->
        val pageSize = config.pageSize
        val initialSize = config.initialSize

        val curPage = page ?: initialPage
        val curPageSize = if (curPage == initialPage) initialSize else pageSize

        val result = onRequest(curPage, curPageSize)
        if (result.code == successCode) {
            val dataList = result.data
            var nextPage: Int? = when (curPage == initialPage) {
                true -> (initialSize / pageSize).plus(1)
                else -> curPage.plus(1)
            }
            if (dataList.size * spanCount < pageSize
                || !config.enableLoadMore
                || curPage >= config.maxPage
            ) {
                nextPage = null
            }
            LoadResult.Page(dataList, nextPage)
        } else {
            LoadResult.Error(PageException(result.code, result.msg))
        }
    }
}