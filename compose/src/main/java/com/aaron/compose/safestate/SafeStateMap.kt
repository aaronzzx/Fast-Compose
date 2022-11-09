package com.aaron.compose.safestate

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/11/8
 */
@Stable
abstract class SafeStateMap<K, V>(
    private val delegate: SnapshotStateMap<K, V>
) : Map<K, V> by delegate {

    internal fun editInternal(): MutableMap<K, V> {
        return delegate
    }
}

private class SafeStateMapImpl<K, V> : SafeStateMap<K, V>(mutableStateMapOf())

fun <K, V> safeStateMapOf(): SafeStateMap<K, V> = SafeStateMapImpl()

fun <K, V> safeStateMapOf(vararg pairs: Pair<K, V>): SafeStateMap<K, V> {
    return SafeStateMapImpl<K, V>().also {
        it.editInternal().putAll(pairs.toMap())
    }
}

fun <K, V> Iterable<Pair<K, V>>.toSafeStateMap(): SafeStateMap<K, V> {
    return safeStateMapOf<K, V>().also {
        it.editInternal().putAll(this.toMap())
    }
}