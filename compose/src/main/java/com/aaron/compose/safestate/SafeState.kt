package com.aaron.compose.safestate

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.structuralEqualityPolicy

/**
 * 简单地封装 [MutableState] ，目的是控制值的修改权限，仅在实现了 [SafeStateScope] 的范围下才能修改值。
 *
 * @author aaronzzxup@gmail.com
 * @since 2022/10/25
 */
@Stable
abstract class SafeState<T>(
    private val delegate: MutableState<T>
) : State<T> by delegate {

    internal fun setValueInternal(value: T) {
        delegate.value = value
    }
}

private class SafeStateImpl<T>(
    value: T,
    policy: SnapshotMutationPolicy<T> = structuralEqualityPolicy()
) : SafeState<T>(mutableStateOf(value, policy))

fun <T> safeStateOf(
    value: T,
    policy: SnapshotMutationPolicy<T> = structuralEqualityPolicy()
): SafeState<T> = SafeStateImpl(value, policy)