package com.aaron.fastcompose

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.aaron.compose.ktx.rememberEventBusEvent
import com.aaron.fastcompose.ui.theme.FastComposeTheme
import kotlinx.coroutines.delay
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import java.util.*

class SecondActivity : ComponentActivity() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, SecondActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FastComposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val helloEvent by rememberEventBusEvent(AnotherEvent::class, true)
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = helloEvent?.text ?: "SecondActivity")
                    }

                    val activity = LocalContext.current as Activity
                    LaunchedEffect(Unit) {
                        delay(2000)
                        val date = Date(System.currentTimeMillis())
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)
                        EventBus.getDefault().post(HelloEvent("SecondActivity: ${sdf.format(date)}"))
                        activity.finish()
                    }
                }
            }
        }
    }
}

@Composable
private fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FastComposeTheme {
        Greeting("Android")
    }
}