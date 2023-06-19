package com.aaron.compose.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.aaron.compose.ktx.getCenterOffset
import com.aaron.compose.ktx.roundToPx

/**
 * 自动缩放字体大小的文本控件
 *
 * @param text 文本
 * @param modifier 修饰符
 * @param fontSizeRange 字体大小区间
 * @param maxLines 最大行数
 * @param overflow 溢出处理
 * @param softWrap 软换行：在文本编辑器或处理器中，自动将长文本行分为多行以适应屏幕宽度的功能。
 * @param fitCenter 偏移到控件中间，比基线稍上一些
 * @param style 文本风格
 */
@Composable
fun AutoResizeText(
    text: String,
    modifier: Modifier = Modifier,
    fontSizeRange: FontSizeRange = DefaultFontSizeRange,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    fitCenter: Boolean = false,
    style: TextStyle = LocalTextStyle.current
) {
    AutoResizeText(
        text = AnnotatedString(text),
        modifier = modifier,
        fontSizeRange = fontSizeRange,
        maxLines = maxLines,
        overflow = overflow,
        softWrap = softWrap,
        fitCenter = fitCenter,
        style = style
    )
}

/**
 * 自动缩放字体大小的文本控件
 *
 * @param text 文本
 * @param modifier 修饰符
 * @param fontSizeRange 字体大小区间
 * @param maxLines 最大行数
 * @param overflow 溢出处理
 * @param softWrap 软换行：在文本编辑器或处理器中，自动将长文本行分为多行以适应屏幕宽度的功能。
 * @param fitCenter 偏移到控件中间，比基线稍上一些
 * @param style 文本风格
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun AutoResizeText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    fontSizeRange: FontSizeRange = DefaultFontSizeRange,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    fitCenter: Boolean = false,
    style: TextStyle = LocalTextStyle.current
) {
    BoxWithConstraints(modifier = modifier) {
        val textMeasurer = rememberTextMeasurer()
        val textResult = run {
            val constraints = Constraints(
                maxWidth = maxWidth.roundToPx(),
                maxHeight = maxHeight.roundToPx()
            )
            textMeasurer.measureSuitable(
                text = text,
                style = style,
                maxLines = maxLines,
                overflow = overflow,
                softWrap = softWrap,
                fontSizeRange = fontSizeRange,
                constraints = constraints
            )
        }

        val density = LocalDensity.current
        val width by remember(density, constraints) {
            derivedStateOf {
                if (constraints.hasFixedWidth) {
                    maxWidth
                } else {
                    density.run { textResult.size.width.toDp() }
                }
            }
        }
        val height by remember(density, constraints) {
            derivedStateOf {
                if (constraints.hasFixedHeight) {
                    maxHeight
                } else {
                    density.run { textResult.size.height.toDp() }
                }
            }
        }
        Canvas(modifier = Modifier.size(width, height)) {
            if (fitCenter) {
                val textOffset = textResult.getCenterOffset(0)
                translate(top = size.height / 2 - textResult.size.height / 2 - textOffset) {
                    drawText(textResult)
                }
            } else {
                drawText(textResult)
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class)
private fun TextMeasurer.measureSuitable(
    text: AnnotatedString,
    style: TextStyle,
    maxLines: Int,
    overflow: TextOverflow,
    softWrap: Boolean,
    fontSizeRange: FontSizeRange,
    constraints: Constraints
): TextLayoutResult {
    val measure: (TextUnit) -> TextLayoutResult = {
        measure(
            text = text,
            maxLines = maxLines,
            overflow = overflow,
            softWrap = softWrap,
            style = style.copy(fontSize = it),
            constraints = constraints
        )
    }

    var fontSize = fontSizeRange.max.value
    var textResult = measure(fontSize.sp)
    while (textResult.hasVisualOverflow) {
        fontSize -= fontSizeRange.step.value
        if (fontSize < fontSizeRange.min.value) {
            fontSize = fontSizeRange.min.value
        }
        textResult = measure(fontSize.sp)
        if (fontSize == fontSizeRange.min.value) {
            break
        }
    }
    return textResult
}

data class FontSizeRange(
    val min: TextUnit,
    val max: TextUnit,
    val step: TextUnit = 1.sp
) {
    init {
        check(min > 0.sp && max > 0.sp) {
            "min and max must be greater than 0.sp. min: $min, max: $max"
        }
        check(min <= max) {
            "max must be greater than or equal to min. min: $min, max: $max"
        }
        check(step > 0.sp) {
            "step must be greater than 0.sp. min: $min, max: $max"
        }
    }
}

private val DefaultFontSizeRange = FontSizeRange(8.sp, 16.sp)