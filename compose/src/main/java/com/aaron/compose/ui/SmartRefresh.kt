package com.aaron.compose.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationEndReason.BoundReached
import androidx.compose.animation.core.AnimationEndReason.Finished
import androidx.compose.animation.core.AnimationResult
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.exponentialDecay
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
import com.aaron.compose.ui.SmartRefreshType.Companion.Idle
import com.aaron.compose.ui.SmartRefreshType.Companion.Refreshing
import com.aaron.compose.ui.SmartRefreshType.Failure
import com.aaron.compose.ui.SmartRefreshType.FinishRefresh
import com.aaron.compose.ui.SmartRefreshType.FinishRefresh.Companion.DismissDelayMillis
import com.aaron.compose.ui.SmartRefreshType.Idle
import com.aaron.compose.ui.SmartRefreshType.Refreshing
import com.aaron.compose.ui.SmartRefreshType.Success
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 刷新阻尼
 */
private const val DragMultiplier = 0.5f

/**
 * 刷新状态
 */
@Stable
sealed class SmartRefreshType {

    companion object {
        internal val Idle: Idle = Idle()
        internal val Refreshing: Refreshing = Refreshing()
    }

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
    sealed class FinishRefresh(val dismissDelayMillis: Long) : SmartRefreshType() {
        companion object {
            const val DismissDelayMillis = 300L
        }
    }
    class Success(dismissDelayMillis: Long = DismissDelayMillis) : FinishRefresh(dismissDelayMillis)
    class Failure(dismissDelayMillis: Long = DismissDelayMillis) : FinishRefresh(dismissDelayMillis)
}

/**
 * 维护刷新期间各种状态，使用时应该将之维护在 [androidx.lifecycle.ViewModel] 中，
 * 因为 Compose 生命周期缘故，可能会频繁进入、退出 Composition ，导致状态被反复创建，
 * 例如在 [com.google.accompanist.pager.HorizontalPager] 中使用时，Pager 基于
 * [androidx.compose.foundation.lazy.LazyList] 构造而来，在切页时会出现回收的情况，
 * 因此状态将变得不明确。
 *
 * @param isRefreshing 初始刷新状态
 */
@Stable
class SmartRefreshState(isRefreshing: Boolean) {

    /**
     * 刷新的偏移量
     */
    private val _indicatorOffset = Animatable(0f)

    private val mutatorMutex = MutatorMutex()

    var type: SmartRefreshType by mutableStateOf(if (isRefreshing) Refreshing else Idle)
        private set

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

    internal val isAnimating: Boolean get() = _indicatorOffset.isRunning

    fun idle() {
        if (!isIdle) {
            type = Idle
        }
    }

    fun refreshing() {
        if (!isRefreshing) {
            type = Refreshing
        }
    }

    fun success(dismissDelayMillis: Long = DismissDelayMillis) {
        if (isRefreshing) {
            type = Success(dismissDelayMillis)
        }
    }

    fun failure(dismissDelayMillis: Long = DismissDelayMillis) {
        if (isRefreshing) {
            type = Failure(dismissDelayMillis)
        }
    }

    internal suspend fun animateOffsetTo(
        offset: Float,
        animationSpec: AnimationSpec<Float> = spring()
    ) {
        mutatorMutex.mutate {
            _indicatorOffset.animateTo(offset, animationSpec = animationSpec)
        }
    }

    internal suspend fun animateOffsetDecay(
        initialVelocity: Float,
        animationSpec: DecayAnimationSpec<Float> = exponentialDecay(),
        block: (Animatable<Float, AnimationVector1D>.() -> Unit)? = null
    ): AnimationResult<Float, AnimationVector1D> = mutatorMutex.mutate {
        _indicatorOffset.animateDecay(initialVelocity, animationSpec, block)
    }

    /**
     * Dispatch scroll delta in pixels from touch events.
     */
    internal suspend fun dispatchScrollDelta(delta: Float) {
        mutatorMutex.mutate(MutatePriority.UserInput) {
            _indicatorOffset.snapTo(_indicatorOffset.value + delta)
        }
    }

    internal fun updateScrollBounds(
        lowerBound: Float? = _indicatorOffset.lowerBound,
        upperBound: Float? = _indicatorOffset.upperBound
    ) {
        _indicatorOffset.updateBounds(lowerBound = lowerBound, upperBound = upperBound)
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

    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        // 保证滑动时默认最大距离
        state.updateScrollBounds(upperBound = maxIndicatorOffset)
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
        val state = state

        state.isContentArriveTop = available.y > 0

        val coroutineScope = coroutineScope
        val refreshTrigger = refreshTriggerOffset
        if (state.isIdle
            && source == NestedScrollSource.Fling
            && available.y > 0
            && state.indicatorOffset in 0f..refreshTrigger
            && consumed.y > 0f
        ) {
            val needConsumed = (consumed.y + available.y).coerceAtMost(refreshTrigger)
            coroutineScope.launch {
                state.animateOffsetTo(needConsumed, spring(stiffness = Spring.StiffnessHigh))
                state.animateOffsetTo(0f, spring(stiffness = Spring.StiffnessLow))
            }
            return Offset(x = 0f, y = available.y)
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

        val dragMultiplier = when {
            state.isRefreshing && indicatorOffset <= refreshTriggerOffset -> 1f
            available.y > 0 && indicatorOffset > refreshTriggerOffset -> {
                // 最大偏移量减去触发偏移量得出
                val residueOffset = maxIndicatorOffset - refreshTriggerOffset
                val delta = indicatorOffset - refreshTriggerOffset
                val ratio = delta / residueOffset * defaultDragMultiplier
                val decayDragMultiplier = defaultDragMultiplier - ratio
                decayDragMultiplier.coerceAtLeast(0.01f)
            }
            else -> defaultDragMultiplier
        }

        val coerceMax = maxIndicatorOffset
        val availableOffset = available.y * dragMultiplier
        val newOffset = (indicatorOffset + availableOffset).coerceIn(0f, coerceMax)
        val dragConsumed = newOffset - indicatorOffset

        if (indicatorOffset <= maxIndicatorOffset) {
            coroutineScope.launch {
                state.dispatchScrollDelta(dragConsumed)
            }
        }
        if (available.y > 0) {
            return available
        } else if (indicatorOffset != 0f) {
            return available
        }
        return Offset.Zero
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

        // 刷新头有露出来时缩回去
        if (state.indicatorOffset > 0f && available.y < 0f) {
            val animationResult = state.animateOffsetDecay(available.y) {
                updateBounds(lowerBound = 0f)
            }
            val endState = animationResult.endState
            return when (animationResult.endReason) {
                // 触达边界，只消费部分速度
                BoundReached -> {
                    // 剩下的速度
                    val residue = endState.velocity
                    val consumed = available.y - residue
                    Velocity(x = 0f, y = consumed)
                }
                // 正常结束动画，全部消费掉
                Finished -> available
            }
        }

        return Velocity.Zero
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        val state = state
        val initialOffset = state.indicatorOffset
        val triggerOffset = refreshTriggerOffset
        val maxOffset = maxIndicatorOffset
        // 触达顶部边界或者刷新头出来了且是向上滚动
        if (initialOffset >= 0f && available.y > 0f) {
            if (!state.isIdle && !state.isAnimating && state.indicatorOffset < triggerOffset) {
                // 非空闲状态直接使用衰减动画
                // 非空闲的话不给太多回弹，一点足够
                val upperBound = when {
                    available.y < 3000f -> triggerOffset
                    else -> triggerOffset * 1.2f
                }
                state.updateScrollBounds(upperBound = upperBound)
                state.animateOffsetDecay(available.y)
                state.updateScrollBounds(upperBound = maxOffset)
                if (state.indicatorOffset > triggerOffset) {
                    state.animateOffsetTo(triggerOffset, spring(stiffness = Spring.StiffnessLow))
                }
                return available
            }
        }
        return when {
            // 如果刷新头出来了，拦截掉
            initialOffset >= 0f && available.y > 0f -> available
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
        HandleSmartIndicatorOffset(state, refreshTriggerPx, onIdle)
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


    Box(
        modifier = modifier
            .nestedScroll(connection = nestedScrollConnection)
    ) {
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
    refreshTriggerPx: Float,
    onIdle: () -> Unit
) {
    val refreshType = state.type
    var isPrevTypeFinishRefresh by remember {
        mutableStateOf(refreshType is FinishRefresh)
    }
    LaunchedEffect(state.isSwipeInProgress, refreshType) {
        val _isPrevTypeFinishRefresh = isPrevTypeFinishRefresh
        isPrevTypeFinishRefresh = refreshType is FinishRefresh
        when (refreshType) {
            is Idle -> {
                if (state.indicatorOffset != 0f) {
                    state.animateOffsetTo(0f)
                }
            }
            is Refreshing -> {
                if (state.indicatorOffset > refreshTriggerPx) {
                    state.animateOffsetTo(refreshTriggerPx)
                }
            }
            is FinishRefresh -> {
                // 如果上一个状态也是 FinishRefresh ，就不要悬挂了
                // 这种情况一般是刷新完成时用户进行交互
                if (!_isPrevTypeFinishRefresh) {
                    delay(refreshType.dismissDelayMillis.coerceAtLeast(0))
                }
                // 回调 onIdle 之前先 snap 回去，不然会瞥到 Idle 状态的 UI
                state.animateOffsetTo(0f)
                onIdle.invoke()
            }
        }
    }
}