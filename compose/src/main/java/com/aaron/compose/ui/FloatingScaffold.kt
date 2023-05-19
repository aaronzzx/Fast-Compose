package com.aaron.compose.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.aaron.compose.utils.noLocalProvidedFor
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * @author aaronzzxup@gmail.com
 * @since 2023/5/17
 */

@Composable
fun FloatingScaffold(
    modifier: Modifier = Modifier,
    scrimColor: Color = MaterialTheme.colors.onSurface.copy(alpha = 0.32f),
    scrimEnter: EnterTransition = fadeIn(animationSpec = tween()),
    scrimExit: ExitTransition = fadeOut(animationSpec = tween()),
    floating: @Composable FloatingScaffoldScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    CompositionLocalProvider(
        LocalFloatingTotal providesDefault remember { FloatingTotal() }
    ) {
        Box(modifier = modifier) {
            content()

            val floatingTotal = LocalFloatingTotal.current
            AnimatedVisibility(
                modifier = Modifier
                    .zIndex(floatingTotal.zIndexForScrim())
                    .matchParentSize(),
                visible = floatingTotal.hasFloating,
                enter = scrimEnter,
                exit = scrimExit
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(color = scrimColor)
                )
            }

            remember { FloatingScaffoldScopeImpl(this) }.floating()
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FloatingScaffoldScope.Dialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    properties: FloatingElementProperties = FloatingElementProperties(),
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
    backgroundColor: Color = MaterialTheme.colors.surface,
    shape: Shape = RoundedCornerShape(4.dp),
    elevation: Dp = 24.dp,
    content: @Composable () -> Unit
) {
    FloatingElement(
        modifier = modifier,
        visible = visible,
        onDismiss = onDismiss,
        properties = properties
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .width(280.dp)
                .animateEnterExit(enter, exit, label),
            color = backgroundColor,
            shape = shape,
            elevation = if (hasFocus) elevation else 0.dp
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FloatingScaffoldScope.BottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    properties: FloatingElementProperties = FloatingElementProperties(),
    enter: EnterTransition = slideInVertically { it },
    exit: ExitTransition = slideOutVertically { it },
    label: String = "BottomSheet",
    backgroundColor: Color = MaterialTheme.colors.surface,
    shape: Shape = RectangleShape,
    elevation: Dp = 24.dp,
    content: @Composable () -> Unit
) {
    FloatingElement(
        modifier = modifier,
        visible = visible,
        onDismiss = onDismiss,
        properties = properties
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .animateEnterExit(enter, exit, label),
            color = backgroundColor,
            shape = shape,
            elevation = if (hasFocus) elevation else 0.dp
        ) {
            content()
        }
    }
}

data class FloatingElementProperties(
    val dismissOnBackPress: Boolean = true,
    val dismissOnClickOutside: Boolean = true
)

@Stable
interface FloatingScaffoldScope : BoxScope {

    @Composable
    fun FloatingElement(
        visible: Boolean,
        onDismiss: () -> Unit,
        modifier: Modifier = Modifier,
        properties: FloatingElementProperties = FloatingElementProperties(),
        content: @Composable FloatingElementScope.() -> Unit
    ) {
    }
}

private class FloatingScaffoldScopeImpl(
    boxScope: BoxScope
) : FloatingScaffoldScope, BoxScope by boxScope {

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    override fun FloatingElement(
        visible: Boolean,
        onDismiss: () -> Unit,
        modifier: Modifier,
        properties: FloatingElementProperties,
        content: @Composable FloatingElementScope.() -> Unit
    ) {
        val floatingTotal = LocalFloatingTotal.current
        val uniqueId = remember {
            UUID.randomUUID().toString()
        }

        val curOnDismiss by rememberUpdatedState(onDismiss)

        BackHandler(properties.dismissOnBackPress && visible) {
            curOnDismiss()
        }

        AnimatedVisibility(
            modifier = modifier.zIndex(floatingTotal.zIndex(uniqueId)),
            visible = visible,
            enter = EnterTransition.None,
            exit = ExitTransition.None,
            label = "FloatingElement"
        ) {
            LaunchedEffect(floatingTotal, transition) {
                snapshotFlow { transition.targetState }
                    .map { it == EnterExitState.Visible }
                    .collect { visible ->
                        if (visible) {
                            floatingTotal.add(uniqueId)
                        } else {
                            floatingTotal.remove(uniqueId)
                        }
                    }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures {
                            if (properties.dismissOnClickOutside) {
                                curOnDismiss()
                            }
                        }
                    }
            ) {
                val floatingElementScope = remember {
                    FloatingElementScopeImpl(
                        animatedVisibilityScope = this@AnimatedVisibility,
                        boxScope = this
                    ) {
                        floatingTotal.hasFocus(uniqueId)
                    }
                }
                floatingElementScope.content()
            }
        }
    }
}

@Stable
interface FloatingElementScope : AnimatedVisibilityScope, BoxScope {

    val hasFocus: Boolean
}

private class FloatingElementScopeImpl(
    animatedVisibilityScope: AnimatedVisibilityScope,
    boxScope: BoxScope,
    val onHasFocus: () -> Boolean
) : FloatingElementScope,
    BoxScope by boxScope,
    AnimatedVisibilityScope by animatedVisibilityScope {

    override val hasFocus: Boolean
        get() = onHasFocus()
}

private val LocalFloatingTotal = staticCompositionLocalOf<FloatingTotal> {
    noLocalProvidedFor("LocalFloatingTotal")
}

private class FloatingTotal {

    val visibleBucket = mutableStateListOf<String>()

    val visibleCount: Int by derivedStateOf { visibleBucket.size }

    val hasFloating: Boolean by derivedStateOf { visibleCount > 0 }

    fun hasFocus(id: String): Boolean {
        return visibleBucket.lastOrNull() == id
    }

    fun zIndex(id: String): Float {
        val index = visibleBucket.indexOf(id)
        return index.toFloat().coerceAtLeast(0f)
    }

    fun zIndexForScrim(): Float {
        return (visibleCount - 1f).coerceAtLeast(0f)
    }

    fun add(id: String) {
        visibleBucket.add(id)
    }

    fun remove(id: String) {
        visibleBucket.remove(id)
    }
}