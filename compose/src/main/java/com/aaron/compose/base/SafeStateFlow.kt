package com.aaron.compose.base

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/10/26
 */
@Stable
abstract class SafeStateFlow<T>(
    private val delegate: MutableStateFlow<T>
) : StateFlow<T> by delegate {

    internal fun setValue(value: T) {
        delegate.value = value
    }
}

private class SafeStateFlowImpl<T>(value: T) : SafeStateFlow<T>(MutableStateFlow(value))

fun <T> safeStateFlowOf(value: T): SafeStateFlow<T> = SafeStateFlowImpl(value)