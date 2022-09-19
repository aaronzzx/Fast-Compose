package com.aaron.compose.architecture

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/9/18
 */
sealed class ViewState<out T> {

    data class Initial<T>(val data: T? = null) : ViewState<T>()

    class Loading : ViewState<Nothing>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }

    data class Success<T>(val data: T) : ViewState<T>()

    data class Fail(val code: Int, val msg: String?) : ViewState<Nothing>()

    data class Error(val exception: Throwable) : ViewState<Nothing>()
}

typealias ViewStateFlow<T> = SafeStateFlow<ViewState<T>>

inline fun <T> viewStateFlow(
    crossinline initial: () -> ViewState<T> = { ViewState.Initial() }
): Lazy<ViewStateFlow<T>> {
    return lazy { SafeStateFlow(initial()) }
}