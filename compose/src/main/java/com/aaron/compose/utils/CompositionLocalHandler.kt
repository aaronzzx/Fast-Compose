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
 *
 * @param enabled false 为不显示 OverScroll 。
 * @param defaultFactory 当 [enabled] 为 true 时调用。
 */
@Composable
fun OverScrollHandler(
    enabled: Boolean,
    defaultFactory: @Composable () -> OverscrollConfiguration = { OverscrollConfiguration() },
    content: @Composable () -> Unit
) {
    val provided = when (enabled) {
        true -> defaultFactory()
        else -> null
    }
    CompositionLocalProvider(
        LocalOverscrollConfiguration provides provided,
        content = content,
    )
}

/**
 * 处理系统级字体缩放
 *
 * @param enabled false 为禁用缩放。
 * @param defaultFactory 当 [enabled] 为 true 时调用。
 */
@Composable
fun SystemFontScaleHandler(
    enabled: Boolean,
    defaultFactory: @Composable () -> Density = { createDefaultDensity() },
    content: @Composable () -> Unit
) {
    val provided = when (enabled) {
        true -> defaultFactory()
        else -> Density(density = LocalDensity.current.density, fontScale = 1f)
    }
    CompositionLocalProvider(
        LocalDensity provides provided,
        content = content
    )
}

/**
 * 处理屏幕适配。
 *
 * @param adaptWidth 需要适配的屏幕宽度，一般拿设计稿上的屏幕宽度，传小于等于 0 的值为取消适配
 * @param defaultFactory 当 [adaptWidth] 小于等于 0 时调用。
 */
@Composable
fun FitScreenHandler(
    adaptWidth: Int,
    defaultFactory: @Composable () -> Density = { createDefaultDensity() },
    content: @Composable () -> Unit
) {
    val fontScale = LocalDensity.current.fontScale
    val widthPixels = LocalContext.current.resources.displayMetrics.widthPixels
    val provided = when {
        adaptWidth > 0 -> Density(
            density = widthPixels / adaptWidth.toFloat(),
            fontScale = fontScale
        )
        else -> defaultFactory()
    }
    CompositionLocalProvider(
        LocalDensity provides provided,
        content = content
    )
}

@Composable
private fun createDefaultDensity() = Density(
    density = LocalDensity.current.density,
    fontScale = LocalContext.current.resources.configuration.fontScale
)

/**
 * 用于 CompositionLocal
 */
fun noLocalProvidedFor(name: String): Nothing {
    error("CompositionLocal $name not present")
}
