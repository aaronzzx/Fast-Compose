package com.aaron.compose.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * 处理注册、解注册的组件
 */
@Composable
fun RegistryComponent(
    register: () -> Unit,
    unregister: () -> Unit,
    content: @Composable () -> Unit
) {
    LifecycleComponent(
        onEnterCompose = {
            register()
        },
        onExitCompose = {
            unregister()
        },
        content = content
    )
}

/**
 * 感知生命周期组件
 */
@Composable
fun LifecycleComponent(
    onEnterCompose: (() -> Unit)? = null,
    onExitCompose: (() -> Unit)? = null,
    onCreate: (() -> Unit)? = null,
    onStart: (() -> Unit)? = null,
    onResume: (() -> Unit)? = null,
    onPause: (() -> Unit)? = null,
    onStop: (() -> Unit)? = null,
    onDestroy: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    LifecycleComponent(
        listener = object : ComposeLifecycleListener {
            override fun onEnterCompose(owner: LifecycleOwner) {
                super.onEnterCompose(owner)
                onEnterCompose?.invoke()
            }

            override fun onExitCompose(owner: LifecycleOwner) {
                super.onExitCompose(owner)
                onExitCompose?.invoke()
            }

            override fun onCreate(owner: LifecycleOwner) {
                super.onCreate(owner)
                onCreate?.invoke()
            }

            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                onStart?.invoke()
            }

            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                onResume?.invoke()
            }

            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                onPause?.invoke()
            }

            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)
                onStop?.invoke()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                onDestroy?.invoke()
            }
        },
        content = content
    )
}

/**
 * 感知生命周期组件
 */
@Composable
fun LifecycleComponent(
    listener: ComposeLifecycleListener,
    content: @Composable () -> Unit
) {
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