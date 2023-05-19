package com.aaron.compose.ktx

import androidx.compose.runtime.MutableState

/**
 * @author aaronzzxup@gmail.com
 * @since 2023/4/11
 */

inline fun <T> MutableState<T>.update(block: (T) -> T) {
    value = block(value)
}