package com.aaron.compose.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.animation.core.animate
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aaron.compose.base.ComposeUIScope.Companion.start
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ktx.onSingleClick
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch
import java.io.Serializable
import kotlin.math.roundToInt

/**
 * Compose 壳，Activity 实现此接口后用 [composeUI] 获取 [ComposeUI]，
 * ComposeUI 通过 [start] 启动。
 *
 * @author aaronzzxup@gmail.com
 * @since 2023/3/13
 */
interface ComposeUIScope {

    companion object {
        private const val EXTRA_COMPOSE_UI = "EXTRA_COMPOSE_UI"

        fun start(
            context: Context,
            composeUI: ComposeUI,
            targetClass: Class<out Activity>,
            extras: (Intent.() -> Unit)? = null
        ) {
            val intent = Intent(context, targetClass).apply {
                putExtra(EXTRA_COMPOSE_UI, composeUI)
                extras?.invoke(this)
            }
            context.startActivity(intent)
        }

        fun getIntent(
            context: Context,
            composeUI: ComposeUI,
            targetClass: Class<out Activity>,
            extras: (Intent.() -> Unit)? = null
        ) : Intent {
            val intent = Intent(context, targetClass).apply {
                putExtra(EXTRA_COMPOSE_UI, composeUI)
                extras?.invoke(this)
            }
            return intent
        }
    }

    fun Activity.composeUI(): Lazy<ComposeUI?> {
        return lazy {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent?.getSerializableExtra(EXTRA_COMPOSE_UI, ComposeUI::class.java)
            } else {
                intent?.getSerializableExtra(EXTRA_COMPOSE_UI) as? ComposeUI
            }
        }
    }
}

/**
 * 实现此接口编写 Composable
 * ```
 * class ShoppingUI : ComposeUI {
 *
 *     companion object {
 *         fun start(context: Context) {
 *             ComposeUI.start(context, ShoppingUI())
 *         }
 *     }
 *
 *     @Composable
 *     fun Content() {
 *         // Write compose ui.
 *     }
 * }
 * ```
 */
interface ComposeUI : Serializable {

    companion object {
        var debug by mutableStateOf(false)
    }

    @Composable
    fun MainContent() {
        Box(modifier = Modifier.fillMaxSize()) {
            Content()

            if (debug) {
                DebugView(composeUIName = this@ComposeUI::class.java.name)
            }
        }

        val systemUiController = rememberSystemUiController()
        SideEffect {
            systemUiController.setStatusBarColor(
                color = Color.Transparent,
                darkIcons = true
            )
            systemUiController.setNavigationBarColor(
                color = Color.Transparent,
                darkIcons = true,
                navigationBarContrastEnforced = false
            )
        }
    }

    @Composable
    fun Content()
}

@Composable
private fun DebugView(composeUIName: String) {
    var showDebugDialog by remember {
        mutableStateOf(false)
    }

    if (showDebugDialog) {
        Dialog(onDismissRequest = { showDebugDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.TopCenter),
                    text = "DEBUG",
                    color = MaterialTheme.colors.primary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(),
                    text = composeUIName,
                    color = MaterialTheme.colors.primary,
                    fontSize = 14.sp
                )
            }
        }
    }

    val context = LocalContext.current
    val screenWidth = remember(context) {
        context.resources.displayMetrics.widthPixels
    }
    val screenHeight = remember(context) {
        context.resources.displayMetrics.heightPixels
    }
    val density = LocalDensity.current
    val size = 48.dp

    var offsetX by remember {
        mutableStateOf(screenWidth - density.run { size.toPx() })
    }
    var offsetY by remember {
        mutableStateOf(screenHeight * 0.4f)
    }
    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .size(size)
            .shadow(
                elevation = 4.dp,
                shape = CircleShape
            )
            .clipToBackground(
                color = MaterialTheme.colors.primary,
                shape = CircleShape
            )
            .run {
                val coroutineScope = rememberCoroutineScope()
                pointerInput(Unit) {
                    val sizePx = density.run { size.toPx() }
                    val xMax = screenWidth - sizePx
                    val yMax = screenHeight - sizePx
                    val fitEdge: () -> Unit = {
                        val fitStartEdge = offsetX < screenWidth / 2 - sizePx / 2
                        coroutineScope.launch {
                            animate(
                                initialValue = offsetX,
                                targetValue = if (fitStartEdge) 0f else xMax
                            ) { value, _ ->
                                offsetX = value
                            }
                        }
                    }
                    detectDragGestures(
                        onDragEnd = fitEdge,
                        onDragCancel = fitEdge
                    ) { change, dragAmount ->
                        change.consume()
                        offsetX = (offsetX + dragAmount.x).coerceIn(0f, xMax)
                        offsetY = (offsetY + dragAmount.y).coerceIn(0f, yMax)
                    }
                }
            }
            .onSingleClick {
                showDebugDialog = true
            }
            .padding(3.dp)
            .background(
                color = Color.White,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            imageVector = Icons.Default.Build,
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colors.primary)
        )
    }
}