package com.aaron.compose.component

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aaron.compose.ktx.isNotEmpty
import com.aaron.compose.ktx.onClick
import com.aaron.compose.paging.LoadState
import com.aaron.compose.paging.PageData
import com.aaron.compose.ui.refresh.SmartRefreshState

/**
 * 分页
 */
@Composable
fun <K, V> PagingComponent(
    component: PagingComponent<K, V>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    refreshState: SmartRefreshState? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    footer: (@Composable (
        pagingComponent: PagingComponent<K, V>,
        pagingFooterType: PagingFooterType
    ) -> Unit)? = { pagingComponent, pagingFooterType ->
        rememberPagingComponentFooter<K, V>().Content(
            component = pagingComponent,
            footerType = pagingFooterType
        )
    },
    content: LazyListScope.(pageData: PageData<K, V>) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled
    ) {
        val pageData = component.pageData
        content(pageData)

        if (footer == null) {
            return@LazyColumn
        }

        val pagingFooterType = getPagingFooterType(component)
        if (pagingFooterType != PagingFooterType.Idle) {
            item(contentType = "PageComponentFooter") {
                footer(component, pagingFooterType)
            }
        }
    }
}

/**
 * 网格分页
 */
@Composable
fun <K, V> PagingGridComponent(
    component: PagingComponent<K, V>,
    columns: GridCells,
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    refreshState: SmartRefreshState? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    customFooter: Boolean = false,
    footer: @Composable (
        pagingComponent: PagingComponent<K, V>,
        pagingFooterType: PagingFooterType
    ) -> Unit = { pagingComponent, pagingFooterType ->
        rememberPagingComponentFooter<K, V>().Content(
            component = pagingComponent,
            footerType = pagingFooterType
        )
    },
    content: LazyGridScope.(pageData: PageData<K, V>) -> Unit
) {
    LazyVerticalGrid(
        columns = columns,
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        verticalArrangement = verticalArrangement,
        horizontalArrangement = horizontalArrangement,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled
    ) {
        val pageData = component.pageData
        content(pageData)

        if (customFooter) {
            return@LazyVerticalGrid
        }

        val pagingFooterType = getPagingFooterType(component)
        if (pagingFooterType != PagingFooterType.Idle) {
            item(
                span = {
                    GridItemSpan(maxLineSpan)
                },
                contentType = "PageComponentFooter"
            ) {
                footer(component, pagingFooterType)
            }
        }
    }
}

fun <K, V> getPagingFooterType(component: PagingComponent<K, V>): PagingFooterType {
    val pageData = component.pageData
    val loadMoreState = pageData.loadState.loadMore
    val waitingRefresh = loadMoreState is LoadState.Waiting
    val loadMore = loadMoreState is LoadState.Idle
            && !loadMoreState.noMoreData
            && pageData.isNotEmpty
    val loading = loadMoreState is LoadState.Loading
    val loadError = loadMoreState is LoadState.Error
    val noMoreData = loadMoreState.noMoreData
    
    return when {
        loading -> PagingFooterType.Loading
        loadMore -> PagingFooterType.LoadMore
        loadError -> PagingFooterType.LoadError
        noMoreData -> PagingFooterType.NoMoreData
        waitingRefresh -> PagingFooterType.WaitingRefresh
        else -> PagingFooterType.Idle
    }
}

enum class PagingFooterType {

    Idle, Loading, LoadMore, LoadError, NoMoreData, WaitingRefresh
}

@Composable
fun <K, V> rememberPagingComponentFooter(): PagingComponentFooter<K, V> {
    return remember {
        PagingComponentFooter()
    }
}

open class PagingComponentFooter<K, V> {

    @Composable
    open fun Content(component: PagingComponent<K, V>, footerType: PagingFooterType) {
        when (footerType) {
            PagingFooterType.Loading -> LoadingContent(component)
            PagingFooterType.LoadMore -> LoadMoreContent(component)
            PagingFooterType.LoadError -> LoadErrorContent(component)
            PagingFooterType.NoMoreData -> NoMoreDataContent(component)
            PagingFooterType.WaitingRefresh -> WaitingRefreshContent(component)
            else -> Unit
        }
    }
    
    @Composable
    open fun LoadingContent(component: PagingComponent<K, V>) {
        FooterText(
            text = "加载中...",
            component = component,
            footerType = PagingFooterType.Loading
        )
    }

    @Composable
    open fun LoadMoreContent(component: PagingComponent<K, V>) {
        FooterText(
            text = "点击加载更多",
            component = component,
            footerType = PagingFooterType.LoadMore
        )
    }

    @Composable
    open fun LoadErrorContent(component: PagingComponent<K, V>) {
        FooterText(
            text = "加载失败，点击重试",
            component = component,
            footerType = PagingFooterType.LoadError
        )
    }

    @Composable
    open fun NoMoreDataContent(component: PagingComponent<K, V>) {
        FooterText(
            text = "已经到底了",
            component = component,
            footerType = PagingFooterType.NoMoreData
        )
    }

    @Composable
    open fun WaitingRefreshContent(component: PagingComponent<K, V>) {
        FooterText(
            text = "等待刷新完成",
            component = component,
            footerType = PagingFooterType.WaitingRefresh
        )
    }

    @Composable
    open fun FooterText(
        text: String,
        component: PagingComponent<K, V>,
        footerType: PagingFooterType,
        modifier: Modifier = Modifier,
        fontSize: TextUnit = 12.sp,
        textColor: Color = Color(0xFF666666)
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .let {
                    when (footerType) {
                        PagingFooterType.LoadMore -> it.onClick(enableRipple = false) {
                            component.pagingLoadMore()
                        }
                        PagingFooterType.LoadError -> it.onClick(enableRipple = false) {
                            component.pagingRetry()
                        }
                        else -> it
                    }
                }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = fontSize,
                color = textColor
            )
        }
    }
}

@Stable
interface PagingComponent<K, V> : RefreshComponent {

    val pageData: PageData<K, V>

    override fun refreshIgnoreAnimation() {
        pagingRefresh()
    }

    fun pagingRefresh() {
        pageData.refresh()
    }

    fun pagingLoadMore() {
        pageData.loadMore()
    }

    fun pagingRetry() {
        pageData.retry()
    }
}