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
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.autoSaver
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
import com.aaron.compose.ktx.toPx
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
 * ????????????
 */
private const val DragMultiplier = 0.5f

@Composable
fun rememberSmartRefreshState(isRefreshing: Boolean): SmartRefreshState {
    return rememberSaveable(saver = SmartRefreshState.Saver) {
        SmartRefreshState(isRefreshing)
    }
}

/**
 * ????????????
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
     * ????????????????????? [dismissDelayMillis] ?????????????????????
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
 * ??????????????????????????????
 *
 * @param isRefreshing ??????????????????
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

    constructor(isRefreshing: Boolean) : this(
        type = if (isRefreshing) Refreshing else Idle,
        indicatorOffset = 0f,
        isContentArriveTop = true
    )

    /**
     * ??????????????????
     */
    private val _indicatorOffset = Animatable(indicatorOffset)

    private val mutatorMutex = MutatorMutex()

    var type: SmartRefreshType by mutableStateOf(type)
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
     * ????????????????????????????????????
     */
    internal var isContentArriveTop = isContentArriveTop

    internal val isAnimating: Boolean get() = _indicatorOffset.isRunning

    fun idle() {
        if (!isIdle) {
            type = Idle
        }
    }

    fun refresh() {
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
        // ?????????????????????????????????
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
                // ??????????????????????????????????????????
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

        // ?????????????????????????????????
        if (state.indicatorOffset > 0f && available.y < 0f) {
            val animationResult = state.animateOffsetDecay(available.y) {
                updateBounds(lowerBound = 0f)
            }
            val endState = animationResult.endState
            return when (animationResult.endReason) {
                // ????????????????????????????????????
                BoundReached -> {
                    // ???????????????
                    val residue = endState.velocity
                    val consumed = available.y - residue
                    Velocity(x = 0f, y = consumed)
                }
                // ????????????????????????????????????
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
        // ????????????????????????????????????????????????????????????
        if (initialOffset >= 0f && available.y > 0f) {
            if (!state.isIdle && !state.isAnimating && state.indicatorOffset < triggerOffset) {
                // ???????????????????????????????????????
                // ????????????????????????????????????????????????
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
            // ????????????????????????????????????
            initialOffset >= 0f && available.y > 0f -> available
            else -> Velocity.Zero
        }
    }
}

/**
 * ??????????????????????????? [com.google.accompanist.swiperefresh.SwipeRefresh] ????????????
 *
 * @param state ??????????????????
 * @param onRefresh ????????????????????????
 * @param onIdle ???????????????????????????????????????????????????????????????
 * @param modifier ?????????
 * @param swipeEnabled ??????????????????
 * @param triggerRatio ?????????????????????????????? [indicatorHeight]
 * @param maxDragRatio ??????????????????????????? [indicatorHeight]
 * @param indicatorHeight ??????????????????
 * @param indicator ?????????
 * @param content ???????????????????????????
 */
@Composable
fun SmartRefresh(
    state: SmartRefreshState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    onIdle: () -> Unit = { state.idle() },
    swipeEnabled: Boolean = true,
    triggerRatio: Float = 1f,
    maxDragRatio: Float = 2f,
    indicatorHeight: Dp = 80.dp,
    indicator: @Composable (
        smartRefreshState: SmartRefreshState,
        triggerPixels: Float,
        maxDragPixels: Float,
        indicatorHeight: Dp
    ) -> Unit = { smartRefreshState, triggerPixels, maxDragPixels, height ->
        SmartRefreshIndicator(
            state = smartRefreshState,
            triggerPx = triggerPixels,
            maxDragPx = maxDragPixels,
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
        modifier = Modifier
            .nestedScroll(connection = nestedScrollConnection)
            .then(modifier)
    ) {
        Box(
            Modifier.align(Alignment.TopCenter)
//                .let {
//                    if (isHeaderNeedClip(
//                            state,
//                            indicatorHeightPx
//                        )
//                    ) it.clipToBounds() else it
//                }
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
                // ??????????????????????????? FinishRefresh ?????????????????????
                // ??????????????????????????????????????????????????????
                if (!_isPrevTypeFinishRefresh) {
                    delay(refreshType.dismissDelayMillis.coerceAtLeast(0))
                }
                // ?????? onIdle ????????? snap ???????????????????????? Idle ????????? UI
                state.animateOffsetTo(0f)
                onIdle.invoke()
            }
        }
    }
}