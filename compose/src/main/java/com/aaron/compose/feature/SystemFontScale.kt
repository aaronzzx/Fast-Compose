package com.aaron.compose.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/**
 * 屏蔽系统级字体缩放
 */
@Composable
fun DisableFontScale(content: @Composable () -> Unit) {
    val density = LocalDensity.current.density
    CompositionLocalProvider(
        LocalDensity provides Density(
            density = density,
            fontScale = 1f
        ),
        content = content
    )
}

/**
 * 启用系统级字体缩放
 */
@Composable
fun EnableFontScale(content: @Composable () -> Unit) {
    val density = LocalDensity.current.density
    val fontScale = LocalContext.current.resources.configuration.fontScale
    CompositionLocalProvider(
        LocalDensity provides Density(
            density = density,
            fontScale = fontScale
        ),
        content = content
    )
}