package com.aaron.compose.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aaron.compose.R
import com.aaron.compose.ktx.canScroll
import com.aaron.compose.ktx.isEmpty
import com.aaron.compose.ktx.isNotEmpty
import com.aaron.compose.ktx.items
import com.aaron.compose.ktx.lastIndex
import com.aaron.compose.ktx.onClick
import com.aaron.compose.ktx.toDp
import com.aaron.compose.ktx.toPx
import com.aaron.compose.paging.LoadResult
import com.aaron.compose.paging.LoadState
import com.aaron.compose.paging.PageData
import com.aaron.compose.paging.PagingScope
import com.aaron.compose.utils.DevicePreview
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

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
    pagingStateFooter: PagingStateFooter? = PagingComponentDefaults.verticalPagingStateFooter,
    headerContent: (@Composable () -> Unit)? = null,
    footerContent: (@Composable () -> Unit)? = null,
    loadingContent: (@Composable () -> Unit)? = null,
    emptyContent: (@Composable () -> Unit)? = {
        PagingEmpty()
    },
    content: LazyListScope.(PageData<K, V>) -> Unit
) {
    SubcomposeList(
        slotId = "${component}-LazyColumn",
        modifier = modifier
    ) { listHeightPixels ->
        var headerHeightPixels by remember {
            mutableStateOf(0)
        }
        var footerHeightPixels by remember {
            mutableStateOf(0)
        }

        val pagingFooterType by rememberPagingFooterType(component = component)
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
                item(
                    key = "${component}-Header",
                    contentType = "Header"
                ) {
                    SubcomposeLayout { constraint ->
                        val placeables = subcompose("${component}-Header", headerContent).map {
                            it.measure(constraint).apply {
                                headerHeightPixels = height.coerceAtLeast(headerHeightPixels)
                            }
                        }
                        layout(constraint.maxWidth, headerHeightPixels) {
                            placeables.forEach {
                                it.placeRelative(0, 0)
                            }
                        }
                    }
                }
            }

            val pageData = component.pageData
            if (loadingContent != null && pageData.isInitialized.not()) {
                item(
                    key = "${component}-Loading",
                    contentType = "Loading"
                ) {
                    CentralContent(
                        contentPadding = contentPadding,
                        verticalArrangement = verticalArrangement,
                        existsHeader = headerContent != null,
                        existsFooter = footerContent != null,
                        listHeightPixels = listHeightPixels,
                        headerHeightPixels = headerHeightPixels,
                        footerHeightPixels = footerHeightPixels
                    ) {
                        loadingContent()
                    }
                }
            } else if (emptyContent != null && pageData.isEmpty) {
                item(
                    key = "${component}-Empty",
                    contentType = "Empty"
                ) {
                    CentralContent(
                        contentPadding = contentPadding,
                        verticalArrangement = verticalArrangement,
                        existsHeader = headerContent != null,
                        existsFooter = footerContent != null,
                        listHeightPixels = listHeightPixels,
                        headerHeightPixels = headerHeightPixels,
                        footerHeightPixels = footerHeightPixels
                    ) {
                        emptyContent()
                    }
                }
            } else {
                content(pageData)
            }

            if (footerContent != null) {
                item(
                    key = "${component}-Footer",
                    contentType = "Footer"
                ) {
                    SubcomposeLayout { constraint ->
                        val placeables = subcompose("${component}-Footer", footerContent).map {
                            it.measure(constraint).apply {
                                footerHeightPixels = height.coerceAtLeast(footerHeightPixels)
                            }
                        }
                        layout(constraint.maxWidth, footerHeightPixels) {
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
                        lazyListScope.item(
                            key = "${component}-PagingStateFooter",
                            contentType = "PagingStateFooter"
                        ) {
                            content(component)
                        }
                    }
                }
                when (pagingFooterType) {
                    PagingFooterType.Loading -> trySetFooter(
                        this,
                        component,
                        pagingStateFooter.loading
                    )
                    PagingFooterType.LoadMore -> trySetFooter(
                        this,
                        component,
                        pagingStateFooter.loadMore
                    )
                    PagingFooterType.LoadError -> trySetFooter(
                        this,
                        component,
                        pagingStateFooter.loadError
                    )
                    PagingFooterType.NoMoreData -> trySetFooter(
                        this,
                        component,
                        pagingStateFooter.noMoreData
                    )
                    PagingFooterType.WaitingRefresh -> trySetFooter(
                        this,
                        component,
                        pagingStateFooter.waitingRefresh
                    )
                    else -> Unit
                }
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
    pagingStateFooter: PagingStateFooter? = PagingComponentDefaults.verticalPagingStateFooter,
    headerContent: (@Composable () -> Unit)? = null,
    footerContent: (@Composable () -> Unit)? = null,
    loadingContent: (@Composable () -> Unit)? = null,
    emptyContent: (@Composable () -> Unit)? = {
        PagingEmpty()
    },
    content: LazyGridScope.(PageData<K, V>) -> Unit
) {
    SubcomposeList(
        slotId = "${component}-LazyVerticalGrid",
        modifier = modifier
    ) { listHeightPixels ->
        var headerHeightPixels by remember {
            mutableStateOf(0)
        }
        var footerHeightPixels by remember {
            mutableStateOf(0)
        }

        val pagingFooterType by rememberPagingFooterType(component = component)
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
                    key = "${component}-Header",
                    contentType = "Header"
                ) {
                    SubcomposeLayout { constraint ->
                        val placeables = subcompose("${component}-Header", headerContent).map {
                            it.measure(constraint).apply {
                                headerHeightPixels = height.coerceAtLeast(headerHeightPixels)
                            }
                        }
                        layout(constraint.maxWidth, headerHeightPixels) {
                            placeables.forEach {
                                it.placeRelative(0, 0)
                            }
                        }
                    }
                }
            }

            val pageData = component.pageData
            if (loadingContent != null && pageData.isInitialized.not()) {
                item(
                    span = {
                        GridItemSpan(maxLineSpan)
                    },
                    key = "${component}-Loading",
                    contentType = "Loading"
                ) {
                    CentralContent(
                        contentPadding = contentPadding,
                        verticalArrangement = verticalArrangement,
                        existsHeader = headerContent != null,
                        existsFooter = footerContent != null,
                        listHeightPixels = listHeightPixels,
                        headerHeightPixels = headerHeightPixels,
                        footerHeightPixels = footerHeightPixels
                    ) {
                        loadingContent()
                    }
                }
            } else if (emptyContent != null && pageData.isEmpty) {
                item(
                    span = {
                        GridItemSpan(maxLineSpan)
                    },
                    key = "${component}-Empty",
                    contentType = "Empty"
                ) {
                    CentralContent(
                        contentPadding = contentPadding,
                        verticalArrangement = verticalArrangement,
                        existsHeader = headerContent != null,
                        existsFooter = footerContent != null,
                        listHeightPixels = listHeightPixels,
                        headerHeightPixels = headerHeightPixels,
                        footerHeightPixels = footerHeightPixels
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
                    key = "$component-Footer",
                    contentType = "Footer"
                ) {
                    SubcomposeLayout { constraint ->
                        val placeables = subcompose("$component-Footer", footerContent).map {
                            it.measure(constraint).apply {
                                footerHeightPixels = height.coerceAtLeast(footerHeightPixels)
                            }
                        }
                        layout(constraint.maxWidth, footerHeightPixels) {
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
                            key = "${component}-PagingStateFooter",
                            contentType = "PagingStateFooter"
                        ) {
                            content(component)
                        }
                    }
                }
                when (pagingFooterType) {
                    PagingFooterType.Loading -> trySetFooter(
                        this,
                        component,
                        pagingStateFooter.loading
                    )
                    PagingFooterType.LoadMore -> trySetFooter(
                        this,
                        component,
                        pagingStateFooter.loadMore
                    )
                    PagingFooterType.LoadError -> trySetFooter(
                        this,
                        component,
                        pagingStateFooter.loadError
                    )
                    PagingFooterType.NoMoreData -> trySetFooter(
                        this,
                        component,
                        pagingStateFooter.noMoreData
                    )
                    PagingFooterType.WaitingRefresh -> trySetFooter(
                        this,
                        component,
                        pagingStateFooter.waitingRefresh
                    )
                    else -> Unit
                }
            }
        }
    }
}

/**
 * 瀑布流分页
 */
@Composable
fun <K, V> PagingStaggeredGridComponent(
    component: PagingComponent<K, V>,
    columns: StaggeredGridCells,
    modifier: Modifier = Modifier,
    state: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(0.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(0.dp),
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    pagingStateFooter: PagingStateFooter? = PagingComponentDefaults.verticalPagingStateFooter,
    headerContent: (@Composable () -> Unit)? = null,
    footerContent: (@Composable () -> Unit)? = null,
    loadingContent: (@Composable () -> Unit)? = null,
    emptyContent: (@Composable () -> Unit)? = {
        PagingEmpty()
    },
    content: LazyStaggeredGridScope.(PageData<K, V>) -> Unit
) {
    val pageData = component.pageData
    if (loadingContent != null && pageData.isInitialized.not()) {
        loadingContent()
    } else if (emptyContent != null && pageData.isEmpty) {
        emptyContent()
    } else {
        LazyVerticalStaggeredGrid(
            columns = columns,
            modifier = modifier,
            state = state,
            contentPadding = contentPadding,
            verticalArrangement = verticalArrangement,
            horizontalArrangement = horizontalArrangement,
            flingBehavior = flingBehavior,
            userScrollEnabled = userScrollEnabled
        ) {
            content(pageData)
        }
    }
}

/**
 * 水平分页
 */
@Composable
fun <K, V> PagingHorizontalComponent(
    component: PagingComponent<K, V>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    horizontalArrangement: Arrangement.Horizontal =
        if (!reverseLayout) Arrangement.Start else Arrangement.End,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    pagingStateFooter: PagingStateFooter? = PagingComponentDefaults.horizontalPagingStateFooter,
    content: LazyListScope.(PageData<K, V>) -> Unit
) {
    val density = LocalDensity.current
    var lastIndex = remember(component) { component.pageData.lastIndex }
    val hasLoadingContent by rememberUpdatedState(newValue = pagingStateFooter?.loading != null)
    val hasLoadMoreContent by rememberUpdatedState(newValue = pagingStateFooter?.loadMore != null)
    val hasLoadErrorContent by rememberUpdatedState(newValue = pagingStateFooter?.loadError != null)
    val pagingFooterType by rememberPagingFooterType(component = component)
    val isDragged by state.interactionSource.collectIsDraggedAsState()

    LaunchedEffect(component, state) {
        // 当没有设置 loading 时，监听到加载完成时自动滑动列表以提示新数据
        launch {
            snapshotFlow { component.pageData.lastIndex }
                .filter { !hasLoadingContent }
                .filter { pagingLastIndex ->
                    val curLastIndex = lastIndex
                    lastIndex = pagingLastIndex
                    pagingLastIndex != -1
                            && curLastIndex != -1
                            && curLastIndex < pagingLastIndex
                }
                .filter { !state.isScrollInProgress && !state.canScroll(1) }
                .collect {
                    // 滑出 item 的一半
                    val lastHalfSize = state.layoutInfo.visibleItemsInfo.last().size / 2f
                    val spacing = with(density) { horizontalArrangement.spacing.toPx() }
                    val offset = lastHalfSize + spacing
                    state.animateScrollBy(offset)
                }
        }
        // 拖拽到底部触发加载、重试
        launch {
            snapshotFlow { state.isScrollInProgress to state.firstVisibleItemScrollOffset }
                .filter {
                    isDragged && !state.canScroll(1)
                }
                .collect {
                    val pageConfig = component.pageData.config
                    val prefetchEnabled = pageConfig.prefetchDistance > 0
                    when {
                        !hasLoadMoreContent
                                && !prefetchEnabled
                                && pagingFooterType == PagingFooterType.LoadMore -> component.pagingLoadMore()
                        !hasLoadErrorContent
                                && pagingFooterType == PagingFooterType.LoadError -> component.pagingRetry()
                        else -> Unit
                    }
                }
        }
    }
    LazyRow(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled
    ) {
        content(component.pageData)

        if (pagingStateFooter != null) {
            fun trySetFooter(
                lazyListScope: LazyListScope,
                component: PagingComponent<*, *>,
                content: (@Composable (PagingComponent<*, *>) -> Unit)?
            ) {
                if (content != null) {
                    lazyListScope.item(
                        key = "${component}-PagingStateFooter",
                        contentType = "PagingStateFooter"
                    ) {
                        content(component)
                    }
                }
            }
            when (pagingFooterType) {
                PagingFooterType.Loading -> trySetFooter(
                    this,
                    component,
                    pagingStateFooter.loading
                )
                PagingFooterType.LoadMore -> trySetFooter(
                    this,
                    component,
                    pagingStateFooter.loadMore
                )
                PagingFooterType.LoadError -> trySetFooter(
                    this,
                    component,
                    pagingStateFooter.loadError
                )
                PagingFooterType.NoMoreData -> trySetFooter(
                    this,
                    component,
                    pagingStateFooter.noMoreData
                )
                PagingFooterType.WaitingRefresh -> trySetFooter(
                    this,
                    component,
                    pagingStateFooter.waitingRefresh
                )
                else -> Unit
            }
        }
    }
}

@Composable
private fun SubcomposeList(
    slotId: Any?,
    modifier: Modifier = Modifier,
    content: @Composable (listHeightPixels: Int) -> Unit
) {
    var listHeightPixels by remember {
        mutableStateOf(0)
    }
    SubcomposeLayout(modifier = modifier) { constraint ->
        val placeables = subcompose(slotId) {
            content(listHeightPixels)
        }.map {
            it.measure(constraint).apply {
                listHeightPixels = height.coerceAtLeast(listHeightPixels)
            }
        }
        layout(constraint.maxWidth, constraint.maxHeight) {
            placeables.forEach {
                it.placeRelative(0, 0)
            }
        }
    }
}

@Composable
fun PagingLoading(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.primary,
    strokeWidth: Dp = ProgressIndicatorDefaults.StrokeWidth
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = color,
            strokeWidth = strokeWidth
        )
    }
}

@Composable
fun PagingEmpty(
    modifier: Modifier = Modifier,
    text: String = "暂无数据",
    backgroundColor: Color = Color.White,
    shape: Shape = RoundedCornerShape(8.dp),
    @DrawableRes iconRes: Int = R.drawable.details_image_wholea_normal,
    iconSize: Dp = 160.dp,
    betweenPadding: Dp = 24.dp,
    textColor: Color = Color(0xFF999999),
    textSize: TextUnit = 16.sp,
    textWeight: FontWeight = FontWeight.Normal,
    textStyle: TextStyle? = null
) {
    StateView(
        text = text,
        modifier = modifier,
        backgroundColor = backgroundColor,
        shape = shape,
        iconRes = iconRes,
        iconSize = iconSize,
        betweenPadding = betweenPadding,
        textColor = textColor,
        textSize = textSize,
        textWeight = textWeight,
        textStyle = textStyle
    )
}

@Composable
private fun CentralContent(
    contentPadding: PaddingValues,
    verticalArrangement: Arrangement.Vertical,
    existsHeader: Boolean,
    existsFooter: Boolean,
    listHeightPixels: Int,
    headerHeightPixels: Int,
    footerHeightPixels: Int,
    content: @Composable () -> Unit
) {
    val verticalPaddingPx = with(contentPadding) {
        calculateTopPadding() + calculateBottomPadding()
    }.toPx()
    val spacingPx = verticalArrangement.spacing.toPx()
    val totalSpacingPx = run {
        if (existsHeader && existsFooter) {
            spacingPx * 2
        } else if (existsHeader || existsFooter) {
            spacingPx
        } else {
            0f
        }
    }
    val emptyContentHeightPx =
        listHeightPixels - headerHeightPixels - footerHeightPixels - verticalPaddingPx - totalSpacingPx

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(emptyContentHeightPx.toDp())
    ) {
        content()
    }
}

@DevicePreview
@Composable
private fun PagingComponent() {
    PagingComponent<Int, String>(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF0F0F0)),
        component = pagingComponent("1", "2", "3", "4", "5"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) { pageData ->
        items(pageData) { item ->
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        color = Color.White,
                    )
                    .padding(16.dp)
                    .wrapContentSize(),
                text = item
            )
        }
    }
}

@Composable
fun <K, V> rememberPagingFooterType(component: PagingComponent<K, V>): State<PagingFooterType> {
    val pageData = component.pageData
    val loadMoreState = pageData.loadState.loadMore
    val waitingRefresh = loadMoreState is LoadState.Waiting
    val loadMore = loadMoreState is LoadState.Idle
            && !loadMoreState.noMoreData
            && pageData.isNotEmpty
    val loading = loadMoreState is LoadState.Loading
    val loadError = loadMoreState is LoadState.Error
    val noMoreData = loadMoreState.noMoreData

    val state = remember {
        mutableStateOf(PagingFooterType.None)
    }
    state.value = when {
        pageData.isEmpty -> PagingFooterType.None
        loading -> PagingFooterType.Loading
        loadMore -> PagingFooterType.LoadMore
        loadError -> PagingFooterType.LoadError
        noMoreData -> PagingFooterType.NoMoreData
        waitingRefresh -> PagingFooterType.WaitingRefresh
        else -> PagingFooterType.None
    }
    return state
}

enum class PagingFooterType {

    None, Loading, LoadMore, LoadError, NoMoreData, WaitingRefresh
}

object PagingComponentDefaults {

    var verticalPagingStateFooter: PagingStateFooter = PagingStateFooter()

    var horizontalPagingStateFooter: PagingStateFooter = object : PagingStateFooter() {
        override val loading: @Composable (PagingComponent<*, *>) -> Unit = {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        override val loadMore: ((PagingComponent<*, *>) -> Unit)? = null
        override val loadError: ((PagingComponent<*, *>) -> Unit)? = null
        override val noMoreData: ((PagingComponent<*, *>) -> Unit)? = null
        override val waitingRefresh: ((PagingComponent<*, *>) -> Unit)? = null
    }
}

@Stable
open class PagingStateFooter {

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

    /**
     * PageData 是否已经初始化，即成功加载过数据
     */
    val isInitialized: Boolean get() = pageData.isInitialized

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

@Stable
interface PagingMultiComponent : PagingScope {

    fun <K, V> PageData<K, V>.toPagingComponent(
        onRefresh: ((PageData<K, V>) -> Unit)? = null,
        onLoadMore: ((PageData<K, V>) -> Unit)? = null,
        onRetry: ((PageData<K, V>) -> Unit)? = null
    ): PagingComponent<K, V> = object : PagingComponent<K, V> {
        override val pageData: PageData<K, V> = this@toPagingComponent

        override fun pagingRefresh() {
            onRefresh?.invoke(pageData) ?: super.pagingRefresh()
        }

        override fun pagingLoadMore() {
            onLoadMore?.invoke(pageData) ?: super.pagingLoadMore()
        }

        override fun pagingRetry() {
            onRetry?.invoke(pageData) ?: super.pagingRetry()
        }
    }
}

@Composable
fun <K, V> pagingComponent(vararg item: V): PagingComponent<K, V> = object : PagingComponent<K, V> {

    override val pageData: PageData<K, V> = PageData(
        coroutineScope = MainScope(),
        onRequest = { LoadResult.Page(listOf(*item), null) }
    )
}