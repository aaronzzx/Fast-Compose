package com.aaron.compose.ui

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.packInts
import androidx.compose.ui.util.unpackInt1
import androidx.compose.ui.util.unpackInt2
import com.aaron.compose.ktx.onClick
import com.aaron.compose.ktx.toSp
import com.aaron.compose.ui.RollPickerDefaults.AbsMaxFlingVelocity
import com.aaron.compose.ui.RollPickerDefaults.LineSpacingMultiplier
import com.aaron.compose.ui.RollPickerDefaults.Loop
import com.aaron.compose.ui.RollPickerDefaults.Style
import com.aaron.compose.ui.RollPickerDefaults.VisibleCount
import com.aaron.compose.ui.RollPickerDefaults.getDefaultSize
import com.aaron.compose.ui.RollPickerStyle.Flat
import com.aaron.compose.ui.RollPickerStyle.Wheel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * @author aaronzzxup@gmail.com
 * @since 2023/6/17
 */

@Composable
fun VerticalRollPicker(
    count: Int,
    onPick: (index: RollPickerIndex) -> Unit,
    modifier: Modifier = Modifier,
    state: RollPickerState = rememberRollPickerState(),
    style: RollPickerStyle = Style,
    visibleCount: Int = VisibleCount,
    lineSpacingMultiplier: Float = LineSpacingMultiplier,
    loop: Boolean = Loop,
    onItemClick: ((index: RollPickerIndex) -> Unit)? = null,
    itemContent: @Composable (index: RollPickerIndex) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .size(getDefaultSize(style, visibleCount))
            .flingForRollPicker(state)
            .snapToCurrent(state)
    ) {
        if (maxWidth <= 0.dp || maxHeight <= 0.dp) return@BoxWithConstraints

        RollPicker(
            count = count,
            onPick = onPick,
            orientation = Orientation.Vertical,
            diameter = maxHeight,
            state = state,
            style = style,
            visibleCount = visibleCount,
            lineSpacingMultiplier = lineSpacingMultiplier,
            loop = loop,
            onItemClick = onItemClick,
            itemContent = itemContent
        )
    }
}

@Composable
fun HorizontalRollPicker(
    count: Int,
    onPick: (index: RollPickerIndex) -> Unit,
    modifier: Modifier = Modifier,
    state: RollPickerState = rememberRollPickerState(),
    style: RollPickerStyle = Style,
    visibleCount: Int = VisibleCount,
    lineSpacingMultiplier: Float = LineSpacingMultiplier,
    loop: Boolean = Loop,
    onItemClick: ((index: RollPickerIndex) -> Unit)? = null,
    itemContent: @Composable (index: RollPickerIndex) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .size(getDefaultSize(style, visibleCount))
            .flingForRollPicker(state)
            .snapToCurrent(state)
    ) {
        if (maxWidth <= 0.dp || maxHeight <= 0.dp) return@BoxWithConstraints

        RollPicker(
            count = count,
            onPick = onPick,
            orientation = Orientation.Horizontal,
            diameter = maxWidth,
            state = state,
            style = style,
            visibleCount = visibleCount,
            lineSpacingMultiplier = lineSpacingMultiplier,
            loop = loop,
            onItemClick = onItemClick,
            itemContent = itemContent
        )
    }
}

@Composable
private fun RollPicker(
    count: Int,
    onPick: (index: RollPickerIndex) -> Unit,
    orientation: Orientation,
    diameter: Dp,
    state: RollPickerState,
    style: RollPickerStyle,
    visibleCount: Int,
    lineSpacingMultiplier: Float,
    loop: Boolean,
    onItemClick: ((index: RollPickerIndex) -> Unit)?,
    itemContent: @Composable (index: RollPickerIndex) -> Unit
) {
    if (count <= 0) return

    check(visibleCount > 0 && visibleCount and 1 != 0) {
        "visibleCount must be greater than 0 and must be odd number"
    }

    val actualVisibleCount = when (style) {
        is Wheel -> visibleCount + 2
        is Flat -> if (style.halfExposed) visibleCount + 1 else visibleCount
    }
    val itemSizeAnchor = when (style) {
        is Wheel -> diameter / (actualVisibleCount - 1)
        is Flat -> diameter / actualVisibleCount
    }
    val itemSize = when (style) {
        is Wheel -> itemSizeAnchor * 1.5f / lineSpacingMultiplier
        is Flat -> itemSizeAnchor / lineSpacingMultiplier
    }
    val padding = (diameter - itemSizeAnchor) / 2

    val virtualCount = if (loop) Int.MAX_VALUE else count
    val startIndexOffset = if (loop) virtualCount / 2 else 0
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = startIndexOffset + state.initialIndex
    )
    state.also {
        it.listState = listState
        it.loop = loop
        it.startIndexOffset = startIndexOffset
        it.visibleCount = visibleCount
        it.count = count
        it.diameter = diameter
        it.itemSizeAnchor = itemSizeAnchor
        it.itemSize = itemSize
        it.itemFontSize = itemSize.toSp() * 0.677f
    }

    val curOnPick by rememberUpdatedState(newValue = onPick)
    LaunchedEffect(key1 = state) {
        snapshotFlow { state.isScrollInProgress }
            .drop(1)
            .filter { !it && state.currentIndexOffsetFraction == 0f }
            .map { state.currentIndex }
            .distinctUntilChanged()
            .collect { index ->
                state.settledIndex = index
                curOnPick(index)
            }
    }

    val coroutineScope = rememberCoroutineScope()
    val internalOnItemClick: (Int) -> Unit = { index ->
        val rollPickerIndex = RollPickerIndex(state.mapIndex(index), index)
        onItemClick?.invoke(rollPickerIndex)
            ?: coroutineScope.launch {
                state.animateScrollToIndex(rollPickerIndex)
            }
    }

    val pickerBox: @Composable (index: Int) -> Unit = { index ->
        val rollPickerIndex = RollPickerIndex(state.mapIndex(index), index)
        // 第一个 Box 固定位置，确保 snap 正常
        Box(
            modifier = Modifier
                .run {
                    if (orientation == Orientation.Vertical) {
                        this
                            .fillMaxWidth()
                            .height(itemSizeAnchor)
                    } else {
                        this
                            .fillMaxHeight()
                            .width(itemSizeAnchor)
                    }
                }
                .run {
                    if (style !is Wheel) this else {
                        wheelTransformation(
                            state = state,
                            index = rollPickerIndex,
                            curvature = style.curvature
                        )
                    }
                }
                .onClick(enableRipple = false) {
                    internalOnItemClick(index)
                },
            contentAlignment = Alignment.Center
        ) {
            // 第二个 Box 实现 lineSpacingMultiplier 效果
            Box(
                modifier = Modifier
                    .run {
                        if (orientation == Orientation.Vertical) {
                            this
                                .fillMaxWidth()
                                .requiredHeight(itemSize)
                        } else {
                            this
                                .fillMaxHeight()
                                .requiredWidth(itemSize)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                itemContent(rollPickerIndex)
            }
        }
    }

    if (orientation == Orientation.Vertical) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(vertical = padding)
        ) {
            items(virtualCount) { index ->
                pickerBox(index)
            }
        }
    } else {
        LazyRow(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(horizontal = padding)
        ) {
            items(virtualCount) { index ->
                pickerBox(index)
            }
        }
    }
}

fun Modifier.clipCenterForRollPicker(
    state: RollPickerState,
    color: Color,
    scaleX: Float = 1.0f,
    scaleY: Float = 1.0f,
    clipFraction: Float = 1.0f
) = run {
    graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        .drawWithCache {
            val path = Path().apply {
                val itemSizePx = state.itemSize.toPx()
                val halfItemSizePx = itemSizePx / 2f
                val fraction = 1f - clipFraction
                if (state.orientation == Orientation.Vertical) {
                    val top = (size.height - itemSizePx) / 2f
                    addRect(
                        Rect(
                            left = 0f,
                            top = top + (halfItemSizePx * fraction),
                            right = size.width,
                            bottom = (top + itemSizePx) - (halfItemSizePx * fraction)
                        )
                    )
                } else {
                    val left = (size.width - itemSizePx) / 2f
                    addRect(
                        Rect(
                            left = left + (halfItemSizePx * fraction),
                            top = 0f,
                            right = (left + itemSizePx) - (halfItemSizePx * fraction),
                            bottom = size.height
                        )
                    )
                }
            }
            onDrawWithContent {
                clipPath(path, clipOp = ClipOp.Difference) {
                    this@onDrawWithContent.drawContent()
                }
                clipPath(path) {
                    scale(scaleX = scaleX, scaleY = scaleY) {
                        this@onDrawWithContent.drawContent()
                        if (color.isSpecified) {
                            drawRect(
                                color = color,
                                blendMode = BlendMode.SrcIn
                            )
                        }
                    }
                }
            }
        }
}

fun Modifier.flingForRollPicker(
    state: RollPickerState,
    absMaxVelocity: Float = AbsMaxFlingVelocity
) = composed {
    val velocityRange = -absMaxVelocity..absMaxVelocity
    val coroutineScope = rememberCoroutineScope()
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val nestedScrollConnection = object : NestedScrollConnection {
        override suspend fun onPreFling(available: Velocity): Velocity {
            val limitedVelocity = Velocity(
                x = available.x.coerceIn(velocityRange),
                y = available.y.coerceIn(velocityRange)
            )
            var lastValue = 0f
            AnimationState(
                initialValue = 0f,
                initialVelocity = when (state.orientation) {
                    Orientation.Vertical -> limitedVelocity.y
                    else -> limitedVelocity.x
                }
            ).animateDecay(animationSpec = decayAnimationSpec) {
                coroutineScope.launch {
                    val delta = value - lastValue
                    state.scrollBy(-delta)
                    lastValue = value
                }
            }
            return available
        }
    }
    nestedScroll(nestedScrollConnection)
}

fun Modifier.slowInFlingForRollPicker(
    state: RollPickerState,
    absMaxVelocity: Float = AbsMaxFlingVelocity,
    maxFlingCount: Int = Int.MAX_VALUE,
    avgItemDurationMillis: Int = 300,
    durationMillisRange: IntRange = 300..3000
) = composed {
    val velocityRange = -absMaxVelocity..absMaxVelocity
    val density = LocalDensity.current
    val splineBasedDecay = rememberSplineBasedDecay<Float>()
    val nestedScrollConnection = object : NestedScrollConnection {
        override suspend fun onPreFling(available: Velocity): Velocity {
            val limitedVelocity = Velocity(
                x = available.x.coerceIn(velocityRange),
                y = available.y.coerceIn(velocityRange)
            )
            val converter = Float.VectorConverter
            val initialValue = converter.convertToVector(0f)
            val initialVelocity = when (state.orientation) {
                Orientation.Vertical -> converter.convertToVector(limitedVelocity.y)
                else -> converter.convertToVector(limitedVelocity.x)
            }
            val targetValue = splineBasedDecay.vectorize(converter).getTargetValue(
                initialValue = initialValue,
                initialVelocity = initialVelocity
            )
            val indexCount = targetValue.value / density.run { state.itemSizeAnchor.toPx() }
            val limitedIndexCount = indexCount.toInt().coerceIn(-maxFlingCount, maxFlingCount)
            state.animateScrollToIndex(
                index = state.currentIndex - limitedIndexCount,
                animationSpec = tween(
                    durationMillis = (avgItemDurationMillis * limitedIndexCount.absoluteValue).coerceIn(durationMillisRange),
                    easing = LinearOutSlowInEasing
                )
            )
            return available
        }
    }
    nestedScroll(nestedScrollConnection)
}

private fun Modifier.snapToCurrent(state: RollPickerState) = run {
    val nestedScrollConnection = object : NestedScrollConnection {
        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            if (state.isScrollInProgress.not() && state.currentIndexOffsetFraction != 0f) {
                state.snapToCurrentIndex()
            }
            return super.onPostFling(consumed, available)
        }
    }
    nestedScroll(nestedScrollConnection)
}

private fun Modifier.wheelTransformation(
    state: RollPickerState,
    index: RollPickerIndex,
    curvature: Float
) = graphicsLayer {
    val orientation = state.orientation
    val diameter = state.diameter.toPx()
    val radius = diameter / 2f
    val visibleCount = state.visibleCount
    val itemSizeAnchor = state.itemSizeAnchor.toPx()

    val offsetFraction = state.calculateOffsetFraction(index)
    val actualVisibleCount = visibleCount + 2
    val actualSideVisibleCount = (actualVisibleCount - 1) / 2
    val degree = (offsetFraction * 90f / actualSideVisibleCount).coerceIn(-90f, 90f)
    val radian = Math
        .toRadians(degree.toDouble())
        .toFloat()

//    val scaleFactor = 1f - (degree.absoluteValue / 90f)
    val scaleFactor = cos(radian)

    val arcLength = sin(radian) * radius

    // 所有子项偏移到原点（选中区域）
    val initialTrans = itemSizeAnchor * offsetFraction
    val transOffset = initialTrans - arcLength

    if (orientation == Orientation.Vertical) {
        scaleY = scaleFactor
        translationY = transOffset
    } else {
        scaleX = scaleFactor
        translationX = transOffset
    }
}.drawWithContent {
    val orientation = state.orientation
    val itemSizeAnchor = state.itemSizeAnchor.toPx()
    val offsetFraction = state.calculateOffsetFraction(index)

    val degree = (45f * curvature * -offsetFraction).coerceIn(-45f, 45f)
    val radian = Math.toRadians(degree.toDouble()).toFloat()
    val initialTrans = itemSizeAnchor * sin(radian)
    val skew = tan(radian)
    var transOffset = initialTrans * -offsetFraction
    if (offsetFraction > 0f) {
        // 因为错切原点一样，如果选中区上方不减少偏移就和下方对应不上
        transOffset -= initialTrans
    }

    if (orientation == Orientation.Vertical) {
        translate(left = transOffset) {
            drawContext.canvas.skew(sx = skew, sy = 0f)
            this@drawWithContent.drawContent()
        }
    } else {
        translate(top = transOffset) {
            drawContext.canvas.skew(sx = 0f, sy = skew)
            this@drawWithContent.drawContent()
        }
    }
}

@Composable
fun rememberRollPickerState(initialIndex: Int = 0): RollPickerState {
    return remember {
        RollPickerState(initialIndex)
    }
}

@Stable
class RollPickerState(val initialIndex: Int = 0) {

    internal var listState: LazyListState? by mutableStateOf(null)

    internal var loop: Boolean by mutableStateOf(false)

    internal var visibleCount: Int by mutableStateOf(0)

    internal var count: Int by mutableStateOf(0)

    internal var startIndexOffset: Int by mutableStateOf(0)

    /**
     * 当前索引
     */
    val currentIndex: RollPickerIndex by derivedStateOf {
        val foundIndex = listState
            ?.layoutInfo
            ?.visibleItemsInfo
            ?.fastFirstOrNull {
                it.offset.absoluteValue <= it.size / 2f
            }
            ?.index
            ?: -1
        RollPickerIndex(mapIndex(foundIndex), foundIndex)
    }

    var settledIndex: RollPickerIndex by mutableStateOf(currentIndex)
        internal set

    /**
     * 当前索引偏移值
     */
    val currentIndexOffsetFraction: Float by derivedStateOf {
        val curItem = listState
            ?.layoutInfo
            ?.visibleItemsInfo
            ?.fastFirstOrNull {
                it.index == currentIndex.actualValue
            }
            ?: return@derivedStateOf 0f
        -curItem.offset / curItem.size.toFloat()
    }

    /**
     * 是否在滚动中
     */
    val isScrollInProgress: Boolean by derivedStateOf {
        listState?.isScrollInProgress ?: false
    }

    /**
     * 能否向前滚动，结束方向
     */
    val canScrollForward: Boolean
        get() = listState?.canScrollForward ?: false

    /**
     * 能否向后滚动，开始方向
     */
    val canScrollBackward: Boolean
        get() = listState?.canScrollBackward ?: false

    /**
     * 滚轮方向
     */
    val orientation: Orientation by derivedStateOf {
        listState?.layoutInfo?.orientation ?: Orientation.Vertical
    }

    /**
     * 直径，[VerticalRollPicker] 为高度，[HorizontalRollPicker] 为宽度
     */
    var diameter: Dp by mutableStateOf(0.dp)
        internal set

    /**
     * 锚点子项大小，用来固定子项位置，例如滚轮的形变基于这个大小
     */
    var itemSizeAnchor: Dp by mutableStateOf(0.dp)
        internal set

    /**
     * 子项大小
     */
    var itemSize: Dp by mutableStateOf(0.dp)
        internal set

    /**
     * 子项字体大小
     */
    var itemFontSize: TextUnit by mutableStateOf(16.sp)
        internal set

    /**
     * 计算给定索引偏移值
     */
    fun calculateOffsetFraction(index: RollPickerIndex): Float {
        if (loop) {
            var index2 = index
            if (index2.actualValue == -1) {
                index2 = suitableIndexToCurrent(index2)
            }
            return (currentIndex.actualValue - index2.actualValue) + currentIndexOffsetFraction
        }
        return (currentIndex.value - index.value) + currentIndexOffsetFraction
    }

    /**
     * 按像素滚动
     */
    suspend fun scrollBy(value: Float) {
        listState?.scrollBy(value)
    }

    /**
     * 立即滚动到给定索引
     */
    suspend fun scrollToIndex(index: RollPickerIndex) {
        animateScrollToIndex(
            index = suitableIndexToCurrent(index),
            animationSpec = snap()
        )
    }

    /**
     * 动画形式滚动到给定索引
     */
    suspend fun animateScrollToIndex(
        index: RollPickerIndex,
        animationSpec: AnimationSpec<Float> = spring(
            stiffness = Spring.StiffnessMediumLow
        )
    ) {
        val listState = listState ?: return

        val size = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: return
        val fraction = calculateOffsetFraction(suitableIndexToCurrent(index))
        val scrollOffset = size * fraction
        listState.animateScrollBy(
            value = -scrollOffset,
            animationSpec = animationSpec
        )
    }

    internal suspend fun snapToCurrentIndex() {
        animateScrollToIndex(currentIndex)
    }

    internal fun suitableIndexToCurrent(index: RollPickerIndex): RollPickerIndex {
        if (!loop || index.actualValue != -1) return index

        val currentIndex = currentIndex
        val count = count

        val curIndex = currentIndex.value
        val curIndexReversed = curIndex - count
        val value = index.value
        val reversedValue = value - count

        // count = 4, curIndex = 2, curIndexReversed = -2, value = 0, reversedValue = -4
        // offset1 = -2, offset2 = -6, offset3 = 2, offset4 = -2
        // count = 9, curIndex = 4, curIndexReversed = -5, value = 0, reversedValue = -9
        // offset1 = -4, offset2 = -13, offset3 = 5, offset4 = -4
        val offset1 = value - curIndex
        val offset2 = reversedValue - curIndex
        val offset3 = value - curIndexReversed
        val offset4 = reversedValue - curIndexReversed

        val offset = arrayOf(
            offset1 to offset1.absoluteValue,
            offset2 to offset2.absoluteValue,
            offset3 to offset3.absoluteValue,
            offset4 to offset4.absoluteValue
        ).minByOrNull { it.second }!!.first

        return currentIndex + offset
    }

    /**
     * 转换索引，在开启循环时真实索引过大，需要进行转换才能对应数据
     */
    internal fun mapIndex(index: Int): Int {
        if (!loop) return index
        return floorMod(index - startIndexOffset, count)
    }

    private fun floorMod(value: Int, other: Int): Int = when (other) {
        0 -> value
        else -> value - value.floorDiv(other) * other
    }
}

@Stable
sealed class RollPickerStyle {

    data class Wheel(val curvature: Float = 0f) : RollPickerStyle()

    data class Flat(val halfExposed: Boolean = false) : RollPickerStyle()
}

fun RollPickerIndex(value: Int) = RollPickerIndex(value, -1)

private fun RollPickerIndex(index: Int, actualIndex: Int) = RollPickerIndex(packInts(index, actualIndex))

@JvmInline
value class RollPickerIndex internal constructor(private val packedValue: Long) {

    val value: Int get() = unpackInt1(packedValue)

    /**
     * 真实索引，可能为 -1 ，如果是外部创建的索引
     */
    internal val actualValue: Int get() = unpackInt2(packedValue)

    operator fun plus(offset: Int): RollPickerIndex {
        return RollPickerIndex(
            index = value + offset,
            actualIndex = if (actualValue == -1) -1 else actualValue + offset
        )
    }

    operator fun minus(offset: Int): RollPickerIndex {
        return RollPickerIndex(
            index = value - offset,
            actualIndex = if (actualValue == -1) -1 else actualValue - offset
        )
    }

    override fun toString(): String {
        return "RollPickerIndex(value=$value)"
    }
}

object RollPickerDefaults {

    const val VisibleCount = 5
    const val LineSpacingMultiplier = 1f
    const val Loop = false
    const val AbsMaxFlingVelocity = 6000f
    val Style = Wheel()

    private val ItemSize = 25.dp

    internal fun getDefaultSize(style: RollPickerStyle, visibleCount: Int): Dp {
        val defaultItemSize = ItemSize
        if (style is Wheel) {
            return defaultItemSize * (visibleCount + 1)
        } else if (style is Flat && style.halfExposed) {
            return defaultItemSize * (visibleCount + 1)
        }
        return defaultItemSize * visibleCount
    }
}