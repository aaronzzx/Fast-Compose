package com.aaron.compose.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ui.VisibilityContainerDefaults.BottomSheetEnterTransition
import com.aaron.compose.ui.VisibilityContainerDefaults.BottomSheetExitTransition
import com.aaron.compose.ui.VisibilityContainerDefaults.DialogEnterTransition
import com.aaron.compose.ui.VisibilityContainerDefaults.DialogExitTransition
import com.aaron.compose.ui.VisibilityContainerDefaults.Elevation
import com.aaron.compose.ui.VisibilityContainerDefaults.NotificationEnterTransition
import com.aaron.compose.ui.VisibilityContainerDefaults.NotificationExitTransition
import com.aaron.compose.ui.VisibilityContainerDefaults.ScrimEnterTransition
import com.aaron.compose.ui.VisibilityContainerDefaults.ScrimExitTransition
import com.aaron.compose.ui.VisibilityContainerDefaults.VisibilityContainerProperties
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * 模拟 Dialog
 *
 * @param show 是否显示
 * @param scrimColor 遮罩颜色。
 * @param scrimEnter 遮罩进入动画。
 * @param scrimExit 遮罩退出动画。
 * @param properties 处理返回键等行为。
 */
@Deprecated("改用 FloatingScaffold")
@Composable
fun VisibilityScrimContainer(
    show: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    scrimColor: Color = VisibilityContainerDefaults.scrimColor,
    scrimEnter: EnterTransition = ScrimEnterTransition,
    scrimExit: ExitTransition = ScrimExitTransition,
    properties: VisibilityContainerProperties = VisibilityContainerProperties,
    content: @Composable VisibilityContainerScope.() -> Unit
) {
    val state = rememberVisibilityContainerState()
    SideEffect {
        if (show) {
            state.show()
        } else {
            state.hide()
        }
    }
    VisibilityScrimContainer(
        state = state,
        modifier = modifier,
        scrimColor = scrimColor,
        scrimEnter = scrimEnter,
        scrimExit = scrimExit,
        properties = properties,
        onDismiss = onDismiss,
        content = content
    )
}

/**
 * 模拟 Dialog
 *
 * @param state 控制 [VisibilityScrimContainer] 的显示隐藏。
 * @param scrimColor 遮罩颜色。
 * @param scrimEnter 遮罩进入动画。
 * @param scrimExit 遮罩退出动画。
 * @param properties 处理返回键等行为。
 */
@Deprecated("改用 FloatingScaffold")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun VisibilityScrimContainer(
    state: VisibilityContainerState,
    modifier: Modifier = Modifier,
    scrimColor: Color = VisibilityContainerDefaults.scrimColor,
    scrimEnter: EnterTransition = ScrimEnterTransition,
    scrimExit: ExitTransition = ScrimExitTransition,
    properties: VisibilityContainerProperties = VisibilityContainerProperties,
    onDismiss: () -> Unit = { state.hide() },
    content: @Composable VisibilityContainerScope.() -> Unit
) {
    val curOnDismiss by rememberUpdatedState(newValue = onDismiss)
    BackHandler(enabled = state.visible && properties.dismissOnBackPress) {
        curOnDismiss()
    }
    VisibilityContainer(
        modifier = modifier,
        state = state,
        scrim = {
            Scrim(
                modifier = Modifier
                    .fillMaxSize()
                    .animateEnterExit(
                        enter = scrimEnter,
                        exit = scrimExit,
                        label = "ScrimAnimation"
                    ),
                color = scrimColor,
                onDismiss = {
                    curOnDismiss()
                },
                enabled = state.visible && properties.dismissOnClickOutside
            )
        }
    ) {
        content()
    }
}

/**
 * 模拟 Popup
 *
 * @param scrim 遮罩视图。
 */
@Deprecated("改用 FloatingScaffold")
@Composable
fun VisibilityContainer(
    show: Boolean,
    modifier: Modifier = Modifier,
    scrim: (@Composable VisibilityContainerScope.() -> Unit)? = null,
    content: @Composable VisibilityContainerScope.() -> Unit
) {
    val state = rememberVisibilityContainerState()
    SideEffect {
        if (show) {
            state.show()
        } else {
            state.hide()
        }
    }
    VisibilityContainer(
        modifier = modifier,
        state = state,
        scrim = scrim,
        content = content
    )
}

/**
 * 模拟 Popup
 *
 * @param state 控制 [VisibilityContainer] 的显示隐藏。
 * @param scrim 遮罩视图。
 */
@Deprecated("改用 FloatingScaffold")
@Composable
fun VisibilityContainer(
    state: VisibilityContainerState,
    modifier: Modifier = Modifier,
    scrim: (@Composable VisibilityContainerScope.() -> Unit)? = null,
    content: @Composable VisibilityContainerScope.() -> Unit
) {
    AnimatedVisibility(
        visible = state.visible,
        modifier = modifier.fillMaxSize(),
        enter = EnterTransition.None,
        exit = ExitTransition.None,
        label = "VisibilityContainer"
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val visibilityContainerScope = remember {
                RealVisibilityContainerScope(
                    boxWithConstraintsScope = this,
                    animatedVisibilityScope = this@AnimatedVisibility
                )
            }
            scrim?.invoke(visibilityContainerScope)
            visibilityContainerScope.content()
        }
    }
}

/**
 * 获取 [VisibilityContainerState]
 */
@Composable
fun rememberVisibilityContainerState(
    initialVisible: Boolean = false
): VisibilityContainerState {
    return remember {
        VisibilityContainerState(initialVisible)
    }
}

/**
 * 控制显示隐藏等状态。
 */
@Stable
class VisibilityContainerState(initialVisible: Boolean) {

    var visible by mutableStateOf(initialVisible)
        private set

    fun show() {
        visible = true
    }

    fun hide() {
        visible = false
    }
}

/**
 * 遮罩
 */
@Composable
private fun Scrim(
    color: Color,
    onDismiss: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val dismissModifier = if (enabled) {
        Modifier.pointerInput(onDismiss) { detectTapGestures { onDismiss() } }
    } else {
        Modifier
    }

    Canvas(
        modifier
            .fillMaxSize()
            .then(dismissModifier)
    ) {
        if (color.isSpecified) {
            drawRect(color = color)
        }
    }
}

/**
 * 处理返回键、点击空白处后的行为
 */
@Stable
class VisibilityContainerProperties(
    val dismissOnBackPress: Boolean = true,
    val dismissOnClickOutside: Boolean = true
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VisibilityContainerProperties

        if (dismissOnBackPress != other.dismissOnBackPress) return false
        if (dismissOnClickOutside != other.dismissOnClickOutside) return false

        return true
    }

    override fun hashCode(): Int {
        var result = dismissOnBackPress.hashCode()
        result = 31 * result + dismissOnClickOutside.hashCode()
        return result
    }
}

@Stable
interface VisibilityContainerScope : BoxWithConstraintsScope, AnimatedVisibilityScope

private class RealVisibilityContainerScope(
    boxWithConstraintsScope: BoxWithConstraintsScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) : VisibilityContainerScope,
    BoxWithConstraintsScope by boxWithConstraintsScope,
    AnimatedVisibilityScope by animatedVisibilityScope

/**
 * 弹出 Dialog
 *
 * @param enter Dialog 进入动画。
 * @param exit Dialog 退出动画。
 * @param backgroundColor Dialog 背景色。
 * @param shape Dialog 形状。
 * @param elevation Dialog 阴影高度。
 */
@Deprecated("改用 FloatingScaffold")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun VisibilityContainerScope.Dialog(
    modifier: Modifier = Modifier,
    enter: EnterTransition = DialogEnterTransition,
    exit: ExitTransition = DialogExitTransition,
    backgroundColor: Color = MaterialTheme.colors.surface,
    shape: Shape = RoundedCornerShape(4.dp),
    elevation: Dp = Elevation,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .systemBarsPadding()
            .align(Alignment.Center)
            .animateEnterExit(
                enter = enter,
                exit = exit,
                label = "DialogAnimation"
            )
            .shadow(
                elevation = elevation,
                shape = shape,
                clip = false
            )
            .width(280.dp)
            .clipToBackground(
                color = backgroundColor,
                shape = shape
            )
            .pointerInput(Unit) {}
    ) {
        content()
    }
}

/**
 * 弹出 BottomDialog
 *
 * @param enter Dialog 进入动画。
 * @param exit Dialog 退出动画。
 * @param backgroundColor Dialog 背景色。
 * @param shape Dialog 形状。
 * @param elevation Dialog 阴影高度。
 */
@Deprecated("改用 FloatingScaffold")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun VisibilityContainerScope.BottomSheet(
    modifier: Modifier = Modifier,
    enter: EnterTransition = BottomSheetEnterTransition,
    exit: ExitTransition = BottomSheetExitTransition,
    backgroundColor: Color = MaterialTheme.colors.surface,
    shape: Shape = RectangleShape,
    elevation: Dp = Elevation,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .statusBarsPadding()
            .align(Alignment.BottomCenter)
            .animateEnterExit(
                enter = enter,
                exit = exit,
                label = "BottomSheetAnimation"
            )
            .shadow(
                elevation = elevation,
                shape = shape,
                clip = false
            )
            .fillMaxWidth()
            .clipToBackground(
                color = backgroundColor,
                shape = shape
            )
            .pointerInput(Unit) {}
    ) {
        content()
    }
}

/**
 * 弹出顶部通知
 *
 * @param onSwipeToDismiss 滑动隐藏的回调
 * @param enter 进入动画
 * @param exit 退出动画
 * @param swipeToDismissEnabled 是否启用滑动隐藏
 * @param swipeToDismissTriggerDistance 滑动隐藏所需的滑动距离
 * @param onOffsetChange 滑动通知时的回调
 * @param contentPadding 通知内部间距
 * @param backgroundColor 背景色
 * @param shape 形状
 * @param elevation 阴影高度
 */
@Deprecated("改用 FloatingScaffold")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun VisibilityContainerScope.Notification(
    onSwipeToDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    enter: EnterTransition = NotificationEnterTransition,
    exit: ExitTransition = NotificationExitTransition,
    swipeToDismissEnabled: Boolean = true,
    swipeToDismissTriggerDistance: Dp = 40.dp,
    onOffsetChange: ((Int) -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    backgroundColor: Color = MaterialTheme.colors.surface,
    shape: Shape = RoundedCornerShape(4.dp),
    elevation: Dp = Elevation,
    content: @Composable() (BoxScope.() -> Unit)
) {
    Box(
        modifier = modifier
            .navigationBarsPadding()
            .swipeNotificationToDismiss(
                gestureEnabled = swipeToDismissEnabled,
                onDismiss = onSwipeToDismiss,
                onOffsetChange = onOffsetChange,
                swipeToDismissTriggerDistance = swipeToDismissTriggerDistance
            )
            .align(Alignment.TopCenter)
            .animateEnterExit(
                enter = enter,
                exit = exit,
                label = "NotificationAnimation"
            )
            .padding(contentPadding)
            .shadow(
                elevation = elevation,
                shape = shape,
                clip = false
            )
            .fillMaxWidth()
            .clipToBackground(
                color = backgroundColor,
                shape = shape
            )
            .pointerInput(Unit) {}
    ) {
        content()
    }
}

private fun Modifier.swipeNotificationToDismiss(
    gestureEnabled: Boolean,
    onDismiss: () -> Unit,
    onOffsetChange: ((Int) -> Unit)? = null,
    swipeToDismissTriggerDistance: Dp,
) = if (!gestureEnabled) this else composed {
    val density = LocalDensity.current
    var curJob: Job? = remember { null }
    var offset by remember { mutableStateOf(0) }
    var dismissed by remember { mutableStateOf(false) }
    val curOnDragOffsetChange by rememberUpdatedState(onOffsetChange)

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
            enabled = !dismissed,
            onDragStarted = {
                curJob?.cancel()
            },
            onDragStopped = {
                if (offset <= with(density) { -swipeToDismissTriggerDistance.toPx() }) {
                    dismissed = true
                    onDismiss()
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

object VisibilityContainerDefaults {

    val scrimColor: Color
        @Composable
        get() = MaterialTheme.colors.onSurface.copy(alpha = 0.32f)

    val Elevation = 24.dp

    val VisibilityContainerProperties = VisibilityContainerProperties()

    val ScrimEnterTransition = fadeIn(animationSpec = tween())

    val ScrimExitTransition = fadeOut(animationSpec = tween())

    @OptIn(ExperimentalAnimationApi::class)
    val DialogEnterTransition = run {
        scaleIn(
            animationSpec = spring(
                stiffness = 2000f
            ),
            initialScale = 0.9f
        )
    }

    @OptIn(ExperimentalAnimationApi::class)
    val DialogExitTransition = run {
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
    }

    val BottomSheetEnterTransition = slideInVertically { it }

    val BottomSheetExitTransition = slideOutVertically { it }

    val NotificationEnterTransition = slideInVertically { -it }

    val NotificationExitTransition = slideOutVertically { -it }
}