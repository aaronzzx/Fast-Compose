package com.aaron.fastcompose

import android.app.Application
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aaron.compose.base.BaseComposeDefaults
import com.aaron.fastcompose.ui.theme.FastComposeTheme

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/7/30
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        BaseComposeDefaults.entrance = { content ->
            FastComposeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    elevation = 4.dp
                ) {
                    content()
                }
            }
        }
    }
}