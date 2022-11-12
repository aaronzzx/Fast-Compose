package com.aaron.compose.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aaron.compose.R
import com.aaron.compose.ktx.isEmpty
import com.aaron.compose.ktx.isNotEmpty
import com.aaron.compose.ktx.onClick
import com.aaron.compose.paging.LoadResult
import com.aaron.compose.paging.LoadState
import com.aaron.compose.paging.PageData
import com.aaron.compose.paging.PagingScope
import kotlinx.coroutines.MainScope

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
    pagingStateFooter: PagingStateFooter? = pagingStateFooterSingleton,
    headerContent: (@Composable () -> Unit)? = null,
    footerContent: (@Composable () -> Unit)? = null,
    emptyContent: (@Composable () -> Unit)? = {
        PagingEmptyLayout()
    },
    content: LazyListScope.(PageData<K, V>) -> Unit
) {
    var listHeightPx by remember {
        mutableStateOf(0)
    }
    var headerHeightPx by remember {
        mutableStateOf(0)
    }
    var footerHeightPx by remember {
        mutableStateOf(0)
    }
    SubcomposeLayout(modifier = modifier) { constraint ->
        val placeables = subcompose("LazyColumn") {
            LazyColumn(
                state = state,
                contentPadding = contentPadding,
                reverseLayout = reverseLayout,
                verticalArrangement = verticalArrangement,
                horizontalAlignment = horizontalAlignment,
                flingBehavior = flingBehavior,
                userScrollEnabled = userScrollEnabled
            ) {
                if (headerContent != null) {
                    item(contentType = "Header") {
                        SubcomposeLayout { constraint ->
                            val placeables = subcompose("Header", headerContent).map {
                                val placeable = it.measure(constraint)
                                headerHeightPx = placeable.height.coerceAtLeast(headerHeightPx)
                                placeable
                            }
                            layout(constraint.maxWidth, headerHeightPx) {
                                placeables.forEach {
                                    it.placeRelative(0, 0)
                                }
                            }
                        }
                    }
                }

                val pageData = component.pageData
                if (pageData.isEmpty && emptyContent != null) {
                    item(contentType = "EmptyContent") {
                        val verticalPaddingPx = with(contentPadding) {
                            calculateTopPadding() + calculateBottomPadding()
                        }.toPx()
                        val spacingPx = verticalArrangement.spacing.toPx()
                        val totalSpacingPx = run {
                            if (headerContent != null && footerContent != null) {
                                spacingPx * 2
                            } else if (headerContent != null || footerContent != null) {
                                spacingPx
                            } else {
                                0f
                            }
                        }
                        val emptyContentHeightPx =
                            listHeightPx - headerHeightPx - footerHeightPx - verticalPaddingPx - totalSpacingPx

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(emptyContentHeightPx.toDp())
                        ) {
                            emptyContent()
                        }
                    }
                } else {
                    content(pageData)
                }

                if (footerContent != null) {
                    item(contentType = "Footer") {
                        SubcomposeLayout { constraint ->
                            val placeables = subcompose("Footer", footerContent).map {
                                val placeable = it.measure(constraint)
                                footerHeightPx = placeable.height.coerceAtLeast(footerHeightPx)
                                placeable
                            }
                            layout(constraint.maxWidth, footerHeightPx) {
                                placeables.forEach {
                                    it.placeRelative(0, 0)
                                }
                            }
                        }
                    }
                }

                if (pagingStateFooter != null) {
                    fun trySetFooter(
                        lazyListScope: LazyListScope,
                        component: PagingComponent<*, *>,
                        content: (@Composable (PagingComponent<*, *>) -> Unit)?
                    ) {
                        if (content != null) {
                            lazyListScope.item(contentType = "PagingStateFooter") {
                                content(component)
                            }
                        }
                    }
                    when (getPagingFooterType(component)) {
                        PagingFooterType.Loading -> trySetFooter(this, component, pagingStateFooter.loading)
                        PagingFooterType.LoadMore -> trySetFooter(this, component, pagingStateFooter.loadMore)
                        PagingFooterType.LoadError -> trySetFooter(this, component, pagingStateFooter.loadError)
                        PagingFooterType.NoMoreData -> trySetFooter(this, component, pagingStateFooter.noMoreData)
                        PagingFooterType.WaitingRefresh -> trySetFooter(this, component, pagingStateFooter.waitingRefresh)
                        else -> Unit
                    }
                }
            }
        }.map {
            val placeable = it.measure(constraint)
            listHeightPx = placeable.height.coerceAtLeast(listHeightPx)
            placeable
        }
        layout(constraint.maxWidth, constraint.maxHeight) {
            placeables.forEach {
                it.placeRelative(0, 0)
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
    pagingStateFooter: PagingStateFooter? = pagingStateFooterSingleton,
    headerContent: (@Composable () -> Unit)? = null,
    footerContent: (@Composable () -> Unit)? = null,
    emptyContent: (@Composable () -> Unit)? = {
        PagingEmptyLayout()
    },
    content: LazyGridScope.(PageData<K, V>) -> Unit
) {
    var listHeightPx by remember {
        mutableStateOf(0)
    }
    var headerHeightPx by remember {
        mutableStateOf(0)
    }
    var footerHeightPx by remember {
        mutableStateOf(0)
    }
    SubcomposeLayout(modifier = modifier) { constraint ->
        val placeables = subcompose("LazyVerticalGrid") {
            LazyVerticalGrid(
                columns = columns,
                state = state,
                contentPadding = contentPadding,
                reverseLayout = reverseLayout,
                verticalArrangement = verticalArrangement,
                horizontalArrangement = horizontalArrangement,
                flingBehavior = flingBehavior,
                userScrollEnabled = userScrollEnabled
            ) {
                if (headerContent != null) {
                    item(
                        span = {
                            GridItemSpan(maxLineSpan)
                        },
                        contentType = "Header"
                    ) {
                        SubcomposeLayout { constraint ->
                            val placeables = subcompose("Header", headerContent).map {
                                val placeable = it.measure(constraint)
                                headerHeightPx = placeable.height.coerceAtLeast(headerHeightPx)
                                placeable
                            }
                            layout(constraint.maxWidth, headerHeightPx) {
                                placeables.forEach {
                                    it.placeRelative(0, 0)
                                }
                            }
                        }
                    }
                }

                val pageData = component.pageData
                if (pageData.isEmpty && emptyContent != null) {
                    item(
                        span = {
                            GridItemSpan(maxLineSpan)
                        },
                        contentType = "EmptyContent"
                    ) {
                        val verticalPaddingPx = with(contentPadding) {
                            calculateTopPadding() + calculateBottomPadding()
                        }.toPx()
                        val spacingPx = verticalArrangement.spacing.toPx()
                        val totalSpacingPx = run {
                            if (headerContent != null && footerContent != null) {
                                spacingPx * 2
                            } else if (headerContent != null || footerContent != null) {
                                spacingPx
                            } else {
                                0f
                            }
                        }
                        val emptyContentHeightPx =
                            listHeightPx - headerHeightPx - footerHeightPx - verticalPaddingPx - totalSpacingPx

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(emptyContentHeightPx.toDp())
                        ) {
                            emptyContent()
                        }
                    }
                } else {
                    content(pageData)
                }

                if (footerContent != null) {
                    item(
                        span = {
                            GridItemSpan(maxLineSpan)
                        },
                        contentType = "Footer"
                    ) {
                        SubcomposeLayout { constraint ->
                            val placeables = subcompose("Footer", footerContent).map {
                                val placeable = it.measure(constraint)
                                footerHeightPx = placeable.height.coerceAtLeast(footerHeightPx)
                                placeable
                            }
                            layout(constraint.maxWidth, footerHeightPx) {
                                placeables.forEach {
                                    it.placeRelative(0, 0)
                                }
                            }
                        }
                    }
                }

                if (pagingStateFooter != null) {
                    fun trySetFooter(
                        lazyGridScope: LazyGridScope,
                        component: PagingComponent<*, *>,
                        content: (@Composable (PagingComponent<*, *>) -> Unit)?
                    ) {
                        if (content != null) {
                            lazyGridScope.item(
                                span = {
                                    GridItemSpan(maxLineSpan)
                                },
                                contentType = "PagingStateFooter"
                            ) {
                                content(component)
                            }
                        }
                    }
                    when (getPagingFooterType(component)) {
                        PagingFooterType.Loading -> trySetFooter(this, component, pagingStateFooter.loading)
                        PagingFooterType.LoadMore -> trySetFooter(this, component, pagingStateFooter.loadMore)
                        PagingFooterType.LoadError -> trySetFooter(this, component, pagingStateFooter.loadError)
                        PagingFooterType.NoMoreData -> trySetFooter(this, component, pagingStateFooter.noMoreData)
                        PagingFooterType.WaitingRefresh -> trySetFooter(this, component, pagingStateFooter.waitingRefresh)
                        else -> Unit
                    }
                }
            }
        }.map {
            val placeable = it.measure(constraint)
            listHeightPx = placeable.height.coerceAtLeast(listHeightPx)
            placeable
        }
        layout(constraint.maxWidth, constraint.maxHeight) {
            placeables.forEach {
                it.placeRelative(0, 0)
            }
        }
    }
}

@Composable
fun PagingEmptyLayout(
    modifier: Modifier = Modifier,
    text: String = "暂无数据",
    @DrawableRes iconRes: Int = R.drawable.details_image_wholea_normal,
    bgColor: Color = Color.White,
    shape: Shape = RoundedCornerShape(8.dp),
    iconSize: Dp = 160.dp,
    spacing: Dp = 24.dp,
    textColor: Color = Color(0xFF999999),
    textSize: TextUnit = 16.sp
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = bgColor, shape = shape),
        contentAlignment = Alignment.Center
    ) {
        VerticalImageText(
            text = text,
            iconRes = iconRes,
            iconSize = iconSize,
            spacing = spacing,
            textColor = textColor,
            textSize = textSize
        )
    }
}

@Composable
private fun VerticalImageText(
    text: String,
    @DrawableRes iconRes: Int,
    iconSize: Dp = 160.dp,
    spacing: Dp = 24.dp,
    textColor: Color = Color(0xFF999999),
    textSize: TextUnit = 16.sp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            modifier = Modifier.size(iconSize),
            contentDescription = null
        )
        Text(text = text, color = textColor, fontSize = textSize)
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

private val pagingStateFooterSingleton = PagingStateFooterImpl()

private class PagingStateFooterImpl : PagingStateFooter()

@Stable
abstract class PagingStateFooter {

    open val loading: (@Composable (PagingComponent<*, *>) -> Unit)? = {
        FooterText(
            text = "加载中...",
            component = it,
            footerType = PagingFooterType.Loading
        )
    }

    open val loadMore: (@Composable (PagingComponent<*, *>) -> Unit)? = {
        FooterText(
            text = "点击加载更多",
            component = it,
            footerType = PagingFooterType.LoadMore
        )
    }

    open val loadError: (@Composable (PagingComponent<*, *>) -> Unit)? = {
        FooterText(
            text = "加载失败，点击重试",
            component = it,
            footerType = PagingFooterType.LoadError
        )
    }

    open val noMoreData: (@Composable (PagingComponent<*, *>) -> Unit)? = {
        FooterText(
            text = "已经到底了",
            component = it,
            footerType = PagingFooterType.NoMoreData
        )
    }

    open val waitingRefresh: (@Composable (PagingComponent<*, *>) -> Unit)? = {
        FooterText(
            text = "等待刷新完成",
            component = it,
            footerType = PagingFooterType.WaitingRefresh
        )
    }
}

@Composable
private fun FooterText(
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

/**
 * 分页状态组件
 */
@Stable
interface PagingComponent<K, V> : PagingScope {

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