package com.aaron.compose.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.io.Serializable

/**
 * Compose 壳
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
 * ```
 * class ShoppingScreen : ComposeUI {
 *
 *     companion object {
 *         fun start(context: Context) {
 *             ComposeUIScope.start(context, ShoppingScreen(), **Activity::class.java)
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
        var debug = false
    }

    @Composable
    fun MainContent() {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                Content()
            }
            if (debug) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = Color.Black)
                        .padding(
                            vertical = 8.dp,
                            horizontal = 16.dp
                        ),
                    text = this@ComposeUI::class.java.name,
                    color = Color.White
                )
            }
        }
    }

    @Composable
    fun Content()
}