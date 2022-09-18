package com.aaron.fastcompose

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import com.aaron.compose.ui.SmartRefreshState
import com.aaron.compose.ui.SmartRefreshType

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/9/17
 */
@Stable
class MainVM : ViewModel() {

    val refreshState = SmartRefreshState(false)
}