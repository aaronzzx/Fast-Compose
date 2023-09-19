package com.aaron.compose.shape

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.InspectableValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * 支持缺口的圆角矩形
 *
 * @author aaronzzxup@gmail.com
 * @since 2023/9/19
 */
class RoundedNotchCornerShape(
    val topStart: CornerSize,
    val topEnd: CornerSize,
    val bottomEnd: CornerSize,
    val bottomStart: CornerSize
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline = Outline.Generic(
        path = createRoundedNotchCornerPath(
            size = size,
            density = density,
            layoutDirection = layoutDirection,
            topStart = topStart,
            topEnd = topEnd,
            bottomEnd = bottomEnd,
            bottomStart = bottomStart
        )
    )
}

/**
 * Creates [RoundedNotchCornerShape] with the same size applied for all four corners.
 * @param corner [CornerSize] to apply.
 */
fun RoundedNotchCornerShape(corner: CornerSize) =
    RoundedNotchCornerShape(corner, corner, corner, corner)

/**
 * Creates [RoundedNotchCornerShape] with the same size applied for all four corners.
 * @param size Size in [Dp] to apply.
 */
fun RoundedNotchCornerShape(size: Dp) = RoundedNotchCornerShape(CornerSize(size))

/**
 * Creates [RoundedNotchCornerShape] with the same size applied for all four corners.
 * @param size Size in pixels to apply.
 */
fun RoundedNotchCornerShape(size: Float) = RoundedNotchCornerShape(CornerSize(size))

/**
 * Creates [RoundedNotchCornerShape] with the same size applied for all four corners.
 * @param percent Size in percents to apply.
 */
fun RoundedNotchCornerShape(percent: Int) =
    RoundedNotchCornerShape(EnhancedPercentCornerSize(percent.toFloat()))

/**
 * Creates [RoundedNotchCornerShape] with sizes defined in [Dp].
 */
fun RoundedNotchCornerShape(
    topStart: Dp = 0.dp,
    topEnd: Dp = 0.dp,
    bottomEnd: Dp = 0.dp,
    bottomStart: Dp = 0.dp
) = RoundedNotchCornerShape(
    topStart = CornerSize(topStart),
    topEnd = CornerSize(topEnd),
    bottomEnd = CornerSize(bottomEnd),
    bottomStart = CornerSize(bottomStart)
)

/**
 * Creates [RoundedNotchCornerShape] with sizes defined in pixels.
 */
fun RoundedNotchCornerShape(
    topStart: Float = 0.0f,
    topEnd: Float = 0.0f,
    bottomEnd: Float = 0.0f,
    bottomStart: Float = 0.0f
) = RoundedNotchCornerShape(
    topStart = CornerSize(topStart),
    topEnd = CornerSize(topEnd),
    bottomEnd = CornerSize(bottomEnd),
    bottomStart = CornerSize(bottomStart)
)

/**
 * Creates [RoundedNotchCornerShape] with sizes defined in percents of the shape's smaller side.
 *
 * @param topStartPercent The top start corner radius as a percentage of the smaller side, with a
 * range of -100 - 100.
 * @param topEndPercent The top end corner radius as a percentage of the smaller side, with a
 * range of -100 - 100.
 * @param bottomEndPercent The bottom end corner radius as a percentage of the smaller side,
 * with a range of -100 - 100.
 * @param bottomStartPercent The bottom start corner radius as a percentage of the smaller side,
 * with a range of -100 - 100.
 */
fun RoundedNotchCornerShape(
    /*@IntRange(from = -100, to = 100)*/
    topStartPercent: Int = 0,
    /*@IntRange(from = -100, to = 100)*/
    topEndPercent: Int = 0,
    /*@IntRange(from = -100, to = 100)*/
    bottomEndPercent: Int = 0,
    /*@IntRange(from = -100, to = 100)*/
    bottomStartPercent: Int = 0
) = RoundedNotchCornerShape(
    topStart = EnhancedPercentCornerSize(topStartPercent.toFloat()),
    topEnd = EnhancedPercentCornerSize(topEndPercent.toFloat()),
    bottomEnd = EnhancedPercentCornerSize(bottomEndPercent.toFloat()),
    bottomStart = EnhancedPercentCornerSize(bottomStartPercent.toFloat())
)

private data class EnhancedPercentCornerSize(
    /*@FloatRange(from = -100.0, to = 100.0)*/
    private val percent: Float
) : CornerSize, InspectableValue {
    init {
        if (percent < -100 || percent > 100) {
            throw IllegalArgumentException("The percent should be in the range of [-100, 100]")
        }
    }

    override fun toPx(shapeSize: Size, density: Density) =
        shapeSize.minDimension * (percent / 100f)

    override fun toString(): String = "CornerSize(size = $percent%)"

    override val valueOverride: String
        get() = "$percent%"
}

fun createRoundedNotchCornerPath(
    size: Size,
    density: Density,
    layoutDirection: LayoutDirection,
    topStart: CornerSize,
    topEnd: CornerSize,
    bottomEnd: CornerSize,
    bottomStart: CornerSize
): Path = Path().apply {
    val topStartPx = topStart.toPx(size, density)
    val topEndPx = topEnd.toPx(size, density)
    val bottomEndPx = bottomEnd.toPx(size, density)
    val bottomStartPx = bottomStart.toPx(size, density)
    val topLeftPx = if (layoutDirection == LayoutDirection.Ltr) topStartPx else topEndPx
    val topRightPx = if (layoutDirection == LayoutDirection.Ltr) topEndPx else topStartPx
    val bottomRightPx = if (layoutDirection == LayoutDirection.Ltr) bottomEndPx else bottomStartPx
    val bottomLeftPx = if (layoutDirection == LayoutDirection.Ltr) bottomStartPx else bottomEndPx

    reset()
    // Top left arc
    if (topLeftPx > 0f) {
        arcTo(
            rect = Rect(
                left = 0f,
                top = 0f,
                right = topLeftPx * 2,
                bottom = topLeftPx * 2
            ),
            startAngleDegrees = -180.0f,
            sweepAngleDegrees = 90.0f,
            forceMoveTo = false
        )
    } else {
        arcTo(
            rect = Rect(
                left = topLeftPx,
                top = topLeftPx,
                right = -topLeftPx,
                bottom = -topLeftPx
            ),
            startAngleDegrees = 90.0f,
            sweepAngleDegrees = -90.0f,
            forceMoveTo = false
        )
    }
    // Top right arc
    if (topRightPx > 0f) {
        arcTo(
            rect = Rect(
                left = size.width - topRightPx * 2,
                top = 0f,
                right = size.width,
                bottom = topRightPx * 2
            ),
            startAngleDegrees = -90.0f,
            sweepAngleDegrees = 90.0f,
            forceMoveTo = false
        )
    } else {
        arcTo(
            rect = Rect(
                left = size.width + topRightPx,
                top = topRightPx,
                right = size.width - topRightPx,
                bottom = -topRightPx
            ),
            startAngleDegrees = -180.0f,
            sweepAngleDegrees = -90.0f,
            forceMoveTo = false
        )
    }
    // Bottom right arc
    if (bottomRightPx > 0f) {
        arcTo(
            rect = Rect(
                left = size.width - bottomRightPx * 2,
                top = size.height - bottomRightPx * 2,
                right = size.width,
                bottom = size.height
            ),
            startAngleDegrees = 0.0f,
            sweepAngleDegrees = 90.0f,
            forceMoveTo = false
        )
    } else {
        arcTo(
            rect = Rect(
                left = size.width + bottomRightPx,
                top = size.height + bottomRightPx,
                right = size.width - bottomRightPx,
                bottom = size.height - bottomRightPx
            ),
            startAngleDegrees = -90.0f,
            sweepAngleDegrees = -90.0f,
            forceMoveTo = false
        )
    }
    // Bottom left arc
    if (bottomLeftPx > 0f) {
        arcTo(
            rect = Rect(
                left = 0f,
                top = size.height - bottomLeftPx * 2,
                right = bottomLeftPx * 2,
                bottom = size.height
            ),
            startAngleDegrees = 90.0f,
            sweepAngleDegrees = 90.0f,
            forceMoveTo = false
        )
    } else {
        arcTo(
            rect = Rect(
                left = bottomLeftPx,
                top = size.height + bottomLeftPx,
                right = -bottomLeftPx,
                bottom = size.height - bottomLeftPx
            ),
            startAngleDegrees = 0.0f,
            sweepAngleDegrees = -90.0f,
            forceMoveTo = false
        )
    }
    close()
}

@Preview
@Composable
private fun RoundedNotchCornerShapePreview() {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val cornerRadius = 50.dp
    Box(
        modifier = Modifier
            .width(200.dp)
            .height(200.dp)
            .drawWithContent { 
                drawContent()
                val path = createRoundedNotchCornerPath(
                    size = size,
                    density = density,
                    layoutDirection = layoutDirection,
                    topStart = CornerSize(-cornerRadius),
                    topEnd = CornerSize(-cornerRadius),
                    bottomEnd = CornerSize(-cornerRadius),
                    bottomStart = CornerSize(-cornerRadius)
                )
                scale(0.5f) {
                    rotate(180f) {
                        drawPath(path = path, color = Color.Blue.copy(alpha = 0.5f))
                    }
                }
            }
            .background(
                color = Color.White,
                shape = RoundedNotchCornerShape(
                    topStart = cornerRadius,
                    topEnd = -cornerRadius,
                    bottomEnd = -cornerRadius,
                    bottomStart = cornerRadius
                )
            )
            .border(
                width = 10.dp,
                color = Color.Red.copy(alpha = 0.5f),
                shape = RoundedNotchCornerShape(
                    topStart = cornerRadius,
                    topEnd = -cornerRadius,
                    bottomEnd = -cornerRadius,
                    bottomStart = cornerRadius
                )
            )
    )
}