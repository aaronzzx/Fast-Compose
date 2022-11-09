package com.aaron.compose.safestate

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/11/8
 */
@Stable
abstract class SafeStateList<E>(
    private val delegate: SnapshotStateList<E>
) : List<E> by delegate {

    internal fun editInternal(): MutableList<E> {
        return delegate
    }
}

private class SafeStateListImpl<E> : SafeStateList<E>(mutableStateListOf())

fun <E> safeStateListOf(): SafeStateList<E> = SafeStateListImpl()

fun <E> safeStateListOf(vararg element: E): SafeStateList<E> {
    return SafeStateListImpl<E>().also {
        it.editInternal().addAll(element.toList())
    }
}

fun <E> Collection<E>.toSafeStateList(): SafeStateList<E> {
    return safeStateListOf<E>().also {
        it.editInternal().addAll(this)
    }
}