package com.aaron.compose.base

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/7/30
 */
abstract class BaseComposeActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init(savedInstanceState)
        setContent {
            MainContent()
        }
    }

    protected open fun init(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    @Composable
    protected open fun MainContent() {
        Content()
    }

    @Composable
    protected abstract fun Content()
}