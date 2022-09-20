package com.aaron.compose.architecture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
}