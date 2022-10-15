package com.aaron.compose.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleOwner
import com.aaron.compose.utils.ComposeLifecycleEvent
import com.aaron.compose.utils.ComposeLifecycleObserver

/**
 * 监听生命周期，顺便可以调用其他 Composable 函数
 */
@Composable
fun ComposeLifecycleComponent(content: @Composable LifecycleOwner.(ComposeLifecycleEvent) -> Unit) {
    var event by remember {
        mutableStateOf(ComposeLifecycleEvent.OnEnter)
    }
    ComposeLifecycleObserver {
        event = it
    }
    LocalLifecycleOwner.current.content(event)
}