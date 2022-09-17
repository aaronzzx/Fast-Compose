package com.aaron.compose.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.aaron.compose.ktx.toPx
import com.aaron.compose.ui.SmartRefreshType.FinishRefresh
import com.aaron.compose.ui.SmartRefreshType.Idle
import com.aaron.compose.ui.SmartRefreshType.Refreshing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

/**
 * 刷新阻尼
 */
private const val DragMultiplier = 0.5f

/**
 * 刷新状态
 */
@Stable
sealed class SmartRefreshType {

    class Idle : SmartRefreshType() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }
    class Refreshing : SmartRefreshType() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }

    /**
     * 结束刷新状态， [dismissDelayMillis] 表示要悬挂多久
     */
    sealed class FinishRefresh(val dismissDelayMillis: Long) : SmartRefreshType()
    class Success(dismissDelayMillis: Long = 300) : FinishRefresh(dismissDelayMillis)
    class Failure(dismissDelayMillis: Long = 300) : FinishRefresh(dismissDelayMillis)
}

/**
 * 维护刷新期间各种状态，使用时应该将之维护在 [androidx.lifecycle.ViewModel] 中，
 * 因为 Compose 生命周期缘故，可能会频繁进入、退出 Composition ，导致状态被反复创建，
 * 例如在 [com.google.accompanist.pager.HorizontalPager] 中使用时，Pager 基于
 * [androidx.compose.foundation.lazy.LazyList] 构造而来，在切页时会出现回收的情况，
 * 因此状态将变得不明确。
 *
 * @param type 初始刷新状态
 */
@Stable
class SmartRefreshState(type: SmartRefreshType) {

    /**
     * 刷新的偏移量
     */
    private val _indicatorOffset = Animatable(0f)

    private val mutatorMutex = MutatorMutex()

    var type: SmartRefreshType by mutableStateOf(type)

    val isIdle: Boolean get() = type is Idle

    /**
     * Whether this [SmartRefreshState] is currently refreshing or not.
     */
    val isRefreshing: Boolean get() = type is Refreshing

    /**
     * Whether a swipe/drag is currently in progress.
     */
    var isSwipeInProgress: Boolean by mutableStateOf(false)
        internal set

    /**
     * The current offset for the indicator, in pixels.
     */
    val indicatorOffset: Float get() = _indicatorOffset.value

    /**
     * 刷新布局是否滚动到顶部了
     */
    internal var isContentArriveTop = true

    internal suspend fun animateOffsetTo(
        offset: Float,
        animationSpec: AnimationSpec<Float> = spring()
    ) {
        mutatorMutex.mutate {
            _indicatorOffset.animateTo(offset, animationSpec = animationSpec)
        }
    }

    /**
     * Dispatch scroll delta in pixels from touch events.
     */
    internal suspend fun dispatchScrollDelta(delta: Float) {
        mutatorMutex.mutate(MutatePriority.UserInput) {
            _indicatorOffset.snapTo(_indicatorOffset.value + delta)
        }
    }
}

private class SmartRefreshNestedScrollConnection(
    private val state: SmartRefreshState,
    private val coroutineScope: CoroutineScope,
    private val onRefresh: () -> Unit,
) : NestedScrollConnection {

    var enabled: Boolean = false
    var refreshTriggerOffset: Float = 0f
    var maxIndicatorOffset: Float = 0f

    private var isHeaderSnapping = false

    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        val state = state
        if (!isHeaderSnapping
            && source == NestedScrollSource.Fling
            && available.y < 0
            && state.indicatorOffset > 0f
        ) {
            if (state.indicatorOffset < refreshTriggerOffset) {
                isHeaderSnapping = true
                coroutineScope.launch {
                    state.animateOffsetTo(0f)
                }.invokeOnCompletion {
                    isHeaderSnapping = false
                }
            }
            return available
        }

        return when {
            // If swiping isn't enabled, return zero
            !enabled -> Offset.Zero
            // If the user is swiping up, handle it
            source == NestedScrollSource.Drag && available.y < 0 -> onScroll(available)
            else -> Offset.Zero
        }
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        state.isContentArriveTop = available.y > 0

        val state = state
        val coroutineScope = coroutineScope
        val refreshTrigger = refreshTriggerOffset
        if (source == NestedScrollSource.Fling
            && available.y > 0
            && state.indicatorOffset <= refreshTrigger
        ) {
            val coerceMax = when (state.isIdle) {
                true -> refreshTrigger - 1f
                else -> refreshTrigger
            }
            val needConsumed = available.y.coerceAtMost(coerceMax)
            coroutineScope.launch {
                if (state.isIdle) {
                    state.dispatchScrollDelta(needConsumed)
                    state.animateOffsetTo(0f, spring(stiffness = Spring.StiffnessLow / 2))
                } else {
                    state.animateOffsetTo(refreshTrigger)
                }
            }
            return Offset(x = 0f, y = needConsumed)
        }

        return when {
            // If swiping isn't enabled, return zero
            !enabled -> Offset.Zero
            // If the user is swiping down and there's y remaining, handle it
            source == NestedScrollSource.Drag && available.y > 0 -> onScroll(available)
            else -> Offset.Zero
        }
    }

    private fun onScroll(available: Offset): Offset {
        val state = state
        val refreshTriggerOffset = refreshTriggerOffset
        val maxIndicatorOffset = maxIndicatorOffset
        val indicatorOffset = state.indicatorOffset
        val defaultDragMultiplier = DragMultiplier

        state.isSwipeInProgress = true

        // 禁止非空闲状态向下滚动
        if (indicatorOffset >= refreshTriggerOffset && !state.isIdle && available.y > 0) {
            return available
        }

        val dragMultiplier = when {
            !state.isIdle -> 1f
            available.y > 0 && indicatorOffset > refreshTriggerOffset -> {
                val delta = indicatorOffset - refreshTriggerOffset
                val decayDragMultiplier = defaultDragMultiplier - delta / max(refreshTriggerOffset, maxIndicatorOffset)
                decayDragMultiplier.coerceAtLeast(0.01f)
            }
            else -> defaultDragMultiplier
        }

        val coerceMax = if (state.isIdle) maxIndicatorOffset else refreshTriggerOffset
        val newOffset = (available.y * dragMultiplier + indicatorOffset).coerceIn(0f, coerceMax)
        val dragConsumed = newOffset - indicatorOffset

        if (indicatorOffset <= maxIndicatorOffset) {
            coroutineScope.launch {
                state.dispatchScrollDelta(dragConsumed)
            }
        }
        if (available.y > 0) {
            return available
        }
        return when (indicatorOffset) {
            0f -> Offset.Zero
            else -> available
        }
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        val state = state

        // If we're dragging, not currently refreshing and scrolled
        // past the trigger point, refresh!
        if (state.isIdle && state.indicatorOffset >= refreshTriggerOffset) {
            onRefresh()
        }

        // Reset the drag in progress state
        state.isSwipeInProgress = false

        return when {
            state.indicatorOffset > 0f && available.y > 0f -> available
            else -> Velocity.Zero
        }
    }
}

/**
 * 刷新组件，通过官方 [com.google.accompanist.swiperefresh.SwipeRefresh] 改造而来
 *
 * @param state 刷新状态容器
 * @param onRefresh 触发刷新时的回调
 * @param onIdle 触发空闲状态的回调，在刷新操作完成后会回调
 * @param modifier 修饰符
 * @param swipeEnabled 是否启用刷新
 * @param triggerRatio 触发刷新的占比，基于 [indicatorHeight]
 * @param maxDragRatio 最大拖拽占比，基于 [indicatorHeight]
 * @param indicatorHeight 刷新头的高度
 * @param indicator 刷新头
 * @param content 需要使用刷新的布局
 */
@Composable
fun SmartRefresh(
    state: SmartRefreshState,
    onRefresh: () -> Unit,
    onIdle: () -> Unit,
    modifier: Modifier = Modifier,
    swipeEnabled: Boolean = true,
    triggerRatio: Float = 1f,
    maxDragRatio: Float = 2f,
    indicatorHeight: Dp = 80.dp,
    indicator: @Composable (
        refreshState: SmartRefreshState,
        triggerPx: Float,
        maxDragPx: Float,
        indicatorHeight: Dp
    ) -> Unit = { refreshState, triggerPx, maxDragPx, height ->
        SmartRefreshIndicator(
            state = refreshState,
            triggerPx = triggerPx,
            maxDragPx = maxDragPx,
            height = height
        )
    },
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val updatedOnRefresh = rememberUpdatedState(onRefresh)
    val indicatorHeightPx = indicatorHeight.toPx()
    val refreshTriggerPx = indicatorHeightPx * triggerRatio
    val maxDragPx = indicatorHeightPx * maxDragRatio

    // Our LaunchedEffect, which animates the indicator to its resting position
    if (swipeEnabled) {
        HandleSmartIndicatorOffset(state, indicatorHeightPx, onIdle)
    }

    // Our nested scroll connection, which updates our state.
    val nestedScrollConnection = remember(state, coroutineScope) {
        SmartRefreshNestedScrollConnection(state, coroutineScope) {
            // On refresh, re-dispatch to the update onRefresh block
            updatedOnRefresh.value.invoke()
        }
    }.apply {
        this.enabled = swipeEnabled
        this.refreshTriggerOffset = refreshTriggerPx
        this.maxIndicatorOffset = maxDragPx
    }

    Box(modifier.nestedScroll(connection = nestedScrollConnection)) {
        Box(
            Modifier.align(Alignment.TopCenter)
                .let {
                    if (isHeaderNeedClip(
                            state,
                            indicatorHeightPx
                        )
                    ) it.clipToBounds() else it
                }
        ) {
            LaunchedEffect(key1 = state) {
                if (state.isRefreshing
                    && state.indicatorOffset == 0f
                    && state.isContentArriveTop
                ) {
                    state.animateOffsetTo(refreshTriggerPx)
                }
            }
            indicator(state, refreshTriggerPx, maxDragPx, indicatorHeight)
        }

        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationY = state.indicatorOffset.coerceAtMost(maxDragPx)
                }
        ) {
            content()
        }
    }
}

private fun isHeaderNeedClip(state: SmartRefreshState, indicatorHeight: Float): Boolean {
    return state.indicatorOffset < indicatorHeight
}

@Composable
private fun HandleSmartIndicatorOffset(
    state: SmartRefreshState,
    indicatorHeightPx: Float,
    onIdle: () -> Unit
) {
    val refreshType = state.type
    LaunchedEffect(state.isSwipeInProgress, refreshType) {
        when (refreshType) {
            is Idle -> {
                if (state.indicatorOffset != 0f) {
                    state.animateOffsetTo(0f)
                }
            }
            is Refreshing -> {
                if (state.indicatorOffset > indicatorHeightPx) {
                    state.animateOffsetTo(indicatorHeightPx)
                }
            }
            is FinishRefresh -> {
                delay(refreshType.dismissDelayMillis.coerceAtLeast(0))
                // 回调 onIdle 之前先 snap 回去，不然会瞥到 Idle 状态的 UI
                state.animateOffsetTo(0f)
                onIdle.invoke()
            }
        }
    }
}