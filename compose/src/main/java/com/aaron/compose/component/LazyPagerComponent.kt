package com.aaron.compose.component

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.snapping.SnapFlingBehavior
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.withStateAtLeast
import com.aaron.compose.ktx.currentPageDelayed
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.filter

/**
 * 懒加载 HorizontalPager Component ，仅在当前页时才会通知处理初始化。
 *
 * @param components 懒加载集合，每个 [LazyComponent] 自己处理初始化逻辑。
 * @param activeState 可以进行初始化的生命周期状态。
 */
@Composable
fun LazyPagerComponent(
    components: ImmutableList<LazyComponent>,
    modifier: Modifier = Modifier,
    activeState: Lifecycle.State = Lifecycle.State.RESUMED,
    pagerState: PagerState = rememberPagerState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    pageSize: PageSize = PageSize.Fill,
    beyondBoundsPageCount: Int = 0,
    pageSpacing: Dp = 0.dp,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    flingBehavior: SnapFlingBehavior = PagerDefaults.flingBehavior(state = pagerState),
    userScrollEnabled: Boolean = true,
    reverseLayout: Boolean = false,
    key: ((index: Int) -> Any)? = null,
    pageNestedScrollConnection: NestedScrollConnection = PagerDefaults.pageNestedScrollConnection(
        Orientation.Horizontal
    ),
    pageContent: @Composable (page: Int) -> Unit
) {
    val saveableStateHolder = rememberSaveableStateHolder()
    HorizontalPager(
        pageCount = components.size,
        modifier = modifier,
        state = pagerState,
        pageSize = pageSize,
        beyondBoundsPageCount = beyondBoundsPageCount,
        pageNestedScrollConnection = pageNestedScrollConnection,
        reverseLayout = reverseLayout,
        pageSpacing = pageSpacing,
        contentPadding = contentPadding,
        verticalAlignment = verticalAlignment,
        flingBehavior = flingBehavior,
        key = key,
        userScrollEnabled = userScrollEnabled
    ) { page ->
        saveableStateHolder.SaveableStateProvider(key = page) {
            val curPage by pagerState.currentPageDelayed()
            val component = components[page]
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
            pageContent(page)
        }
    }
}