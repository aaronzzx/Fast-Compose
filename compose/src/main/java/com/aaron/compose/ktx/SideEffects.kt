package com.aaron.compose.ktx

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.util.Consumer

/**
 * @author aaronzzxup@gmail.com
 * @since 2023/6/2
 */

@Composable
fun NewIntentEffect(block: (Intent?) -> Unit) {
    val context = LocalContext.current
    val curBlock by rememberUpdatedState(newValue = block)
    DisposableEffect(key1 = context) {
        val listener = Consumer<Intent> {
            curBlock(it)
        }
        val activity = context.findGenericActivity<ComponentActivity>()
        activity?.addOnNewIntentListener(listener)
        onDispose {
            activity?.removeOnNewIntentListener(listener)
        }
    }
}