package com.aaron.compose.component

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.withStateAtLeast
import com.aaron.compose.safestate.SafeState
import com.aaron.compose.safestate.safeStateOf

/**
 * 懒加载 Component
 */
@Composable
fun LazyLoadComponent(
    component: LazyLoadComponent,
    modifier: Modifier = Modifier,
    state: Lifecycle.State = Lifecycle.State.RESUMED,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        val owner = LocalLifecycleOwner.current
        LaunchedEffect(key1 = component) {
            owner.withStateAtLeast(state) {
                if (component.isInitialized.value.not()) {
                    component.isInitialized.setValueInternal(true)
                    component.initialize()
                }
            }
        }
        content()
    }
}

/**
 * 懒加载
 */
@Stable
interface LazyLoadComponent {

    /**
     * 是否已经初始化
     */
    val isInitialized: SafeState<Boolean>

    fun initialize()
}

@Composable
fun lazyLoadComponent() = object : LazyLoadComponent {
    override val isInitialized: SafeState<Boolean> = safeStateOf(false)

    override fun initialize() {
        isInitialized.setValueInternal(true)
    }
}