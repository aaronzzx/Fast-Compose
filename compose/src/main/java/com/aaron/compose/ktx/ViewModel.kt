package com.aaron.compose.ktx

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.aaron.compose.architecture.BaseResult
import com.aaron.compose.architecture.BaseViewStateVM
import com.aaron.compose.paging.IntPager
import com.aaron.compose.paging.PagerConfig
import kotlinx.coroutines.flow.Flow

/**
 * 使用 BaseViewStateVM 构建 Paging
 */
fun <I : BaseResult, O : Any> BaseViewStateVM.buildPager(
    successCode: Int = this.successCode,
    config: PagerConfig = PagerConfig(),
    initialPage: Int = 1,
    spanCount: Int = 1,
    printError: Boolean = false,
    onError: (() -> Unit)? = null,
    onTransform: (result: I?) -> List<O>?,
    onRequest: suspend (page: Int, pageSize: Int) -> I
): Flow<PagingData<O>> = IntPager(
    successCode = successCode,
    config = config,
    initialPage = initialPage,
    spanCount = spanCount,
    printError = printError,
    onError = onError,
    onTransform = onTransform,
    onRequest = onRequest
).flow.cachedIn(viewModelScope)