package com.aaron.compose.architecture

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/9/18
 */
class SafeStateFlow<T> internal constructor(
    private val delegate: MutableStateFlow<T>
) : StateFlow<T> by delegate {

    internal suspend fun emit(value: T) {
        delegate.emit(value)
    }
}

fun <T> SafeStateFlow(value: T): SafeStateFlow<T> {
    return SafeStateFlow(MutableStateFlow(value))
}