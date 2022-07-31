package com.aaron.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

/**
 * 屏幕适配
 *
 * @param adaptWidth 需要适配的屏幕宽度，一般拿设计稿上的屏幕宽度
 */
@Composable
fun ScreenAdapter(adaptWidth: Int, content: @Composable () -> Unit) {
    val metrics = LocalContext.current.resources.displayMetrics
    val fontScale = LocalDensity.current.fontScale
    val widthPixels = metrics.widthPixels
    CompositionLocalProvider(
        LocalDensity provides Density(
            density = widthPixels / adaptWidth.toFloat(),
            fontScale = fontScale
        ),
        content = content
    )
}