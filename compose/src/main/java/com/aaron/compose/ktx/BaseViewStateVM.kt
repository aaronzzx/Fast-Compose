package com.aaron.compose.ktx

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.RemoteMediator
import androidx.paging.cachedIn
import com.aaron.compose.architecture.BasePagingResult
import com.aaron.compose.architecture.BaseViewStateVM
import com.aaron.compose.architecture.BaseViewStateVM.Companion.DefaultSuccessCode
import com.aaron.compose.paging.intPager
import com.aaron.compose.paging.AppPagingConfig
import kotlinx.coroutines.flow.Flow

/**
 * 使用 BaseViewStateVM 构建 Paging
 */
fun <O : Any, I : BasePagingResult<O>> BaseViewStateVM.buildPagingFlow(
    successCode: Int = DefaultSuccessCode,
    config: AppPagingConfig = AppPagingConfig(),
    initialPage: Int = 1,
    spanCount: Int = 1,
    printError: Boolean = false,
    onResult: ((I) -> Unit)? = null,
    onError: (() -> Unit)? = null,
    remoteMediator: RemoteMediator<Int, O>? = null,
    onRequest: suspend (page: Int, pageSize: Int) -> I
): Flow<PagingData<O>> = intPager(
    successCode = successCode,
    config = config,
    initialPage = initialPage,
    spanCount = spanCount,
    printError = printError,
    onResult = onResult,
    onError = onError,
    remoteMediator = remoteMediator,
    onRequest = onRequest
).flow.cachedIn(viewModelScope)