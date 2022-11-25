package com.aaron.compose.ktx

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.aaron.compose.component.ComposeLifecycleComponent
import kotlinx.coroutines.CoroutineScope

/**
 * 可根据生命周期状态自动启动与取消
 */
@Composable
@NonRestartableComposable
fun LaunchedLifecycleEffect(
    state: Lifecycle.State,
    key1: Any?,
    block: suspend CoroutineScope.() -> Unit
) {
    ComposeLifecycleComponent {
        LaunchedEffect(key1) {
            repeatOnLifecycle(state) {
                block()
            }
        }
    }
}

@Composable
@NonRestartableComposable
fun LaunchedLifecycleEffect(
    state: Lifecycle.State,
    vararg key: Any?,
    block: suspend CoroutineScope.() -> Unit
) {
    ComposeLifecycleComponent {
        LaunchedEffect(*key) {
            repeatOnLifecycle(state) {
                block()
            }
        }
    }
}