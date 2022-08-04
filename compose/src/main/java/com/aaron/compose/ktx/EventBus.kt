package com.aaron.compose.ktx

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.reflect.KClass

@Composable
fun <T : Any> rememberEventBusEvent(clazz: KClass<T>, sticky: Boolean = false): State<T?> {
    val event = remember {
        mutableStateOf<T?>(null)
    }
    EventBusHandler(clazz, sticky) {
        event.value = it
    }
    return event
}

@Composable
fun <T : Any> EventBusHandler(
    clazz: KClass<T>,
    sticky: Boolean = false,
    onEvent: (T) -> Unit
) {
    val subscriber = remember {
        createSubscriber(clazz, sticky, onEvent)
    }
    DisposableEffect(subscriber) {
        EventBus.getDefault().register(subscriber)
        onDispose {
            EventBus.getDefault().unregister(subscriber)
        }
    }
}

private fun <T : Any> createSubscriber(
    clazz: KClass<T>,
    sticky: Boolean = false,
    onEvent: (T) -> Unit
): Any {
    return if (sticky) {
        object : Any() {
            @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
            fun onAnyEvent(event: Any) {
                if (event::class == clazz) {
                    onEvent(event as T)
                }
            }
        }
    } else {
        object : Any() {
            @Subscribe(threadMode = ThreadMode.MAIN, sticky = false)
            fun onAnyEvent(event: Any) {
                if (event::class == clazz) {
                    onEvent(event as T)
                }
            }
        }
    }
}