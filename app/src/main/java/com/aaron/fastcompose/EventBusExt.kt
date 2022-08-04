package com.aaron.fastcompose

import android.util.Log
import androidx.compose.runtime.Composable
import com.aaron.compose.component.RegistryComponent
import org.greenrobot.eventbus.EventBus

@Composable
fun EventBusComponent(subscriber: Any, content: @Composable () -> Unit) {
    RegistryComponent(
        register = {
            Log.d("zzx", "register: $subscriber")
            EventBus.getDefault().register(subscriber)
        },
        unregister = {
            Log.d("zzx", "unregister: $subscriber")
            EventBus.getDefault().unregister(subscriber)
        },
        content = content
    )
}