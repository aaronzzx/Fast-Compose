package com.aaron.compose.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aaron.compose.base.ComposeUIScope.Companion.start
import com.aaron.compose.ktx.toDp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.io.Serializable

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
            var debugBarHeight by remember {
                mutableStateOf(0.dp)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = debugBarHeight)
            ) {
                Content()
            }

            if (debug) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    val density = LocalDensity.current

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                WindowInsets.statusBars
                                    .getTop(density)
                                    .toDp()
                            )
                            .background(color = Color.White)
                    )

                    Text(
                        modifier = Modifier
                            .onGloballyPositioned {
                                debugBarHeight = density.run { it.size.height.toDp() }
                            }
                            .fillMaxWidth()
                            .background(color = Color.White)
                            .padding(horizontal = 16.dp),
                        text = this@ComposeUI::class.java.name,
                        color = MaterialTheme.colors.primary,
                        fontSize = 12.sp
                    )
                }
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