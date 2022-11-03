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
import com.aaron.compose.ktx.isEmpty
import com.aaron.compose.ktx.isNotEmpty
import com.aaron.compose.ktx.onClick
import com.aaron.compose.paging.LoadResult
import com.aaron.compose.paging.LoadState
import com.aaron.compose.paging.PageData
import kotlinx.coroutines.MainScope

@Composable
fun <K, V> PagingWrapperComponent(
    component: PagingComponent<K, V>,
    modifier: Modifier = Modifier,
    empty: @Composable () -> Unit = {
        ViewStateLayout(text = "暂无数据")
    },
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        if (component.pageData.isEmpty) {
            empty()
        } else {
            content()
        }
    }
}

/**
 * 分页
 */
@Composable
fun <K, V> PagingComponent(
    component: PagingComponent<K, V>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    footer: (@Composable (pagingFooterType: PagingFooterType) -> Unit)? = {
        remember { PagingComponentFooter() }.Content(
            component = component,
            footerType = it
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
        if (pagingFooterType != PagingFooterType.None) {
            item(contentType = "PagingComponentFooter") {
                footer(pagingFooterType)
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
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    footer: (@Composable (pagingFooterType: PagingFooterType) -> Unit)? = {
        remember { PagingComponentFooter() }.Content(
            component = component,
            footerType = it
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

        if (footer == null) {
            return@LazyVerticalGrid
        }

        val pagingFooterType = getPagingFooterType(component)
        if (pagingFooterType != PagingFooterType.None) {
            item(
                span = {
                    GridItemSpan(maxLineSpan)
                },
                contentType = "PagingComponentFooter"
            ) {
                footer(pagingFooterType)
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
        pageData.isEmpty -> PagingFooterType.None
        loading -> PagingFooterType.Loading
        loadMore -> PagingFooterType.LoadMore
        loadError -> PagingFooterType.LoadError
        noMoreData -> PagingFooterType.NoMoreData
        waitingRefresh -> PagingFooterType.WaitingRefresh
        else -> PagingFooterType.None
    }
}

enum class PagingFooterType {

    None, Loading, LoadMore, LoadError, NoMoreData, WaitingRefresh
}

open class PagingComponentFooter {

    @Composable
    open fun Content(component: PagingComponent<*, *>, footerType: PagingFooterType) {
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
    open fun LoadingContent(component: PagingComponent<*, *>) {
        FooterText(
            text = "加载中...",
            component = component,
            footerType = PagingFooterType.Loading
        )
    }

    @Composable
    open fun LoadMoreContent(component: PagingComponent<*, *>) {
        FooterText(
            text = "点击加载更多",
            component = component,
            footerType = PagingFooterType.LoadMore
        )
    }

    @Composable
    open fun LoadErrorContent(component: PagingComponent<*, *>) {
        FooterText(
            text = "加载失败，点击重试",
            component = component,
            footerType = PagingFooterType.LoadError
        )
    }

    @Composable
    open fun NoMoreDataContent(component: PagingComponent<*, *>) {
        FooterText(
            text = "已经到底了",
            component = component,
            footerType = PagingFooterType.NoMoreData
        )
    }

    @Composable
    open fun WaitingRefreshContent(component: PagingComponent<*, *>) {
        FooterText(
            text = "等待刷新完成",
            component = component,
            footerType = PagingFooterType.WaitingRefresh
        )
    }

    @Composable
    open fun FooterText(
        text: String,
        component: PagingComponent<*, *>,
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

/**
 * 分页状态组件
 */
@Stable
interface PagingComponent<K, V> {

    val pageData: PageData<K, V>

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

@Composable
fun <K, V> pagingComponent(vararg item: V): PagingComponent<K, V> = object : PagingComponent<K, V> {

    override val pageData: PageData<K, V> = PageData(
        coroutineScope = MainScope(),
        onRequest = { LoadResult.Page(listOf(*item), null) }
    )
}