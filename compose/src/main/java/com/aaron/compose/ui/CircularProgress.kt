package com.aaron.compose.ui

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProgressIndicatorDefaults.IndicatorBackgroundOpacity
import androidx.compose.material.ProgressIndicatorDefaults.StrokeWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aaron.compose.ktx.toPx
import kotlin.math.min

/**
 * 圆形进度条
 *
 * @param modifier 修饰符
 * @param strokeWidth 环的宽度
 * @param strokeCap 进度条的头形状
 * @param backgroundColor 环内部的圆背景色
 * @param activeColor 进度条颜色
 * @param staticColor 进度条底色
 * @param startAngle 从什么角度开始，从 12 点钟角度开始
 * @param valueRange 范围
 * @param smoothProgress 丝滑过渡
 * @param smoothProgressSpec 丝滑过渡的 AnimationSpec
 * @param progress 当前进度
 */
@Composable
fun CircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = StrokeWidth,
    strokeCap: StrokeCap = StrokeCap.Butt,
    backgroundColor: Color = Color.Unspecified,
    activeColor: Color = MaterialTheme.colors.primary,
    staticColor: Color = activeColor.copy(alpha = IndicatorBackgroundOpacity),
    startAngle: Float = 270f,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    smoothProgress: Boolean = true,
    smoothProgressSpec: AnimationSpec<Float> = tween(),
) {
    require(progress in valueRange) {
        "Invalid progress: $progress"
    }

    val strokeWidthPx = strokeWidth.toPx()
    val stroke = Stroke(width = strokeWidthPx, cap = strokeCap)
    val num = progress / valueRange.endInclusive * 360
    val sweepAngle = when (smoothProgress) {
        true -> animateFloatAsState(num, smoothProgressSpec).value
        else -> num
    }

    Canvas(modifier = modifier.size(48.dp)) {
        val canvasSize = min(size.width, size.height)
        if (backgroundColor != Color.Unspecified) {
            drawCircle(
                color = backgroundColor,
                radius = (canvasSize / 2) - (strokeWidthPx / 2)
            )
        }
        drawCircle(
            color = staticColor,
            radius = canvasSize / 2,
            style = stroke
        )
        drawArc(
            color = activeColor,
            style = stroke,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false
        )
    }
}
