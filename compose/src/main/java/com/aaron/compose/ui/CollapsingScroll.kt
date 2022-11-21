package com.aaron.compose.ui

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.NestedScrollSource.Companion.Drag
import androidx.compose.ui.input.nestedscroll.NestedScrollSource.Companion.Fling
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.aaron.compose.ktx.canScroll
import com.aaron.compose.ui.CollapsingScrollAnimationState.Collapsing
import com.aaron.compose.ui.CollapsingScrollAnimationState.Expanding
import com.aaron.compose.ui.CollapsingScrollAnimationState.Idle

/**
 * 折叠布局
 *
 * @param allowScrollToExpandedWhenCollapsed 是否允许折叠状态下滑动展开
 */
@Composable
fun CollapsingScroll(
    modifier: Modifier = Modifier,
    state: CollapsingScrollState = rememberCollapsingScrollState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    allowScrollToExpandedWhenCollapsed: () -> Boolean = { true },
    header: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    BoxWithConstraints {
        var scrollableForExpanded by remember {
            mutableStateOf(true)
        }
        Column(
            modifier = modifier
                .nestedScroll(remember(state) {
                    object : NestedScrollConnection {
                        override fun onPreScroll(
                            available: Offset,
                            source: NestedScrollSource
                        ): Offset {
                            if (state.isCollapsed
                                && available.y > 0
                                && !allowScrollToExpandedWhenCollapsed()
                                && state.isScrollInProgress
                                && (source == Drag || source == Fling)
                            ) {
                                scrollableForExpanded = false
                                return available
                            }
                            return super.onPreScroll(available, source)
                        }

                        override suspend fun onPostFling(
                            consumed: Velocity,
                            available: Velocity
                        ): Velocity {
                            scrollableForExpanded = true
                            return super.onPostFling(consumed, available)
                        }
                    }
                })
                .verticalScroll(
                    state = state.scrollState,
                    enabled = userScrollEnabled && scrollableForExpanded,
                    flingBehavior = flingBehavior
                )
                .padding(contentPadding)
        ) {
            Box(modifier = Modifier.width(this@BoxWithConstraints.maxWidth)) {
                header()
            }
            Box(
                modifier = Modifier
                    .nestedScroll(remember(state) {
                        object : NestedScrollConnection {
                            override fun onPreScroll(
                                available: Offset,
                                source: NestedScrollSource
                            ): Offset {
                                if (userScrollEnabled
                                    && available.y < 0f
                                    && !state.isCollapsed
                                ) {
                                    state.scrollState.dispatchRawDelta(-available.y)
                                    return available
                                }
                                return super.onPreScroll(available, source)
                            }
                        }
                    })
                    .width(this@BoxWithConstraints.maxWidth)
                    .height(this@BoxWithConstraints.maxHeight)
            ) {
                content()
            }
        }
    }
}

@Composable
fun rememberCollapsingScrollState(collapsed: Boolean = false): CollapsingScrollState {
    val state = rememberScrollState(if (collapsed) Int.MAX_VALUE else 0)
    return remember(state) {
        CollapsingScrollState(state)
    }
}

@Stable
class CollapsingScrollState(internal val scrollState: ScrollState) {

    /**
     * 是否折叠
     */
    val isCollapsed: Boolean get() = with(scrollState) { value == maxValue }

    /**
     * 记录动画状态
     */
    var animationState: CollapsingScrollAnimationState by mutableStateOf(Idle)
        private set

    /**
     * 滚动偏移
     */
    val offset: Int get() = scrollState.value

    /**
     * 最大滚动偏移
     */
    val maxOffset: Int get() = scrollState.maxValue

    /**
     * 交互事件
     */
    val interactionSource: InteractionSource get() = scrollState.interactionSource

    /**
     * 当前是否在滚动
     */
    val isScrollInProgress: Boolean get() = scrollState.isScrollInProgress

    /**
     * 折叠
     */
    suspend fun collapse(
        animate: Boolean = true,
        animationSpec: AnimationSpec<Float>? = null
    ) {
        val composeState = scrollState
        val value = composeState.maxValue
        runOnAnimationState(Collapsing) {
            if (animate) {
                if (animationSpec != null) {
                    composeState.animateScrollTo(value, animationSpec)
                } else {
                    composeState.animateScrollTo(value)
                }
            } else {
                composeState.scrollTo(value)
            }
        }
    }

    /**
     * 展开
     */
    suspend fun expand(
        animate: Boolean = true,
        animationSpec: AnimationSpec<Float>? = null
    ) {
        val composeState = scrollState
        runOnAnimationState(Expanding) {
            if (animate) {
                if (animationSpec != null) {
                    composeState.animateScrollTo(0, animationSpec)
                } else {
                    composeState.animateScrollTo(0)
                }
            } else {
                composeState.scrollTo(0)
            }
        }
    }

    /**
     * 切换伸缩状态
     */
    suspend fun toggle(
        animate: Boolean = true,
        animationSpec: AnimationSpec<Float>? = null
    ) {
        val animationState = animationState
        if (animationState == Expanding) {
            collapse(animate, animationSpec)
        } else if (animationState == Collapsing) {
            expand(animate, animationSpec)
        } else if (!isCollapsed) {
            collapse(animate, animationSpec)
        } else {
            expand(animate, animationSpec)
        }
    }

    /**
     * 判断能否滚动
     *
     * @param direction 小于 0 向上滚动，大于 0 向下滚动
     */
    fun canScroll(direction: Int): Boolean = scrollState.canScroll(direction)

    private inline fun runOnAnimationState(
        state: CollapsingScrollAnimationState,
        block: () -> Unit
    ) {
        try {
            animationState = state
            block()
        } finally {
            if (animationState == state) {
                animationState = Idle
            }
        }
    }
}

/**
 * 折叠动画状态
 */
enum class CollapsingScrollAnimationState {

    Idle, Collapsing, Expanding
}