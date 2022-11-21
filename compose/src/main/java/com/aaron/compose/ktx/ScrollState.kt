package com.aaron.compose.ktx

import androidx.compose.foundation.ScrollState

/**
 * 判断能否垂直、水平滚动
 *
 * @param direction 小于 0 向上滚动(向左滚动(LTR))，大于 0 向下滚动(向右滚动(LTR))
 */
fun ScrollState.canScroll(direction: Int): Boolean {
    if (direction < 0) {
        return value > 0
    } else if (direction > 0) {
        return value < maxValue
    }
    return true
}