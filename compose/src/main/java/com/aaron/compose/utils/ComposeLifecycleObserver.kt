package com.aaron.compose.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.aaron.compose.utils.ComposeLifecycleEvent.OnCreate
import com.aaron.compose.utils.ComposeLifecycleEvent.OnDestroy
import com.aaron.compose.utils.ComposeLifecycleEvent.OnEnter
import com.aaron.compose.utils.ComposeLifecycleEvent.OnExit
import com.aaron.compose.utils.ComposeLifecycleEvent.OnPause
import com.aaron.compose.utils.ComposeLifecycleEvent.OnResume
import com.aaron.compose.utils.ComposeLifecycleEvent.OnStart
import com.aaron.compose.utils.ComposeLifecycleEvent.OnStop

/**
 * 监听 Compose 生命周期。
 */
@Composable
fun ComposeLifecycleObserver(listener: LifecycleOwner.(event: ComposeLifecycleEvent) -> Unit) {
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        owner.listener(OnEnter)
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Event.ON_CREATE -> owner.listener(OnCreate)
                Event.ON_START -> owner.listener(OnStart)
                Event.ON_RESUME -> owner.listener(OnResume)
                Event.ON_PAUSE -> owner.listener(OnPause)
                Event.ON_STOP -> owner.listener(OnStop)
                Event.ON_DESTROY -> owner.listener(OnDestroy)
                else -> Unit
            }
        }
        owner.lifecycle.addObserver(observer)
        onDispose {
            owner.listener(OnExit)
            owner.lifecycle.removeObserver(observer)
        }
    }
}

enum class ComposeLifecycleEvent {

    OnEnter, OnExit, OnCreate, OnStart, OnResume, OnPause, OnStop, OnDestroy
}