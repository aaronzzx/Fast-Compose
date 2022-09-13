package com.aaron.compose.utils

import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.OverscrollConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/**
 * 控制 OverScroll 是否显示
 */
@Composable
fun OverScrollHandler(enabled: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalOverscrollConfiguration provides if (enabled) OverscrollConfiguration() else null
    ) {
        content()
    }
}

/**
 * 控制系统级字体缩放
 */
@Composable
fun SystemFontScaleHandler(enabled: Boolean, content: @Composable () -> Unit) {
    val density = LocalDensity.current.density
    val fontScale = LocalContext.current.resources.configuration.fontScale
    CompositionLocalProvider(
        LocalDensity provides Density(
            density = density,
            fontScale = if (enabled) fontScale else 1f
        ),
        content = content
    )
}

/**
 * 控制屏幕适配
 *
 * @param adaptWidth 需要适配的屏幕宽度，一般拿设计稿上的屏幕宽度，传小于等于 0 的值为取消适配
 */
@Composable
fun FitScreenHandler(adaptWidth: Int, content: @Composable () -> Unit) {
    val metrics = LocalContext.current.resources.displayMetrics
    val density = metrics.density
    val fontScale = LocalDensity.current.fontScale
    val widthPixels = metrics.widthPixels
    CompositionLocalProvider(
        LocalDensity provides Density(
            density = if (adaptWidth > 0) widthPixels / adaptWidth.toFloat() else density,
            fontScale = fontScale
        ),
        content = content
    )
}

/**
 * 用于 CompositionLocal
 */
fun noLocalProvidedFor(name: String): Nothing {
    error("CompositionLocal $name not present")
}
