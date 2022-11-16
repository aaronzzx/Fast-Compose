package com.aaron.compose.ktx

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.jialai.compose.component.ComposeLifecycleComponent
import kotlinx.coroutines.CoroutineScope

/**
 * 可根据生命周期状态自动启动与取消
 */
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