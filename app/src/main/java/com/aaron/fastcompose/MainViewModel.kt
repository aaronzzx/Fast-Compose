package com.aaron.fastcompose

import androidx.lifecycle.ViewModel
import com.aaron.compose.ui.SmartRefreshState
import com.aaron.compose.ui.SmartRefreshType

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/9/17
 */
class MainViewModel : ViewModel() {

    val refreshState = SmartRefreshState(SmartRefreshType.Refreshing())
}