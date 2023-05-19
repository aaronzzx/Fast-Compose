package com.aaron.compose.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.utils.SystemFontScaleHandler

/**
 * 开箱即用型 Badge
 *
 * @param number 提示数，传 null 则只显示小红点
 * @param fontSize 文本大小
 * @param maxCharacterCount 最多显示几位数，包含超出后显示的 +
 * @param backgroundColor 背景色
 * @param contentColor 内容色
 * @param contentPadding 内间距
 * @param enableSystemFontScale 是否响应系统字体缩放
 * @param content 自己决定显示什么
 */
@Composable
fun FastBadge(
    number: Int?,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    maxCharacterCount: Int = 3,
    backgroundColor: Color = MaterialTheme.colors.error,
    contentColor: Color = contentColorFor(backgroundColor),
    contentPadding: PaddingValues = PaddingValues(horizontal = 4.dp),
    enableSystemFontScale: Boolean = false,
    content: (@Composable (String) -> Unit)? = null
) {
    Badge(
        modifier = modifier,
        fontSize = fontSize,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        contentPadding = contentPadding,
        enableSystemFontScale = enableSystemFontScale,
        content = if (number == null) null else {
            {
                val text = remember(number) {
                    var numberText = number.toString()
                    if (numberText.length >= maxCharacterCount) {
                        var temp = ""
                        repeat(maxCharacterCount - 1) {
                            temp += "9"
                        }
                        numberText = "$temp+"
                    }
                    numberText
                }
                if (content != null) {
                    content(text)
                } else {
                    Text(text = text)
                }
            }
        }
    )
}

/**
 * 红点提示，不设置内容则显示一个小红点
 *
 * @param fontSize 文本大小
 * @param backgroundColor 背景色
 * @param contentColor 内容色
 * @param contentPadding 内间距
 * @param enableSystemFontScale 是否响应系统字体缩放
 * @param content 如果没内容将显示小红点
 */
@Composable
fun Badge(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    backgroundColor: Color = MaterialTheme.colors.error,
    contentColor: Color = contentColorFor(backgroundColor),
    contentPadding: PaddingValues = PaddingValues(horizontal = 4.dp),
    enableSystemFontScale: Boolean = false,
    content: @Composable (BoxScope.() -> Unit)? = null
) {
    val radius = if (content != null) 10.dp else 4.dp

    // Draw badge container.
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = radius * 2, minHeight = radius * 2)
            .clipToBackground(
                color = backgroundColor,
                shape = CircleShape
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        if (content != null) {
            SystemFontScaleHandler(enabled = enableSystemFontScale) {
                CompositionLocalProvider(
                    LocalContentColor provides contentColor
                ) {
                    ProvideTextStyle(
                        value = TextStyle(
                            color = contentColor,
                            fontSize = fontSize
                        ),
                        content = { content() }
                    )
                }
            }
        }
    }
}