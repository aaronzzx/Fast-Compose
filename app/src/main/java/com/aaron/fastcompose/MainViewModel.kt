package com.aaron.fastcompose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.aaron.compose.ui.SmartRefreshType

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/9/17
 */
class MainViewModel : ViewModel() {

    var refreshType: SmartRefreshType by mutableStateOf(SmartRefreshType.Refresh())
}