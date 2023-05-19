package com.aaron.compose.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 图标按钮开箱即用
 *
 * @param enabled 是否启用点击
 * @param clickIntervalMs 点击防抖间隔
 * @param size 默认最小尺寸
 * @param indication 点击效果
 * @param interactionSource 监听交互
 */
@Composable
fun FastIconButton(
    onSingleClick: () -> Unit,
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    clickIntervalMs: Long = 800,
    size: Dp = 48.dp,
    indication: Indication? = rememberRipple(bounded = false, radius = size / 2),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Inside,
    alpha: Float = 1.0f,
    colorFilter: ColorFilter? = null
) {
    ResizeIconButton(
        onSingleClick = onSingleClick,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        clickIntervalMs = clickIntervalMs,
        size = size,
        indication = indication,
        interactionSource = interactionSource
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            contentScale = contentScale,
            alpha = alpha,
            colorFilter = colorFilter
        )
    }
}

/**
 * 开箱即用的两种状态的图标按钮
 *
 * @param checked 是否选中
 * @param enabled 是否启用
 * @param size 默认最小尺寸
 * @param indication 点击效果
 * @param interactionSource 点击交互
 */
@Composable
fun FastIconToggleButton(
    @DrawableRes normalRes: Int,
    @DrawableRes checkedRes: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = 48.dp,
    indication: Indication? = rememberRipple(bounded = false, radius = size / 2),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Inside,
    alpha: Float = 1.0f,
    colorFilter: ColorFilter? = null
) {
    ResizeIconToggleButton(
        modifier = modifier,
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        size = size,
        indication = indication,
        interactionSource = interactionSource
    ) {
        Image(
            painter = painterResource(id = if (checked) checkedRes else normalRes),
            contentDescription = contentDescription,
            contentScale = contentScale,
            alpha = alpha,
            colorFilter = colorFilter
        )
    }
}

/**
 * 不受 48.dp 最小尺寸限制的图标按钮
 *
 * @param enabled 是否启用点击
 * @param clickIntervalMs 点击防抖间隔
 * @param size 默认最小尺寸
 * @param indication 点击效果
 * @param interactionSource 监听交互
 */
@Composable
fun ResizeIconButton(
    onSingleClick: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    clickIntervalMs: Long = 800,
    size: Dp = 48.dp,
    indication: Indication? = rememberRipple(bounded = false, radius = size / 2),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = size, minHeight = size)
            .clickable(
                onClick = run {
                    var clickTime by remember {
                        mutableStateOf(0L)
                    }
                    val block = {
                        onClick?.invoke()
                        val curTime = System.currentTimeMillis()
                        if (curTime - clickTime > clickIntervalMs) {
                            clickTime = curTime
                            onSingleClick()
                        }
                    }
                    block
                },
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = indication
            ),
        contentAlignment = Alignment.Center
    ) {
        val contentAlpha = if (enabled) LocalContentAlpha.current else ContentAlpha.disabled
        CompositionLocalProvider(LocalContentAlpha provides contentAlpha, content = content)
    }
}

/**
 * 不受 48.dp 最小尺寸限制，切换两种状态的图标按钮
 *
 * @param checked 是否选中
 * @param enabled 是否启用点击
 * @param size 默认最小尺寸
 * @param indication 点击效果
 * @param interactionSource 监听交互
 */
@Composable
fun ResizeIconToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = 48.dp,
    indication: Indication? = rememberRipple(bounded = false, radius = size / 2),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .defaultMinSize(
                minWidth = size,
                minHeight = size
            )
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                enabled = enabled,
                role = Role.Checkbox,
                interactionSource = interactionSource,
                indication = indication
            ),
        contentAlignment = Alignment.Center
    ) {
        val contentAlpha = if (enabled) LocalContentAlpha.current else ContentAlpha.disabled
        CompositionLocalProvider(LocalContentAlpha provides contentAlpha, content = content)
    }
}