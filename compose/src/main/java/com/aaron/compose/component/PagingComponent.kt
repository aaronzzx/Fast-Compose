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
    pagingable: Pagingable<K, V>,
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
    customFooter: Boolean = false,
    footer: @Composable (
        pagingable2: Pagingable<K, V>,
        pagingFooterType: PagingFooterType
    ) -> Unit = { pagingable2, pagingFooterType ->
        rememberPagingComponentFooter<K, V>().Content(
            pagingable = pagingable2,
            pagingFooterType = pagingFooterType
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
        val pageData = pagingable.pageData
        content(pageData)

        if (customFooter) {
            return@LazyColumn
        }

        val pagingFooterType = getPagingFooterType(pagingable)
        if (pagingFooterType != PagingFooterType.Idle) {
            item(contentType = "PageComponentFooter") {
                footer(pagingable, pagingFooterType)
            }
        }
    }
}

/**
 * 网格分页
 */
@Composable
fun <K, V> PagingGridComponent(
    pagingable: Pagingable<K, V>,
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
        pagingable2: Pagingable<K, V>,
        pagingFooterType: PagingFooterType
    ) -> Unit = { pagingable2, pagingFooterType ->
        rememberPagingComponentFooter<K, V>().Content(
            pagingable = pagingable2,
            pagingFooterType = pagingFooterType
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
        val pageData = pagingable.pageData
        content(pageData)

        if (customFooter) {
            return@LazyVerticalGrid
        }

        val pagingFooterType = getPagingFooterType(pagingable)
        if (pagingFooterType != PagingFooterType.Idle) {
            item(
                span = {
                    GridItemSpan(maxLineSpan)
                },
                contentType = "PageComponentFooter"
            ) {
                footer(pagingable, pagingFooterType)
            }
        }
    }
}

fun <K, V> getPagingFooterType(pagingable: Pagingable<K, V>): PagingFooterType {
    val pageData = pagingable.pageData
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
    open fun Content(pagingable: Pagingable<K, V>, pagingFooterType: PagingFooterType) {
        when (pagingFooterType) {
            PagingFooterType.Loading -> LoadingContent(pagingable)
            PagingFooterType.LoadMore -> LoadMoreContent(pagingable)
            PagingFooterType.LoadError -> LoadErrorContent(pagingable)
            PagingFooterType.NoMoreData -> NoMoreDataContent(pagingable)
            PagingFooterType.WaitingRefresh -> WaitingRefreshContent(pagingable)
            else -> Unit
        }
    }
    
    @Composable
    open fun LoadingContent(pagingable: Pagingable<K, V>) {
        FooterText(
            text = "加载中...",
            pagingable = pagingable,
            pagingFooterType = PagingFooterType.Loading
        )
    }

    @Composable
    open fun LoadMoreContent(pagingable: Pagingable<K, V>) {
        FooterText(
            text = "点击加载更多",
            pagingable = pagingable,
            pagingFooterType = PagingFooterType.LoadMore
        )
    }

    @Composable
    open fun LoadErrorContent(pagingable: Pagingable<K, V>) {
        FooterText(
            text = "加载失败，点击重试",
            pagingable = pagingable,
            pagingFooterType = PagingFooterType.LoadError
        )
    }

    @Composable
    open fun NoMoreDataContent(pagingable: Pagingable<K, V>) {
        FooterText(
            text = "已经到底了",
            pagingable = pagingable,
            pagingFooterType = PagingFooterType.NoMoreData
        )
    }

    @Composable
    open fun WaitingRefreshContent(pagingable: Pagingable<K, V>) {
        FooterText(
            text = "等待刷新完成",
            pagingable = pagingable,
            pagingFooterType = PagingFooterType.WaitingRefresh
        )
    }

    @Composable
    open fun FooterText(
        text: String,
        pagingable: Pagingable<K, V>,
        pagingFooterType: PagingFooterType,
        modifier: Modifier = Modifier,
        fontSize: TextUnit = 12.sp,
        textColor: Color = Color(0xFF666666)
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .let {
                    when (pagingFooterType) {
                        PagingFooterType.LoadMore -> it.onClick {
                            pagingable.loadMore()
                        }
                        PagingFooterType.LoadError -> it.onClick {
                            pagingable.loadMoreRetry()
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
interface Pagingable<K, V> : Refreshable {

    val pageData: PageData<K, V>

    override fun refreshIgnoreAnimation() {
        pageData.refresh()
    }

    fun loadMore() {
        pageData.loadMore()
    }

    fun loadMoreRetry() {
        pageData.retry()
    }
}