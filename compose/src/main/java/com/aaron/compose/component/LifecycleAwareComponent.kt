package com.aaron.compose.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * 感知生命周期
 */
@Composable
fun LifecycleAwareComponent(
    listener: ComposeLifecycleListener? = null,
    content: @Composable () -> Unit
) {
    listener?.also {
        val owner = LocalLifecycleOwner.current

        DisposableEffect(Unit) {
            listener.onEnterCompose(owner)
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_CREATE -> listener.onCreate(owner)
                    Lifecycle.Event.ON_START -> listener.onStart(owner)
                    Lifecycle.Event.ON_RESUME -> listener.onResume(owner)
                    Lifecycle.Event.ON_PAUSE -> listener.onPause(owner)
                    Lifecycle.Event.ON_STOP -> listener.onStop(owner)
                    Lifecycle.Event.ON_DESTROY -> listener.onDestroy(owner)
                    else -> Unit
                }
            }

            owner.lifecycle.addObserver(observer)

            onDispose {
                listener.onExitCompose(owner)
                owner.lifecycle.removeObserver(observer)
            }
        }
    }

    content()
}

interface ComposeLifecycleListener {

    /**
     * 首次进入 Compose
     */
    fun onEnterCompose(owner: LifecycleOwner) = Unit

    /**
     * 退出 Compose
     */
    fun onExitCompose(owner: LifecycleOwner) = Unit

    /**
     * onCreate
     */
    fun onCreate(owner: LifecycleOwner) = Unit

    /**
     * onStart
     */
    fun onStart(owner: LifecycleOwner) = Unit

    /**
     * onResume
     */
    fun onResume(owner: LifecycleOwner) = Unit

    /**
     * onPause
     */
    fun onPause(owner: LifecycleOwner) = Unit

    /**
     * onStop
     */
    fun onStop(owner: LifecycleOwner) = Unit

    /**
     * onDestroy
     */
    fun onDestroy(owner: LifecycleOwner) = Unit
}