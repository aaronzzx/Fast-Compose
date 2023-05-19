package com.aaron.compose.safestate

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 简单地封装 [MutableStateFlow] ，目的是控制值的修改权限，仅在实现了 [SafeStateScope] 的范围下才能修改值。
 *
 * @author aaronzzxup@gmail.com
 * @since 2022/10/26
 */
@Stable
abstract class SafeStateFlow<T>(
    private val delegate: MutableStateFlow<T>
) : StateFlow<T> by delegate {

    internal suspend fun emitInternal(value: T) {
        delegate.emit(value)
    }

    internal fun tryEmitInternal(value: T): Boolean {
        return delegate.tryEmit(value)
    }
}

private class SafeStateFlowImpl<T>(value: T) : SafeStateFlow<T>(MutableStateFlow(value))

fun <T> safeStateFlowOf(value: T): SafeStateFlow<T> = SafeStateFlowImpl(value)