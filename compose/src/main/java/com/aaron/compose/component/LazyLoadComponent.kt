package com.aaron.compose.component

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.withStateAtLeast
import com.aaron.compose.ktx.currentPageDelayed
import com.aaron.compose.safestate.SafeState
import com.aaron.compose.safestate.safeStateOf
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.flow.filter

/**
 * 懒加载 Component
 */
@Composable
fun LazyLoadComponent(
    component: LazyLoadComponent,
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

@Composable
fun LazyLoadPagerComponent(
    component: LazyLoadComponent,
    page: Int,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    activeState: Lifecycle.State = Lifecycle.State.RESUMED,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        val curPage by pagerState.currentPageDelayed()
        val owner = LocalLifecycleOwner.current
        LaunchedEffect(key1 = component) {
            owner.withStateAtLeast(activeState) {}
            snapshotFlow { curPage }
                .filter { !component.initialized.value }
                .filter { page == curPage }
                .collect {
                    component.initialized.setValueInternal(true)
                    component.initialize()
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
    val initialized: SafeState<Boolean>

    fun initialize()
}

@Composable
fun lazyLoadComponent() = object : LazyLoadComponent {
    override val initialized: SafeState<Boolean> = safeStateOf(false)

    override fun initialize() {
        initialized.setValueInternal(true)
    }
}