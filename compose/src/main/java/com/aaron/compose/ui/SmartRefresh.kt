package com.aaron.compose.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.aaron.compose.ui.SmartRefreshType.FinishRefresh
import com.aaron.compose.ui.SmartRefreshType.Idle
import com.aaron.compose.ui.SmartRefreshType.Refresh
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

private const val DragMultiplier = 0.5f

/**
 * Creates a [SmartRefreshState] that is remembered across compositions.
 *
 * Changes to [isRefreshing] will result in the [SmartRefreshState] being updated.
 *
 * @param isRefreshing the value for [SmartRefreshState.isRefreshing]
 */
@Composable
fun rememberSmartRefreshState(
    type: SmartRefreshType = Idle()
): SmartRefreshState = remember {
    SmartRefreshState(type = type)
}.apply {
    this.type = type
}

sealed class SmartRefreshType {

    class Idle : SmartRefreshType()
    class Refresh : SmartRefreshType()

    sealed class FinishRefresh(val dismissDelayMillis: Long) : SmartRefreshType()
    class Success(dismissDelayMillis: Long = 0) : FinishRefresh(dismissDelayMillis)
    class Failure(dismissDelayMillis: Long = 0) : FinishRefresh(dismissDelayMillis)
}

/**
 * A state object that can be hoisted to control and observe changes for [SmartRefresh].
 *
 * In most cases, this will be created via [rememberSmartRefreshState].
 *
 * @param isRefreshing the initial value for [SmartRefreshState.isRefreshing]
 */
@Stable
class SmartRefreshState(type: SmartRefreshType) {
    private val _indicatorOffset = Animatable(0f)
    private val mutatorMutex = MutatorMutex()

    var type: SmartRefreshType by mutableStateOf(type)

    val isIdle: Boolean get() = type is Idle

    /**
     * Whether this [SmartRefreshState] is currently refreshing or not.
     */
    val isRefreshing: Boolean get() = type is Refresh

    /**
     * Whether a swipe/drag is currently in progress.
     */
    var isSwipeInProgress: Boolean by mutableStateOf(false)
        internal set

    /**
     * The current offset for the indicator, in pixels.
     */
    val indicatorOffset: Float get() = _indicatorOffset.value

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
    var refreshTrigger: Float = 0f
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
            if (state.indicatorOffset < refreshTrigger) {
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
            // If we're refreshing, return zero
//            !state.isIdle -> Offset.Zero
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
        val state = state
        val coroutineScope = coroutineScope
        val refreshTrigger = refreshTrigger
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
            // If we're refreshing, return zero
//            !state.isIdle -> Offset.Zero
            // If the user is swiping down and there's y remaining, handle it
            source == NestedScrollSource.Drag && available.y > 0 -> onScroll(available)
            else -> Offset.Zero
        }
    }

    private fun onScroll(available: Offset): Offset {
        val state = state
        val refreshTrigger = refreshTrigger
        val maxIndicatorOffset = maxIndicatorOffset
        val indicatorOffset = state.indicatorOffset
        val defaultDragMultiplier = DragMultiplier

        state.isSwipeInProgress = true

        // 禁止非空闲状态向下滚动
        if (indicatorOffset >= refreshTrigger && !state.isIdle && available.y > 0) {
            return available
        }

        val dragMultiplier = when {
            !state.isIdle -> 1f
            available.y > 0 && indicatorOffset > refreshTrigger -> {
                val delta = indicatorOffset - refreshTrigger
                val decayDragMultiplier = defaultDragMultiplier - delta / max(refreshTrigger, maxIndicatorOffset)
                decayDragMultiplier.coerceAtLeast(0.01f)
            }
            else -> defaultDragMultiplier
        }

        val coerceMax = if (state.isIdle) maxIndicatorOffset else refreshTrigger
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
        if (state.isIdle && state.indicatorOffset >= refreshTrigger) {
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
 * A layout which implements the swipe-to-refresh pattern, allowing the user to refresh content via
 * a vertical swipe gesture.
 *
 * This layout requires its content to be scrollable so that it receives vertical swipe events.
 * The scrollable content does not need to be a direct descendant though. Layouts such as
 * [androidx.compose.foundation.lazy.LazyColumn] are automatically scrollable, but others such as
 * [androidx.compose.foundation.layout.Column] require you to provide the
 * [androidx.compose.foundation.verticalScroll] modifier to that content.
 *
 * Apps should provide a [onRefresh] block to be notified each time a swipe to refresh gesture
 * is completed. That block is responsible for updating the [state] as appropriately,
 * typically by setting [SmartRefreshState.isRefreshing] to `true` once a 'refresh' has been
 * started. Once a refresh has completed, the app should then set
 * [SmartRefreshState.isRefreshing] to `false`.
 *
 * If an app wishes to show the progress animation outside of a swipe gesture, it can
 * set [SmartRefreshState.isRefreshing] as required.
 *
 * This layout does not clip any of it's contents, including the indicator. If clipping
 * is required, apps can provide the [androidx.compose.ui.draw.clipToBounds] modifier.
 *
 * @sample com.google.accompanist.sample.swiperefresh.SwipeRefreshSample
 *
 * @param state the state object to be used to control or observe the [SwipeRefreshLayout] state.
 * @param onRefresh Lambda which is invoked when a swipe to refresh gesture is completed.
 * @param modifier the modifier to apply to this layout.
 * @param swipeEnabled Whether the the layout should react to swipe gestures or not.
 * @param indicator the indicator that represents the current state. By default this
 * will use a [OfficialSwipeRefreshIndicator].
 * @param clipIndicatorToPadding Whether to clip the indicator to [indicatorPadding]. If false is
 * provided the indicator will be clipped to the [content] bounds. Defaults to true.
 * @param content The content containing a scroll composable.
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
        ClassicSmartRefreshIndicator(
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
    val indicatorHeightPx = with(LocalDensity.current) { indicatorHeight.toPx() }
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
        this.refreshTrigger = refreshTriggerPx
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
            indicator(state, refreshTriggerPx, maxDragPx, indicatorHeight)
        }

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        0,
                        state.indicatorOffset.roundToInt().coerceAtMost(maxDragPx.roundToInt())
                    )
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
            is Refresh -> {
                if (state.indicatorOffset > indicatorHeightPx) {
                    state.animateOffsetTo(indicatorHeightPx)
                }
            }
            is FinishRefresh -> {
//                if (state.indicatorOffset > indicatorHeightPx) {
//                    state.animateOffsetTo(indicatorHeightPx)
//                }
                // 保证最少有 300 毫秒悬挂，不然效果不佳
                delay(refreshType.dismissDelayMillis.coerceAtLeast(300))
                // 回调 onIdle 之前先 snap 回去，不然会瞥到 Idle 状态的 UI
                state.animateOffsetTo(0f)
                onIdle.invoke()
            }
        }
    }
}