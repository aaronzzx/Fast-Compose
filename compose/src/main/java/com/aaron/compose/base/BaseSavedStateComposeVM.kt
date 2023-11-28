package com.aaron.compose.base

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle

/**
 * 实现系统回收内存时保存状态
 *
 * @author aaronzzxup@gmail.com
 * @since 2023/11/28
 */
abstract class BaseSavedStateComposeVM<UiState : Any, UiEvent : Any>(
    savedStateHandle: SavedStateHandle
) : BaseComposeVM<UiState, UiEvent>() {

    companion object {
        private const val EXTRA_STATE = "EXTRA_STATE"
        private const val EXTRA_UI_STATE = "EXTRA_UI_STATE"
    }

    protected abstract val initialUiState: UiState

    final override val initialState: UiState
        get() {
            val uiState = getSavedUiState() ?: initialUiState
            check(uiState is Parcelable) {
                "UiState must implement android.os.Parcelable"
            }
            return uiState
        }

    protected val savedState: Bundle? = savedStateHandle.get<Bundle>(EXTRA_STATE)

    init {
        savedStateHandle.setSavedStateProvider(EXTRA_STATE) {
            Bundle().apply {
                onSaveState(this)
            }
        }
    }

    protected open fun onSaveState(saveState: Bundle) {
        val uiState = uiState
        if (uiState is Parcelable) {
            saveState.putParcelable(EXTRA_UI_STATE, uiState)
        }
    }

    private fun getSavedUiState(): UiState? {
        val savedState = savedState ?: return null
        return savedState.getParcelable2(EXTRA_UI_STATE) as? UiState
    }

    private fun Bundle.getParcelable2(key: String): Any? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelable(key, Any::class.java)
        } else {
            getParcelable(key)
        }
    }
}