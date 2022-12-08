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
fun LazyComponent(
    component: LazyComponent,
    modifier: Modifier = Modifier,
    activeState: Lifecycle.State = Lifecycle.State.RESUMED,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        val owner = LocalLifecycleOwner.current
        LaunchedEffect(key1 = component) {
            owner.withStateAtLeast(activeState) {
                if (component.initialized.value.not()) {
                    component.initialized.setValueInternal(true)
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
interface LazyComponent {

    /**
     * 是否已经初始化
     */
    val initialized: SafeState<Boolean>

    fun initialize()
}

@Composable
fun lazyLoadComponent() = object : LazyComponent {
    override val initialized: SafeState<Boolean> = safeStateOf(false)

    override fun initialize() {
        initialized.setValueInternal(true)
    }
}