package com.aaron.fastcompose

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.aaron.compose.component.BaseComposeActivity
import com.aaron.compose.onClick
import com.aaron.fastcompose.theme.ComposeTheme
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : BaseComposeActivity() {

    @Composable
    override fun Content() {
        BackHandler {
            finish()
        }
        ComposeTheme(false) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                color = MaterialTheme.colors.background
            ) {
                Column {
                    kotlin.run {
                        var name by remember {
                            mutableStateOf("MainActivity")
                        }
                        HelloEvent {
                            name = it.text
                        }
                        Greeting(name = name)
                    }

                    HelloEvent2 {
                        val name = "HelloEvent2-${it?.text ?: "MainActivity"}"
                        Greeting(name = name)
                    }

                    val activity = LocalContext.current
                    Box(
                        modifier = Modifier
                            .background(Color.Red.copy(0.5f))
                            .fillMaxSize()
                            .onClick {
                                SecondActivity.start(activity)
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun Greeting(name: String) {
    Text(
        text = "Hello $name!"
    )
}

@Composable
private fun HelloEvent(content: (HelloEvent) -> Unit) {
    Log.d("zzx", "1")
    val currentContent by rememberUpdatedState(newValue = content)
    val subscriber = remember {
        object : Any() {
            @Subscribe(threadMode = ThreadMode.MAIN)
            fun onHelloEvent(event: HelloEvent) {
                Log.d("zzx", "2")
                currentContent(event)
            }
        }
    }
    Log.d("zzx", "3")
    EventBusComponent(subscriber = subscriber)
    Log.d("zzx", "4")
}