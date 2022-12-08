package com.aaron.compose.ui.refresh

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
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.aaron.compose.ktx.toDp
import com.aaron.compose.ktx.toPx
import com.aaron.compose.ui.refresh.SmartRefreshType.FinishRefresh
import com.aaron.compose.ui.refresh.SmartRefreshType.Idle
import com.aaron.compose.ui.refresh.SmartRefreshType.Refreshing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 刷新阻尼
 */
private const val DragMultiplier = 0.5f

@Composable
fun rememberSmartRefreshState(type: SmartRefreshType): SmartRefreshState {
    return rememberSaveable(saver = SmartRefreshState.Saver) {
        SmartRefreshState(type)
    }.also {
        it.type = type
    }
}

/**
 * 刷新状态
 */
@Stable
sealed class SmartRefreshType {

    companion object {
        internal const val TypeIdle = 1
        internal const val TypeRefreshing = 2
        internal const val TypeSuccess = 3
        internal const val TypeFailure = 4

        internal fun create(type: Int): SmartRefreshType {
            return when (type) {
                TypeIdle -> Idle
                TypeRefreshing -> Refreshing
                TypeSuccess -> Success(0)
                TypeFailure -> Failure(0)
                else -> error("Unknown type: $type")
            }
        }
    }

    internal abstract val intType: Int

    object Idle : SmartRefreshType() {
        override val intType: Int = TypeIdle
    }
    object Refreshing : SmartRefreshType() {
        override val intType: Int = TypeRefreshing
    }

    /**
     * 结束刷新状态， [dismissDelayMillis] 表示要悬挂多久
     */
    sealed class FinishRefresh(val dismissDelayMillis: Long) : SmartRefreshType() {
        companion object {
            const val DismissDelayMillis = 300L
        }
    }
    class Success(
        dismissDelayMillis: Long = DismissDelayMillis
    ) : FinishRefresh(dismissDelayMillis) {
        override val intType: Int = TypeSuccess
    }
    class Failure(
        dismissDelayMillis: Long = DismissDelayMillis
    ) : FinishRefresh(dismissDelayMillis) {
        override val intType: Int = TypeFailure
    }
}

/**
 * 维护刷新期间各种状态
 *
 * @param isRefreshing 初始刷新状态
 */
@Stable
class SmartRefreshState internal constructor(
    type: SmartRefreshType,
    indicatorOffset: Float,
    isContentArriveTop: Boolean
) {

    companion object {
        val Saver: Saver<SmartRefreshState, *> = listSaver(
            save = {
                listOf(it.type.intType, it.indicatorOffset, it.isContentArriveTop)
            },
            restore = {
                SmartRefreshState(
                    type = SmartRefreshType.create(it[0] as Int),
                    indicatorOffset = it[1] as Float,
                    isContentArriveTop = it[2] as Boolean
                )
            }
        )
    }

    constructor(type: SmartRefreshType) : this(
        type = type,
        indicatorOffset = 0f,
        isContentArriveTop = true
    )

    /**
     * 刷新的偏移量
     */
    private val _indicatorOffset = Animatable(indicatorOffset)

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
    internal var isContentArriveTop = isContentArriveTop

    internal val isAnimating: Boolean get() = _indicatorOffset.isRunning

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
    private val translateBodyEnabled: Boolean,
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
        if (translateBodyEnabled
            && state.isIdle
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
        if (translateBodyEnabled && initialOffset >= 0f && available.y > 0f) {
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
 * @param clipHeaderEnabled 是否启用裁剪头部
 * @param translateBodyEnabled [SmartRefreshState.indicatorOffset] 偏移时，主体部分是否也跟着偏移
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
    clipHeaderEnabled: Boolean = true,
    translateBodyEnabled: Boolean = true,
    triggerRatio: Float = 1f,
    maxDragRatio: Float = 2f,
    indicatorHeight: Dp = 80.dp,
    indicator: @Composable (
        smartRefreshState: SmartRefreshState,
        triggerDistance: Dp,
        maxDragDistance: Dp,
        indicatorHeight: Dp
    ) -> Unit = { smartRefreshState, triggerDistance, maxDragDistance, height ->
        SmartRefreshIndicator(
            state = smartRefreshState,
            triggerDistance = triggerDistance,
            maxDragDistance = maxDragDistance,
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
    val nestedScrollConnection = remember(state, swipeEnabled, translateBodyEnabled, coroutineScope) {
        SmartRefreshNestedScrollConnection(state, swipeEnabled && translateBodyEnabled, coroutineScope) {
            // On refresh, re-dispatch to the update onRefresh block
            updatedOnRefresh.value.invoke()
        }
    }.apply {
        this.enabled = swipeEnabled
        this.refreshTriggerOffset = refreshTriggerPx
        this.maxIndicatorOffset = maxDragPx
    }

    Box(
        modifier = modifier.nestedScroll(connection = nestedScrollConnection)
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    if (translateBodyEnabled) {
                        translationY = state.indicatorOffset.coerceAtMost(maxDragPx)
                    }
                }
        ) {
            content()
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .let {
                    if (clipHeaderEnabled && isHeaderNeedClip(
                            state,
                            indicatorHeightPx
                        )
                    ) it.clipToBounds() else it
                }
        ) {
            Box(
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                LaunchedEffect(key1 = state) {
                    if (state.isRefreshing
                        && state.indicatorOffset == 0f
                        && state.isContentArriveTop
                    ) {
                        state.animateOffsetTo(refreshTriggerPx)
                    }
                }
                indicator(state, refreshTriggerPx.toDp(), maxDragPx.toDp(), indicatorHeight)
            }
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