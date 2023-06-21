package com.aaron.compose.ktx

import androidx.compose.ui.text.TextLayoutResult

/**
 * 获取字体大小像素
 */
val TextLayoutResult.fontSizePx: Float get() {
    val style = layoutInput.style
    val density = layoutInput.density
    return density.run { style.fontSize.toPx() }
}

/**
 * 获取给定行文本度量相关信息
 */
fun TextLayoutResult.getTextMetrics(lineIndex: Int): TextMetrics {
    val top = getTop(lineIndex)
    val ascent = getAscent(lineIndex)
    val baseline = getBaseline(lineIndex)
    val descent = getDescent(lineIndex)
    val bottom = getBottom(lineIndex)
    return TextMetrics(top, ascent, baseline, descent, bottom)
}

/**
 * 获取给定行文本居中所需偏移值
 */
fun TextLayoutResult.getCenterOffset(lineIndex: Int): Float {
    val top = getTop(lineIndex)
    val ascent = getAscent(lineIndex)
    val baseline = getBaseline(lineIndex)
    val descent = getDescent(lineIndex)
    val bottom = getBottom(lineIndex)
    return ((bottom - baseline) - (ascent - top)) / 2
}

/**
 * 获取给定行 Baseline 位置
 */
fun TextLayoutResult.getBaseline(lineIndex: Int): Float {
    if (lineIndex == 0) {
        return firstBaseline
    } else if (lineIndex == lineCount - 1) {
        return lastBaseline
    }
    val top = getLineTop(lineIndex)
    val bottom = getLineBottom(lineIndex)
    val lineHeight = bottom - top
    return firstBaseline + lineHeight * lineIndex
}

/**
 * 获取给定行 Top 位置
 */
fun TextLayoutResult.getTop(lineIndex: Int): Float {
    return getBaseline(lineIndex) - fontSizePx * TopFactor
}

/**
 * 获取给定行 Bottom 位置
 */
fun TextLayoutResult.getBottom(lineIndex: Int): Float {
    return getBaseline(lineIndex) - fontSizePx * BottomFactor
}

/**
 * 获取给定行 Ascent 位置
 */
fun TextLayoutResult.getAscent(lineIndex: Int): Float {
    return getBaseline(lineIndex) - fontSizePx * AscentFactor
}

/**
 * 获取给定行 Descent 位置
 */
fun TextLayoutResult.getDescent(lineIndex: Int): Float {
    return getBaseline(lineIndex) - fontSizePx * DescentFactor
}

/**
 * 文本度量相关信息类
 */
data class TextMetrics(
    val top: Float,
    val ascent: Float,
    val baseline: Float,
    val descent: Float,
    val bottom: Float
)

private const val TopFactor = 1.06f
private const val AscentFactor = 0.76f
private const val DescentFactor = -0.18f
private const val BottomFactor = -0.24f
