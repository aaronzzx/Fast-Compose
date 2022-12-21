package com.aaron.compose.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun VisibilityScrimContainer(
    modifier: Modifier = Modifier,
    state: VisibilityContainerState = rememberVisibilityContainerState(),
    scrimColor: Color = VisibilityContainerDefaults.scrimColor,
    scrimEnter: EnterTransition = ScrimEnterTransition,
    scrimExit: ExitTransition = ScrimExitTransition,
    properties: VisibilityContainerProperties = VisibilityContainerProperties,
    content: @Composable VisibilityContainerScope.() -> Unit
) {
    BackHandler(enabled = state.visible) {
        if (properties.dismissOnBackPress) {
            state.hide()
        }
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
                    if (properties.dismissOnClickOutside) {
                        state.hide()
                    }
                },
                visible = state.visible
            )
        }
    ) {
        content()
    }
}

@Composable
fun VisibilityContainer(
    modifier: Modifier = Modifier,
    state: VisibilityContainerState = rememberVisibilityContainerState(),
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

@Composable
fun rememberVisibilityContainerState(
    initialVisible: Boolean = false
): VisibilityContainerState {
    return remember {
        VisibilityContainerState(initialVisible)
    }
}

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

@Composable
private fun Scrim(
    color: Color,
    onDismiss: () -> Unit,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    if (color.isSpecified) {
        val dismissModifier = if (visible) {
            Modifier.pointerInput(onDismiss) { detectTapGestures { onDismiss() } }
        } else {
            Modifier
        }

        Canvas(
            modifier
                .fillMaxSize()
                .then(dismissModifier)
        ) {
            drawRect(color = color)
        }
    }
}

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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun VisibilityContainerScope.Dialog(
    modifier: Modifier = Modifier,
    enter: EnterTransition = DialogEnterTransition,
    exit: ExitTransition = DialogExitTransition,
    backgroundColor: Color = MaterialTheme.colors.surface,
    shape: Shape = RoundedCornerShape(4.dp),
    elevation: Dp = Elevation,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun VisibilityContainerScope.BottomSheet(
    modifier: Modifier = Modifier,
    enter: EnterTransition = BottomSheetEnterTransition,
    exit: ExitTransition = BottomSheetExitTransition,
    backgroundColor: Color = MaterialTheme.colors.surface,
    shape: Shape = RectangleShape,
    elevation: Dp = Elevation,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
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
    statusPaddingEnabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    backgroundColor: Color = MaterialTheme.colors.surface,
    shape: Shape = RoundedCornerShape(4.dp),
    elevation: Dp = Elevation,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
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
            .let {
                if (!statusPaddingEnabled) it else {
                    it.statusBarsPadding()
                }
            }
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