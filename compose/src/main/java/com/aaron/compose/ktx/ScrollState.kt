package com.aaron.compose.ktx

import androidx.compose.foundation.ScrollState

/**
 * 判断能否垂直滚动
 *
 * @param direction 小于 0 向上滚动，大于 0 向下滚动
 */
fun ScrollState.canScrollVertical(direction: Int): Boolean {
    if (direction < 0) {
        return value > 0
    } else if (direction > 0) {
        return value < maxValue
    }
    return true
}

/**
 * 判断能否水平滚动
 *
 * @param direction 小于 0 向左滚动，大于 0 向右滚动（LTR）
 */
fun ScrollState.canScrollHorizontal(direction: Int): Boolean {
    if (direction < 0) {
        return value > 0
    } else if (direction > 0) {
        return value < maxValue
    }
    return true
}