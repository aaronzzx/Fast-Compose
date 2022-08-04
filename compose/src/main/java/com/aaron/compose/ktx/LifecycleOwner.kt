package com.aaron.compose.ktx

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
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
fun LifecycleOwner.LifecycleObserver(listener: (ComposeLifecycleEvent) -> Unit) {
    val owner = this
    DisposableEffect(owner) {
        listener(OnEnter)
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Event.ON_CREATE -> listener(OnCreate)
                Event.ON_START -> listener(OnStart)
                Event.ON_RESUME -> listener(OnResume)
                Event.ON_PAUSE -> listener(OnPause)
                Event.ON_STOP -> listener(OnStop)
                Event.ON_DESTROY -> listener(OnDestroy)
                else -> Unit
            }
        }
        owner.lifecycle.addObserver(observer)
        onDispose {
            listener(OnExit)
            owner.lifecycle.removeObserver(observer)
        }
    }
}

enum class ComposeLifecycleEvent {

    OnEnter, OnExit, OnCreate, OnStart, OnResume, OnPause, OnStop, OnDestroy
}