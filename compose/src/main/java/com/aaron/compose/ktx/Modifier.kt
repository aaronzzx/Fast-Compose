package com.aaron.compose.ktx

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/**
 * 带水波纹点击事件
 *
 * @param enableRipple 是否支持水波纹效果
 * @param rippleColor 水波纹颜色
 * @param rippleBounded 如果为真，波纹会被目标布局的边界截断。无界波纹总是从目标布局中心开始动画，有界波纹总是从触摸位置开始动画
 * @param rippleRadius 波纹的半径。如果设置 [Dp.Unspecified] 则大小将根据目标布局大小计算。
 * @param onClick 点击回调
 */
fun Modifier.onClick(
    enableRipple: Boolean = true,
    rippleColor: Color = Color.Unspecified,
    rippleBounded: Boolean = true,
    rippleRadius: Dp = Dp.Unspecified,
    onClick: () -> Unit
) = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = if (enableRipple) rememberRipple(rippleBounded, rippleRadius, rippleColor) else null
    ) {
        onClick()
    }
}