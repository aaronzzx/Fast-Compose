package com.aaron.compose.base

import androidx.lifecycle.ViewModel
import com.aaron.compose.component.LoadingComponent
import com.aaron.compose.safestate.SafeState
import com.aaron.compose.safestate.SafeStateScope
import com.aaron.compose.safestate.safeStateOf

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/11/11
 */
abstract class BaseComposeVM : ViewModel(), LoadingComponent, SafeStateScope {

    override val loading: SafeState<Boolean> = safeStateOf(false)
}