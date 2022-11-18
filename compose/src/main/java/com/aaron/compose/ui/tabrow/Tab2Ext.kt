package com.aaron.compose.ui.tabrow

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * 禁用 Ripple 的 Tab
 */
@Composable
fun NonRippleTab2(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    selectedContentColor: Color = LocalContentColor.current,
    unselectedContentColor: Color = selectedContentColor.copy(alpha = ContentAlpha.medium),
    content: @Composable ColumnScope.() -> Unit
) {
    CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
        Tab2(
            selected = selected,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            interactionSource = interactionSource,
            selectedContentColor = selectedContentColor,
            unselectedContentColor = unselectedContentColor,
            content = content
        )
    }
}

/**
 * 禁用 Ripple 的 Tab
 */
@Composable
fun NonRippleTab2(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    selectedContentColor: Color = LocalContentColor.current,
    unselectedContentColor: Color = selectedContentColor.copy(alpha = ContentAlpha.medium)
) {
    CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
        Tab2(
            selected = selected,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            text = text,
            icon = icon,
            interactionSource = interactionSource,
            selectedContentColor = selectedContentColor,
            unselectedContentColor = unselectedContentColor
        )
    }
}

/**
 * 禁用 Ripple 的 LeadingTab
 */
@Composable
fun NonRippleLeadingIconTab2(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    selectedContentColor: Color = LocalContentColor.current,
    unselectedContentColor: Color = selectedContentColor.copy(alpha = ContentAlpha.medium),
    icon: @Composable (() -> Unit),
    text: @Composable (() -> Unit)
) {
    CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
        LeadingIconTab2(
            selected = selected,
            onClick = onClick,
            text = text,
            icon = icon,
            modifier = modifier,
            enabled = enabled,
            interactionSource = interactionSource,
            selectedContentColor = selectedContentColor,
            unselectedContentColor = unselectedContentColor
        )
    }
}

private object NoRippleTheme : RippleTheme {
    @Composable
    override fun defaultColor() = Color.Unspecified

    @Composable
    override fun rippleAlpha(): RippleAlpha = RippleAlpha(
        0.0f,
        0.0f,
        0.0f,
        0.0f
    )
}