package com.aaron.fastcompose

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * @author DS-Z
 * @since 2022/8/2
 */
class HelloEvent(val text: String)

@Composable
fun HelloEvent2(content: @Composable (HelloEvent?) -> Unit) {
    Log.d("zzx", "1")
    var _event: HelloEvent? by remember {
        mutableStateOf(null)
    }
    Log.d("zzx", "2")
    val subscriber = remember {
        object : Any() {
            @Subscribe(threadMode = ThreadMode.MAIN)
            fun onHelloEvent(event: HelloEvent) {
                _event = event
            }
        }
    }
    Log.d("zzx", "3")
    EventBusComponent(subscriber = subscriber) {
        Log.d("zzx", "4")
        content(_event)
    }
    Log.d("zzx", "5")
}