package com.aaron.compose.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.aaron.compose.utils.noLocalProvidedFor
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs

/**
 * 弹窗脚手架，处理弹窗层级，统一阴影，多个弹窗显示时按照显示顺序设置层级，[floating] 内部弹窗必须使用
 * [FloatingScaffoldScope.FloatingElement]，或基于 FloatingElement 的弹窗如：
 * [FloatingScaffoldScope.Dialog]、[FloatingScaffoldScope.BottomSheet]
 *
 * @param scrimColor 阴影颜色
 * @param scrimEnter 阴影显示动画
 * @param scrimExit 阴影消失动画
 * @param floating 弹窗
 * @param content 正文
 */
@Composable
fun FloatingScaffold(
    modifier: Modifier = Modifier,
    scrimColor: Color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
    scrimEnter: EnterTransition = fadeIn(animationSpec = tween()),
    scrimExit: ExitTransition = fadeOut(animationSpec = tween()),
    floating: @Composable FloatingScaffoldScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    CompositionLocalProvider(
        LocalFloatingTotal provides remember { FloatingTotal() }
    ) {
        Box(modifier = modifier) {
            content()

            val floatingTotal = LocalFloatingTotal.current
            AnimatedVisibility(
                modifier = Modifier
                    .zIndex(floatingTotal.zIndexForMinFocused())
                    .matchParentSize(),
                visible = floatingTotal.showScrim,
                enter = scrimEnter,
                exit = scrimExit,
                label = "Scrim"
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .drawBehind {
                            drawRect(color = scrimColor)
                        }
                )
            }

            val systemUiController = rememberSystemUiController()
            LaunchedEffect(floatingTotal, systemUiController) {
                val oldStatusState = systemUiController.statusBarDarkContentEnabled
                val oldNavBarState = systemUiController.navigationBarDarkContentEnabled
                snapshotFlow { floatingTotal.showScrim }
                    .collect { showScrim ->
                        if (showScrim) {
                            val darkIcons =
                                scrimColor.luminance() > 0.5f || scrimColor.alpha < 0.32f
                            systemUiController.statusBarDarkContentEnabled = darkIcons
                            systemUiController.navigationBarDarkContentEnabled = darkIcons
                        } else {
                            systemUiController.statusBarDarkContentEnabled = oldStatusState
                            systemUiController.navigationBarDarkContentEnabled = oldNavBarState
                        }
                    }
            }

            remember { FloatingScaffoldScopeImpl(this) }.floating()
        }
    }
}

/**
 * 居中弹窗
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FloatingScaffoldScope.Dialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    enter: EnterTransition = run {
        scaleIn(
            animationSpec = spring(
                stiffness = 2000f
            ),
            initialScale = 0.9f
        )
    },
    exit: ExitTransition = run {
        val e1 = fadeOut(
            animationSpec = spring(
                stiffness = Spring.StiffnessMedium
            )
        )
        val e2 = scaleOut(
            animationSpec = spring(
                stiffness = 2000f
            ),
            targetScale = 0.9f
        )
        e1 + e2
    },
    label: String = "Dialog",
    properties: FloatingElementProperties = FloatingElementProperties(),
    backgroundColor: Color = MaterialTheme.colors.surface,
    shape: Shape = RoundedCornerShape(4.dp),
    elevation: Dp = 24.dp,
    content: @Composable () -> Unit
) {
    FloatingElement(
        modifier = modifier,
        contentAlignment = Alignment.Center,
        visible = visible,
        onDismiss = onDismiss,
        enter = enter,
        exit = exit,
        label = label,
        properties = properties
    ) {
        Surface(
            modifier = Modifier
                .systemBarsPadding()
                .width(280.dp),
            color = backgroundColor,
            shape = shape,
            elevation = if (hasFocus) elevation else 0.dp
        ) {
            content()
        }
    }
}

/**
 * 底部弹窗
 */
@Composable
fun FloatingScaffoldScope.BottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    enter: EnterTransition = slideInVertically { it },
    exit: ExitTransition = slideOutVertically { it },
    label: String = "BottomSheet",
    properties: FloatingElementProperties = FloatingElementProperties(),
    backgroundColor: Color = MaterialTheme.colors.surface,
    shape: Shape = RectangleShape,
    elevation: Dp = 24.dp,
    content: @Composable () -> Unit
) {
    FloatingElement(
        modifier = modifier,
        contentAlignment = Alignment.BottomCenter,
        visible = visible,
        onDismiss = onDismiss,
        enter = enter,
        exit = exit,
        label = label,
        properties = properties
    ) {
        Surface(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth(),
            color = backgroundColor,
            shape = shape,
            elevation = if (hasFocus) elevation else 0.dp
        ) {
            content()
        }
    }
}

/**
 * 顶部通知
 *
 * @param swipeToDismissEnabled swipeToDismissEnabled 是否启用滑动隐藏
 * @param swipeToDismissTriggerDistance 滑动隐藏所需的滑动距离
 * @param onOffsetChange 滑动通知时的回调
 */
@Composable
fun FloatingScaffoldScope.Notification(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    enter: EnterTransition = slideInVertically { -it },
    exit: ExitTransition = slideOutVertically { -it },
    label: String = "Notification",
    properties: FloatingElementProperties = FloatingElementProperties(),
    swipeToDismissEnabled: Boolean = true,
    swipeToDismissTriggerDistance: Dp = 40.dp,
    onOffsetChange: ((Int) -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colors.surface,
    shape: Shape = RoundedCornerShape(4.dp),
    elevation: Dp = 24.dp,
    content: @Composable () -> Unit
) {
    FloatingElement(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter,
        visible = visible,
        onDismiss = onDismiss,
        enter = enter,
        exit = exit,
        label = label,
        properties = properties
    ) {
        Surface(
            modifier = modifier
                .systemBarsPadding()
                .swipeNotificationToDismiss(
                    visible = visible,
                    gestureEnabled = swipeToDismissEnabled,
                    onDismiss = onDismiss,
                    onOffsetChange = onOffsetChange,
                    swipeToDismissTriggerDistance = swipeToDismissTriggerDistance
                )
                .fillMaxWidth()
                .padding(16.dp),
            color = backgroundColor,
            shape = shape,
            elevation = if (hasFocus) elevation else 0.dp
        ) {
            content()
        }
    }
}

private fun Modifier.swipeNotificationToDismiss(
    visible: Boolean,
    gestureEnabled: Boolean,
    onDismiss: () -> Unit,
    onOffsetChange: ((Int) -> Unit)? = null,
    swipeToDismissTriggerDistance: Dp,
) = composed {
    val density = LocalDensity.current
    var curJob: Job? = remember { null }
    var offset by remember { mutableStateOf(0) }
    var dismissed by remember { mutableStateOf(false) }
    val curOnDismiss by rememberUpdatedState(onDismiss)
    val curOnDragOffsetChange by rememberUpdatedState(onOffsetChange)

    LaunchedEffect(key1 = visible) {
        if (visible) {
            offset = 0
            dismissed = false
        }
    }
    LaunchedEffect(key1 = Unit) {
        snapshotFlow { offset }
            .distinctUntilChanged()
            .collect {
                val absOffset = abs(it.coerceAtMost(0))
                curOnDragOffsetChange?.invoke(absOffset)
            }
    }

    this
        .offset {
            IntOffset(x = 0, y = offset.coerceAtMost(0))
        }
        .draggable(
            state = rememberDraggableState { delta ->
                offset += (delta * 0.75).toInt()
            },
            orientation = Orientation.Vertical,
            enabled = gestureEnabled && !dismissed,
            onDragStarted = {
                curJob?.cancel()
            },
            onDragStopped = {
                if (offset <= with(density) { -swipeToDismissTriggerDistance.toPx() }) {
                    dismissed = true
                    curOnDismiss()
                } else {
                    launch {
                        animate(
                            initialValue = offset.toFloat(),
                            targetValue = 0f,
                            animationSpec = spring(
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) { newVal, _ ->
                            offset = newVal.toInt()
                        }
                    }.also { job ->
                        curJob = job
                        job.invokeOnCompletion {
                            curJob = null
                        }
                    }
                }
            }
        )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FloatingScaffoldScope.FloatingElement(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    enter: EnterTransition = EnterTransition.None,
    exit: ExitTransition = ExitTransition.None,
    label: String = "FloatingElement",
    properties: FloatingElementProperties = FloatingElementProperties(),
    content: @Composable FloatingElementScope.() -> Unit
) {
    val floatingTotal = LocalFloatingTotal.current
    val uniqueId = remember { UUID.randomUUID().toString() }
    LaunchedEffect(visible, uniqueId, properties) {
        if (visible) {
            floatingTotal.addOnUpdate(uniqueId, properties)
        } else {
            floatingTotal.remove(uniqueId)
        }
    }

    AnimatedVisibility(
        modifier = modifier
            .zIndex(
                zIndex = when (visible) {
                    true -> floatingTotal.zIndex(uniqueId)
                    // 在退出时依然保持焦点模式，不被阴影遮挡
                    else -> floatingTotal.zIndexForMinFocused()
                }
            )
            .matchParentSize(),
        visible = visible,
        enter = EnterTransition.None,
        exit = ExitTransition.None,
        label = label
    ) {
        val curOnDismiss by rememberUpdatedState(onDismiss)
        val enableBackHandler = run {
            // 如果永远有焦点则不能拦截返回键
            !properties.focusedAlways
                    && properties.dismissOnBackPress
                    && visible
        }
        BackHandler(enableBackHandler) {
            curOnDismiss()
        }
        Box(modifier = modifier.matchParentSize()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .run {
                        // 如果永远有焦点则不能拦截阴影点击
                        if (!visible || properties.focusedAlways) this else {
                            pointerInput(Unit) {
                                detectTapGestures {
                                    if (properties.dismissOnClickOutside) {
                                        curOnDismiss()
                                    }
                                }
                            }
                        }
                    }
            )

            transition.AnimatedVisibility(
                modifier = Modifier
                    .align(contentAlignment)
                    .focusable(enabled = floatingTotal.hasFocus(uniqueId)),
                visible = { it == EnterExitState.Visible },
                enter = enter,
                exit = exit
            ) {
                val floatingElementScope = remember {
                    FloatingElementScopeImpl(animatedVisibilityScope = this)
                }
                LaunchedEffect(key1 = floatingTotal) {
                    snapshotFlow { floatingTotal.hasFocus(uniqueId) }
                        .collect {
                            floatingElementScope.hasFocus = it
                        }
                }
                floatingElementScope.content()
            }
        }
    }
}

data class FloatingElementProperties(
    /** 点击返回按钮消失 */
    val dismissOnBackPress: Boolean = true,
    /** 点击弹窗外部区域消失 */
    val dismissOnClickOutside: Boolean = true,
    /**
     * 永远有焦点，不被阴影遮挡，如果当前只有自己将移除阴影，[dismissOnBackPress] 、[dismissOnClickOutside] 将失效
     */
    val focusedAlways: Boolean = false
)

@Stable
interface FloatingScaffoldScope : BoxScope

private class FloatingScaffoldScopeImpl(
    boxScope: BoxScope
) : FloatingScaffoldScope, BoxScope by boxScope

@Stable
interface FloatingElementScope : AnimatedVisibilityScope {

    val hasFocus: Boolean
}

private class FloatingElementScopeImpl(
    animatedVisibilityScope: AnimatedVisibilityScope,
) : FloatingElementScope, AnimatedVisibilityScope by animatedVisibilityScope {

    override var hasFocus: Boolean by mutableStateOf(false)
}

private val LocalFloatingTotal = compositionLocalOf<FloatingTotal> {
    noLocalProvidedFor("LocalFloatingTotal")
}

private class FloatingTotal {

    private val visibleBucket = mutableStateListOf<Floating>()

    val visibleCount: Int by derivedStateOf { visibleBucket.size }

    val showScrim: Boolean by derivedStateOf {
        visibleBucket.run {
            val tryRemoveScrimCount = filter { it.properties.focusedAlways }.size
            tryRemoveScrimCount < size
        }
    }

    fun hasFocus(id: String): Boolean {
        return visibleBucket.run {
            lastOrNull()?.id == id
                    || find { it.id == id }?.properties?.focusedAlways == true
        }
    }

    fun zIndex(id: String): Float {
        val visibleBucket = visibleBucket
        val index = visibleBucket.indexOfFirst { it.id == id }
        val item = visibleBucket.getOrNull(index)
        if (item?.properties?.focusedAlways == true) {
            return zIndexMax()
        }
        return safeZIndex(index.toFloat())
    }

    fun zIndexForMinFocused(): Float {
        val index = visibleBucket.indexOfLast { it.properties.focusedAlways.not() }
        return safeZIndex(index.toFloat())
    }

    private fun zIndexMax(): Float {
        return safeZIndex(visibleBucket.size.toFloat())
    }

    private fun safeZIndex(zIndex: Float): Float {
        return zIndex.coerceAtLeast(0f)
    }

    fun addOnUpdate(id: String, properties: FloatingElementProperties) {
        val visibleBucket = visibleBucket
        val index = visibleBucket.indexOfFirst { it.id == id }
        if (index == -1) {
            visibleBucket.add(Floating(id, properties))
        } else {
            visibleBucket[index] = Floating(id, properties)
        }
    }

    fun remove(id: String) {
        visibleBucket.removeAll { it.id == id }
    }
}

private data class Floating(
    val id: String,
    val properties: FloatingElementProperties
)