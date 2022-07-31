package com.aaron.compose.ui

import android.content.Context
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed

/**
 * 创建一个被 Fragment 使用的 ComposeView
 */
fun FragmentComposeView(context: Context) = ComposeView(context).apply {
    setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
}