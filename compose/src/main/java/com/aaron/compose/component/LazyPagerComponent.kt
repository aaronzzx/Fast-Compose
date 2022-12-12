package com.aaron.compose.component

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.withStateAtLeast
import com.aaron.compose.ktx.currentPageDelayed
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerDefaults
import com.google.accompanist.pager.PagerScope
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.filter

/**
 * 懒加载 HorizontalPager Component
 */
@Composable
fun LazyPagerComponent(
    components: ImmutableList<LazyComponent>,
    modifier: Modifier = Modifier,
    activeState: Lifecycle.State = Lifecycle.State.RESUMED,
    pagerState: PagerState = rememberPagerState(),
    reverseLayout: Boolean = false,
    itemSpacing: Dp = 0.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    flingBehavior: FlingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        endContentPadding = contentPadding.calculateEndPadding(LayoutDirection.Ltr),
    ),
    key: ((page: Int) -> Any)? = null,
    userScrollEnabled: Boolean = true,
    content: @Composable PagerScope.(page: Int) -> Unit
) {
    val saveableStateHolder = rememberSaveableStateHolder()
    HorizontalPager(
        count = components.size,
        modifier = modifier,
        state = pagerState,
        reverseLayout = reverseLayout,
        itemSpacing = itemSpacing,
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
                        component.initialize()
                    }
            }
            content(page)
        }
    }
}