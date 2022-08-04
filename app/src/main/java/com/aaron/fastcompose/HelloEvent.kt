package com.aaron.fastcompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * @author DS-Z
 * @since 2022/8/2
 */
class HelloEvent(val text: String)

@Composable
fun observeHelloEvent(): State<HelloEvent?> {
    val _event: MutableState<HelloEvent?> = remember {
        mutableStateOf(null)
    }
    val subscriber = remember {
        object : Any() {
            @Subscribe(threadMode = ThreadMode.MAIN)
            fun onHelloEvent(event: HelloEvent) {
                _event.value = event
            }
        }
    }
    DisposableEffect(Unit) {
        EventBus.getDefault().register(subscriber)
        onDispose {
            EventBus.getDefault().unregister(subscriber)
        }
    }
    return _event
}

@Composable
fun HelloEventObserver(content: @Composable (HelloEvent?) -> Unit) {
    var _event: HelloEvent? by remember {
        mutableStateOf(null)
    }
    val subscriber = remember {
        object : Any() {
            @Subscribe(threadMode = ThreadMode.MAIN)
            fun onHelloEvent(event: HelloEvent) {
                _event = event
            }
        }
    }
    EventBusComponent(subscriber = subscriber) {
        content(_event)
    }
}