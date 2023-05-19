package com.aaron.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 为内容附加分割线
 */
@Composable
fun WithDivider(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
    startIndent: Dp = 0.dp,
    endIndent: Dp = 0.dp,
    thickness: Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier) {
        content()
        if (enabled) {
            Divider(
                color = color,
                startIndent = startIndent,
                endIndent = endIndent,
                thickness = thickness
            )
        }
    }
}

@Composable
fun Divider(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFF2F2F2),
    startIndent: Dp = 0.dp,
    endIndent: Dp = 0.dp,
    thickness: Dp = 1.dp
) {
    require(thickness > Dp.Hairline)
    val startIndentMod = if (startIndent.value != 0f) {
        Modifier.padding(start = startIndent)
    } else {
        Modifier
    }
    val endIndentMod = if (endIndent.value != 0f) {
        Modifier.padding(end = endIndent)
    } else {
        Modifier
    }
    Box(
        modifier = modifier
            .then(startIndentMod)
            .then(endIndentMod)
            .fillMaxWidth()
            .height(thickness)
            .background(color = color)
    )
}