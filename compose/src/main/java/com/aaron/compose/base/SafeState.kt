package com.aaron.compose.base

import android.util.ArrayMap
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/10/25
 */
@Stable
abstract class SafeState<T>(
    private val delegate: MutableState<T>
) : State<T> by delegate {

    private val map: MutableMap<Any, Any?> by lazy { ArrayMap() }

    internal operator fun set(key: Any, value: Any?) {
        map[key] = value
    }

    internal fun <T> get(key: Any): T? {
        return map[key] as? T
    }

    internal fun <T> remove(key: Any): T? {
        return map.remove(key) as? T
    }

    internal fun fastRemove(key: Any): Any? {
        return remove(key)
    }

    internal fun setValue(value: T) {
        delegate.value = value
    }
}

private class SafeStateImpl<T>(value: T) : SafeState<T>(mutableStateOf(value))

fun <T> safeStateOf(value: T): SafeState<T> = SafeStateImpl(value)