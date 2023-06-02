package com.aaron.compose.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.aaron.compose.ktx.currentPageDelayed
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
    enabled: Boolean = true,
    activeState: Lifecycle.State = Lifecycle.State.RESUMED,
    pagerInfo: Pair<PagerState, Int>? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        if (!enabled) {
            content()
        } else {
            val owner = LocalLifecycleOwner.current
            if (pagerInfo != null) {
                val (pagerState, page) = pagerInfo
                val curPage = pagerState.currentPageDelayed()
                LaunchedEffect(component, pagerState, page) {
                    owner.repeatOnLifecycle(activeState) {
                        snapshotFlow { curPage.value to component.initialized.value }
                            .filter { (curPageVal, initializedVal) ->
                                curPageVal == page && !initializedVal
                            }
                            .collect {
                                component.initialized.setValueInternal(true)
                                component.initialize()
                            }
                    }
                }
            } else {
                LaunchedEffect(key1 = component) {
                    owner.repeatOnLifecycle(activeState) {
                        snapshotFlow { component.initialized.value }
                            .filter { initialized ->
                                !initialized
                            }
                            .collect {
                                component.initialized.setValueInternal(true)
                                component.initialize()
                            }
                    }
                }
            }
            content()
        }
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

    fun markAsUninitialized() {
        initialized.setValueInternal(false)
    }
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