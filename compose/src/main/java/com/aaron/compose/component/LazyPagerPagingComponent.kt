package com.aaron.compose.component

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.snapping.SnapFlingBehavior
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import com.aaron.compose.ktx.lazyPagingComponentAt
import com.aaron.compose.ktx.rememberLazyPagingComponents
import com.aaron.compose.paging.PagingScope
import com.aaron.compose.safestate.SafeState
import com.aaron.compose.safestate.SafeStateScope
import com.aaron.compose.safestate.safeStateOf
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * 多个页面的分页懒加载组件。
 *
 * @param component 分页逻辑处理主体。
 * @param activeState 开始初始化的声明周期状态。
 */
@Composable
fun <T, K, V> LazyPagerPagingComponent(
    component: LazyPagerPagingComponent<T, K, V>,
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
    pageContent: @Composable (lazyPagingComponent: LazyPagingComponent<K, V>) -> Unit
) {
    LazyPagerComponent(
        components = component.rememberLazyPagingComponents(),
        modifier = modifier,
        pageSize = pageSize,
        beyondBoundsPageCount = beyondBoundsPageCount,
        pageNestedScrollConnection = pageNestedScrollConnection,
        activeState = activeState,
        pagerState = pagerState,
        reverseLayout = reverseLayout,
        pageSpacing = pageSpacing,
        contentPadding = contentPadding,
        verticalAlignment = verticalAlignment,
        flingBehavior = flingBehavior,
        key = key,
        userScrollEnabled = userScrollEnabled
    ) { page ->
        val lazyPagingComponent = component.lazyPagingComponentAt(page) ?: return@LazyPagerComponent
        pageContent(lazyPagingComponent)
    }
}

/**
 * T 代表 Tab ，K 代表分页 key ，V 代表数据
 */
@Stable
interface LazyPagerPagingComponent<T, K, V> : PagingScope, SafeStateScope {

    val lazyPagingData: SafeState<LazyPagingData<T, K, V>>
}

/**
 * 用于 Compose 预览的参数占位。
 */
fun <T, K, V> lazyPagerPagingComponent(
    lazyPagingData: LazyPagingData<T, K, V> = persistentListOf()
): LazyPagerPagingComponent<T, K, V> = object : LazyPagerPagingComponent<T, K, V> {

    override val lazyPagingData: SafeState<LazyPagingData<T, K, V>> =
        safeStateOf(lazyPagingData)
}

typealias LazyPagingData<T, K, V> = ImmutableList<Pair<T, LazyPagingComponent<K, V>>>