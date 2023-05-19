package com.aaron.compose.ktx

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.min

/**
 * 按蓝湖上的阴影参数来绘制阴影
 *
 * @param blurRadius 阴影模糊半径，模糊值增加，阴影会更加模糊
 * @param cornerRadius 阴影圆角半径
 * @param color 阴影颜色
 * @param offsetX x轴偏移值
 * @param offsetY y轴偏移值
 * @param spread 点差值，如果点差值增加，阴影的大小越大
 */
fun Modifier.shadow(
    blurRadius: Dp = 4.dp,
    cornerRadius: Dp = 0.dp,
    color: Color = Color.Black.copy(alpha = 0.16f),
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp,
    spread: Dp = 0.dp
) = if (blurRadius <= 0.dp) this else this.then(
    drawWithCache {
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        val cornerRadiusPixel = cornerRadius.toPx()
        val blurRadiusPixel = blurRadius.toPx()
        val spreadPixel = spread.toPx()
        val leftPixel = (0f - spreadPixel) + offsetX.toPx()
        val topPixel = (0f - spreadPixel) + offsetY.toPx()
        val rightPixel = (this.size.width + spreadPixel)
        val bottomPixel = (this.size.height + spreadPixel)
        val argbColor = color.toArgb()
        onDrawBehind {
            drawIntoCanvas {
                if (blurRadiusPixel != 0f) {
                    /*
                     * The feature maskFilter used below to apply the blur effect only works
                     * with hardware acceleration disabled.
                     */
                    frameworkPaint.maskFilter =
                        BlurMaskFilter(blurRadiusPixel, BlurMaskFilter.Blur.NORMAL)
                }

                frameworkPaint.color = argbColor
                it.drawRoundRect(
                    left = leftPixel,
                    top = topPixel,
                    right = rightPixel,
                    bottom = bottomPixel,
                    radiusX = cornerRadiusPixel,
                    radiusY = cornerRadiusPixel,
                    paint
                )
            }
        }
    }
)

/**
 * 综合点击，单击防抖
 *
 * @param enabled 是否启用点击
 * @param enableRipple 是否支持水波纹效果
 * @param rippleColor 水波纹颜色
 * @param rippleBounded 如果为真，波纹会被目标布局的边界截断。无界波纹总是从目标布局中心开始动画，有界波纹总是从触摸位置开始动画
 * @param rippleRadius 波纹的半径。如果设置 [Dp.Unspecified] 则大小将根据目标布局大小计算。
 * @param onSingleClick 点击回调
 */
fun Modifier.onCombinedSingleClick(
    enabled: Boolean = true,
    enableRipple: Boolean = true,
    singleClickIntervalMs: Long = 800,
    rippleColor: Color = Color.Unspecified,
    rippleBounded: Boolean = true,
    rippleRadius: Dp = Dp.Unspecified,
    interactionSource: MutableInteractionSource? = null,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    onSingleClick: () -> Unit
) = composed {
    combinedClickable(
        enabled = enabled,
        interactionSource = interactionSource ?: remember { MutableInteractionSource() },
        indication = if (!enableRipple) null else {
            rememberRipple(
                rippleBounded,
                rippleRadius,
                rippleColor
            )
        },
        onLongClick = onLongClick,
        onDoubleClick = onDoubleClick,
        onClick = run {
            var clickTime by remember {
                mutableStateOf(0L)
            }
            val block = {
                onClick?.invoke()
                val curTime = System.currentTimeMillis()
                if (curTime - clickTime > singleClickIntervalMs) {
                    clickTime = curTime
                    onSingleClick()
                }
            }
            block
        }
    )
}

/**
 * 带水波纹点击事件
 *
 * @param enabled 是否启用点击
 * @param enableRipple 是否支持水波纹效果
 * @param rippleColor 水波纹颜色
 * @param rippleBounded 如果为真，波纹会被目标布局的边界截断。无界波纹总是从目标布局中心开始动画，有界波纹总是从触摸位置开始动画
 * @param rippleRadius 波纹的半径。如果设置 [Dp.Unspecified] 则大小将根据目标布局大小计算。
 * @param onClick 点击回调
 */
fun Modifier.onClick(
    enabled: Boolean = true,
    enableRipple: Boolean = true,
    rippleColor: Color = Color.Unspecified,
    rippleBounded: Boolean = true,
    rippleRadius: Dp = Dp.Unspecified,
    interactionSource: MutableInteractionSource? = null,
    onClick: () -> Unit
) = composed {
    clickable(
        enabled = enabled,
        interactionSource = interactionSource ?: remember { MutableInteractionSource() },
        indication = if (!enableRipple) null else {
            rememberRipple(
                rippleBounded,
                rippleRadius,
                rippleColor
            )
        },
        onClick = onClick
    )
}

/**
 * 带水波纹点击事件
 *
 * @param enabled 是否启用点击
 * @param enableRipple 是否支持水波纹效果
 * @param clickIntervalMs 点击防抖间隔
 * @param rippleColor 水波纹颜色
 * @param rippleBounded 如果为真，波纹会被目标布局的边界截断。无界波纹总是从目标布局中心开始动画，有界波纹总是从触摸位置开始动画
 * @param rippleRadius 波纹的半径。如果设置 [Dp.Unspecified] 则大小将根据目标布局大小计算。
 * @param onClick 点击回调
 */
fun Modifier.onSingleClick(
    enabled: Boolean = true,
    enableRipple: Boolean = true,
    clickIntervalMs: Long = 800,
    rippleColor: Color = Color.Unspecified,
    rippleBounded: Boolean = true,
    rippleRadius: Dp = Dp.Unspecified,
    interactionSource: MutableInteractionSource? = null,
    onClick: () -> Unit
) = composed {
    clickable(
        enabled = enabled,
        interactionSource = interactionSource ?: remember { MutableInteractionSource() },
        indication = if (!enableRipple) null else {
            rememberRipple(
                rippleBounded,
                rippleRadius,
                rippleColor
            )
        },
        onClick = run {
            var clickTime by remember {
                mutableStateOf(0L)
            }
            val block = {
                val curTime = System.currentTimeMillis()
                if (curTime - clickTime > clickIntervalMs) {
                    clickTime = curTime
                    onClick()
                }
            }
            block
        }
    )
}

/**
 * Draws [shape] with a solid [color] behind the content.
 *
 * @sample androidx.compose.foundation.samples.DrawBackgroundColor
 *
 * @param color color to paint background with
 * @param shape desired shape of the background
 */
fun Modifier.clipToBackground(
    color: Color,
    shape: Shape = RectangleShape
) = this
    .background(color, shape)
    .clip(shape)

/**
 * Draws [shape] with [brush] behind the content.
 *
 * @sample androidx.compose.foundation.samples.DrawBackgroundShapedBrush
 *
 * @param brush brush to paint background with
 * @param shape desired shape of the background
 * @param alpha Opacity to be applied to the [brush], with `0` being completely transparent and
 * `1` being completely opaque. The value must be between `0` and `1`.
 */
fun Modifier.clipToBackground(
    brush: Brush,
    shape: Shape = RectangleShape,
    /*@FloatRange(from = 0.0, to = 1.0)*/
    alpha: Float = 1.0f
) = this
    .background(brush, shape, alpha)
    .clip(shape)

/**
 * Modify element to add border with appearance specified with a [border] and a [shape] and clip it.
 *
 * @sample androidx.compose.foundation.samples.BorderSample()
 *
 * @param border [BorderStroke] class that specifies border appearance, such as size and color
 * @param shape shape of the border
 */
fun Modifier.clipToBorder(border: BorderStroke, shape: Shape = RectangleShape) =
    border(width = border.width, brush = border.brush, shape = shape).clip(shape)

/**
 * Modify element to add border with appearance specified with a [width], a [color] and a [shape]
 * and clip it.
 *
 * @sample androidx.compose.foundation.samples.BorderSampleWithDataClass()
 *
 * @param width width of the border. Use [Dp.Hairline] for a hairline border.
 * @param color color to paint the border with
 * @param shape shape of the border
 */
fun Modifier.clipToBorder(width: Dp, color: Color, shape: Shape = RectangleShape) =
    border(width, SolidColor(color), shape).clip(shape)

/**
 * A [Modifier] that draws a border around elements that are recomposing. The border increases in
 * size and interpolates from red to green as more recompositions occur before a timeout.
 */
@Stable
fun Modifier.recomposeHighlighter(): Modifier = this.then(recomposeModifier)

// Use a single instance + @Stable to ensure that recompositions can enable skipping optimizations
// Modifier.composed will still remember unique data per call site.
private val recomposeModifier =
    Modifier.composed(inspectorInfo = debugInspectorInfo { name = "recomposeHighlighter" }) {
        // The total number of compositions that have occurred. We're not using a State<> here be
        // able to read/write the value without invalidating (which would cause infinite
        // recomposition).
        val totalCompositions = remember { arrayOf(0L) }
        totalCompositions[0]++

        // The value of totalCompositions at the last timeout.
        val totalCompositionsAtLastTimeout = remember { mutableStateOf(0L) }

        // Start the timeout, and reset everytime there's a recomposition. (Using totalCompositions
        // as the key is really just to cause the timer to restart every composition).
        LaunchedEffect(totalCompositions[0]) {
            delay(3000)
            totalCompositionsAtLastTimeout.value = totalCompositions[0]
        }

        Modifier.drawWithCache {
            onDrawWithContent {
                // Draw actual content.
                drawContent()

                // Below is to draw the highlight, if necessary. A lot of the logic is copied from
                // Modifier.border
                val numCompositionsSinceTimeout =
                    totalCompositions[0] - totalCompositionsAtLastTimeout.value

                val hasValidBorderParams = size.minDimension > 0f
                if (!hasValidBorderParams || numCompositionsSinceTimeout <= 0) {
                    return@onDrawWithContent
                }

                val (color, strokeWidthPx) =
                    when (numCompositionsSinceTimeout) {
                        // We need at least one composition to draw, so draw the smallest border
                        // color in blue.
                        1L -> Color.Blue to 1f
                        // 2 compositions is _probably_ okay.
                        2L -> Color.Green to 2.dp.toPx()
                        // 3 or more compositions before timeout may indicate an issue. lerp the
                        // color from yellow to red, and continually increase the border size.
                        else -> {
                            lerp(
                                Color.Yellow.copy(alpha = 0.8f),
                                Color.Red.copy(alpha = 0.5f),
                                min(1f, (numCompositionsSinceTimeout - 1).toFloat() / 100f)
                            ) to numCompositionsSinceTimeout.toInt().dp.toPx()
                        }
                    }

                val halfStroke = strokeWidthPx / 2
                val topLeft = Offset(halfStroke, halfStroke)
                val borderSize = Size(size.width - strokeWidthPx, size.height - strokeWidthPx)

                val fillArea = (strokeWidthPx * 2) > size.minDimension
                val rectTopLeft = if (fillArea) Offset.Zero else topLeft
                val size = if (fillArea) size else borderSize
                val style = if (fillArea) Fill else Stroke(strokeWidthPx)

                drawRect(
                    brush = SolidColor(color),
                    topLeft = rectTopLeft,
                    size = size,
                    style = style
                )
            }
        }
    }