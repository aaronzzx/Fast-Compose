package com.aaron.fastcompose

import androidx.compose.runtime.Composable
import com.aaron.compose.base.BaseComposeActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/12/28
 */
@AndroidEntryPoint
class MainActivity : BaseComposeActivity() {

    @Composable
    override fun Content() {
        FastComposeApp()
    }
}