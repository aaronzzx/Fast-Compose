package com.aaron.compose.ktx

import android.content.Context
import kotlin.math.max

/**
 * 计算出合适的 spanCount
 */
fun calcSpanCount(context: Context, minItemWidthDp: Int, minSpanCount: Int): Int {
    val displayMetrics = context.resources.displayMetrics
    val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
    val spanCount = (screenWidthDp / minItemWidthDp.toFloat() + 0.5).toInt()
    return max(spanCount, minSpanCount)
}