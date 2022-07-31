package com.aaron.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color

/**
 * 带水波纹点击事件
 *
 * @param enableRipple 是否支持水波纹效果
 * @param rippleColor 水波纹颜色
 * @param onClick 点击回调
 */
fun Modifier.onClick(
    enableRipple: Boolean = true,
    rippleColor: Color = Color.Unspecified,
    onClick: () -> Unit
) = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = if (enableRipple) rememberRipple(color = rippleColor, bounded = true) else null
    ) {
        onClick()
    }
}