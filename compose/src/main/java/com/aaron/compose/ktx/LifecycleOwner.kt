package com.aaron.compose.ktx

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.aaron.compose.ktx.ComposeLifecycleEvent.OnCreate
import com.aaron.compose.ktx.ComposeLifecycleEvent.OnDestroy
import com.aaron.compose.ktx.ComposeLifecycleEvent.OnEnter
import com.aaron.compose.ktx.ComposeLifecycleEvent.OnExit
import com.aaron.compose.ktx.ComposeLifecycleEvent.OnPause
import com.aaron.compose.ktx.ComposeLifecycleEvent.OnResume
import com.aaron.compose.ktx.ComposeLifecycleEvent.OnStart
import com.aaron.compose.ktx.ComposeLifecycleEvent.OnStop

@Composable
fun ComposeLifecycleObserver(listener: ComposeLifecycleListener) {
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        owner.listener(OnEnter)
        val observer = LifecycleEventObserver { _owner, event ->
            when (event) {
                Event.ON_CREATE -> _owner.listener(OnCreate)
                Event.ON_START -> _owner.listener(OnStart)
                Event.ON_RESUME -> _owner.listener(OnResume)
                Event.ON_PAUSE -> _owner.listener(OnPause)
                Event.ON_STOP -> _owner.listener(OnStop)
                Event.ON_DESTROY -> _owner.listener(OnDestroy)
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

private typealias ComposeLifecycleListener = LifecycleOwner.(event: ComposeLifecycleEvent) -> Unit

enum class ComposeLifecycleEvent {

    OnEnter, OnExit, OnCreate, OnStart, OnResume, OnPause, OnStop, OnDestroy
}