package com.aaron.compose.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.withStateAtLeast
import com.aaron.compose.safestate.SafeState
import com.aaron.compose.safestate.safeStateOf
import kotlinx.coroutines.flow.filter

/**
 * 懒加载 Component
 *
 * @param activeState 开始加载的生命周期状态
 */
@Composable
fun LazyComponent(
    component: LazyComponent,
    modifier: Modifier = Modifier,
    activeState: Lifecycle.State = Lifecycle.State.RESUMED,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        val owner = LocalLifecycleOwner.current
        LaunchedEffect(key1 = component) {
            owner.withStateAtLeast(activeState) {}
            snapshotFlow { component.initialized.value }
                .filter { !it }
                .collect {
                    component.initialized.setValueInternal(true)
                    component.initialize()
                }
        }
        content()
    }
}

/**
 * 懒加载，由 ViewModel 实现接口，在 [initialize] 中处理初始化逻辑。
 */
@Stable
interface LazyComponent {

    /**
     * 是否已经初始化
     */
    val initialized: SafeState<Boolean>

    fun initialize()
}

/**
 * 用于 Compose 预览时提供的参数占位
 */
fun lazyComponent() = object : LazyComponent {
    override val initialized: SafeState<Boolean> = safeStateOf(false)

    override fun initialize() {
        initialized.setValueInternal(true)
    }
}