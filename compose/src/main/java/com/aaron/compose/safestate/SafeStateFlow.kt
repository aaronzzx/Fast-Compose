package com.aaron.compose.safestate

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/10/26
 */
@Stable
abstract class SafeStateFlow<T>(
    private val delegate: MutableStateFlow<T>
) : StateFlow<T> by delegate {

    @PublishedApi
    internal suspend fun emitInternal(value: T) {
        delegate.emit(value)
    }

    @PublishedApi
    internal fun tryEmitInternal(value: T) {
        delegate.tryEmit(value)
    }

    @PublishedApi
    internal fun updateInternal(block: (T) -> T) {
        delegate.update(block)
    }
}

private class SafeStateFlowImpl<T>(value: T) : SafeStateFlow<T>(MutableStateFlow(value))

fun <T> safeStateFlowOf(value: T): SafeStateFlow<T> = SafeStateFlowImpl(value)