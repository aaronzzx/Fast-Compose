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
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.derivedStateOf
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
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.aaron.compose.R
import com.aaron.compose.ktx.canScroll
import com.aaron.compose.ktx.isEmpty
import com.aaron.compose.ktx.isNotEmpty
import com.aaron.compose.ktx.lastIndex
import com.aaron.compose.ktx.lazylist.items
import com.aaron.compose.ktx.onClick
import com.aaron.compose.ktx.toDp
import com.aaron.compose.ktx.toPx
import com.aaron.compose.paging.CombinedLoadState
import com.aaron.compose.paging.LoadResult
import com.aaron.compose.paging.LoadState
import com.aaron.compose.paging.PageData
import com.aaron.compose.paging.PagingScope
import com.aaron.compose.utils.DevicePreview
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.drop
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
    loadingContent: (@Composable () -> Unit)? = {
        PagingLoading()
    },
    emptyContent: (@Composable () -> Unit)? = {
        PagingEmpty()
    },
    errorContent: (@Composable () -> Unit)? = {
        PagingError(onClick = { component.pagingRefresh() })
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

        val pageData = component.pageData
        val loadState = pageData.loadState

        val refreshLoading by remember(loadState) {
            derivedStateOf {
                loadState.refresh is LoadState.Loading
            }
        }
        val refreshError by remember(loadState) {
            derivedStateOf {
                loadState.refresh is LoadState.Error
            }
        }

        ScrollToTopWhenRefreshEffect(state, loadState)

        LoadingBox(
            component = component,
            refreshLoading = refreshLoading,
            loadingContent = loadingContent
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = state,
                contentPadding = contentPadding,
                reverseLayout = reverseLayout,
                verticalArrangement = verticalArrangement,
                horizontalAlignment = horizontalAlignment,
                flingBehavior = flingBehavior,
                userScrollEnabled = userScrollEnabled
            ) {
                if (headerContent != null) {
                    itemHeaderFooter(
                        scope = this,
                        key = "${component}-Header",
                        contentType = "Header",
                        slotId = "${component}-Header",
                        onHeightChange = {
                            headerHeightPixels = it
                        },
                        content = headerContent
                    )
                }

                handleCentralContent(
                    scope = this,
                    component = component,
                    refreshLoading = refreshLoading,
                    refreshError = refreshError,
                    listHeightPixels = listHeightPixels,
                    headerHeightPixels = headerHeightPixels,
                    footerHeightPixels = footerHeightPixels,
                    contentPadding = contentPadding,
                    verticalArrangement = verticalArrangement,
                    headerContent = headerContent,
                    footerContent = footerContent,
                    loadingContent = loadingContent,
                    emptyContent = emptyContent,
                    errorContent = errorContent
                ) {
                    content(it)
                }

                if (footerContent != null) {
                    itemHeaderFooter(
                        scope = this,
                        key = "${component}-Footer",
                        contentType = "Footer",
                        slotId = "${component}-Footer",
                        onHeightChange = {
                            footerHeightPixels = it
                        },
                        content = footerContent
                    )
                }

                if (pagingStateFooter != null) {
                    trySetPagingStateFooter(
                        scope = this,
                        component = component,
                        pagingFooterType = pagingFooterType,
                        pagingStateFooter = pagingStateFooter
                    )
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
    loadingContent: (@Composable () -> Unit)? = {
        PagingLoading()
    },
    emptyContent: (@Composable () -> Unit)? = {
        PagingEmpty()
    },
    errorContent: (@Composable () -> Unit)? = {
        PagingError(onClick = { component.pagingRefresh() })
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

        val pageData = component.pageData
        val loadState = pageData.loadState

        val refreshLoading by remember(loadState) {
            derivedStateOf {
                loadState.refresh is LoadState.Loading
            }
        }
        val refreshError by remember(loadState) {
            derivedStateOf {
                loadState.refresh is LoadState.Error
            }
        }

        ScrollToTopWhenRefreshEffect(state, loadState)

        LoadingBox(
            component = component,
            refreshLoading = refreshLoading,
            loadingContent = loadingContent
        ) {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
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
                    itemHeaderFooter(
                        scope = this,
                        key = "${component}-Header",
                        contentType = "Header",
                        slotId = "${component}-Header",
                        onHeightChange = {
                            headerHeightPixels = it
                        },
                        content = headerContent
                    )
                }

                handleCentralContent(
                    scope = this,
                    component = component,
                    refreshLoading = refreshLoading,
                    refreshError = refreshError,
                    listHeightPixels = listHeightPixels,
                    headerHeightPixels = headerHeightPixels,
                    footerHeightPixels = footerHeightPixels,
                    contentPadding = contentPadding,
                    verticalArrangement = verticalArrangement,
                    headerContent = headerContent,
                    footerContent = footerContent,
                    loadingContent = loadingContent,
                    emptyContent = emptyContent,
                    errorContent = errorContent
                ) {
                    content(it)
                }

                if (footerContent != null) {
                    itemHeaderFooter(
                        scope = this,
                        key = "${component}-Footer",
                        contentType = "Footer",
                        slotId = "${component}-Footer",
                        onHeightChange = {
                            footerHeightPixels = it
                        },
                        content = footerContent
                    )
                }

                if (pagingStateFooter != null) {
                    trySetPagingStateFooter(
                        scope = this,
                        component = component,
                        pagingFooterType = pagingFooterType,
                        pagingStateFooter = pagingStateFooter
                    )
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
    loadingContent: (@Composable () -> Unit)? = {
        PagingLoading()
    },
    emptyContent: (@Composable () -> Unit)? = {
        PagingEmpty()
    },
    errorContent: (@Composable () -> Unit)? = {
        PagingError(onClick = { component.pagingRefresh() })
    },
    content: LazyStaggeredGridScope.(PageData<K, V>) -> Unit
) {
    val pageData = component.pageData
    val loadState = pageData.loadState

    val refreshLoading by remember(loadState) {
        derivedStateOf {
            loadState.refresh is LoadState.Loading
        }
    }
    val refreshError by remember(loadState) {
        derivedStateOf {
            loadState.refresh is LoadState.Error
        }
    }

    ScrollToTopWhenRefreshEffect(state, loadState)

    LoadingBox(
        component = component,
        refreshLoading = refreshLoading,
        loadingContent = loadingContent
    ) {
        if (loadingContent != null
            && pageData.isEmpty
            && (pageData.isInitialized.not() || refreshLoading)
        ) {
            Spacer(modifier = Modifier.fillMaxSize())
        } else if (errorContent != null && refreshError && pageData.isEmpty) {
            errorContent()
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
}

@Composable
private fun <K, V> LoadingBox(
    component: PagingComponent<K, V>,
    refreshLoading: Boolean,
    loadingContent: (@Composable () -> Unit)?,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        content()
        if (loadingContent != null
            && (component.pageData.isInitialized.not() || refreshLoading)
        ) {
            loadingContent()
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

    ScrollToTopWhenRefreshEffect(state, component.pageData.loadState)

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

private fun <K, V> handleCentralContent(
    scope: Any,
    component: PagingComponent<K, V>,
    refreshLoading: Boolean,
    refreshError: Boolean,
    listHeightPixels: Int,
    headerHeightPixels: Int,
    footerHeightPixels: Int,
    contentPadding: PaddingValues,
    verticalArrangement: Arrangement.Vertical,
    headerContent: (@Composable () -> Unit)?,
    footerContent: (@Composable () -> Unit)?,
    loadingContent: (@Composable () -> Unit)?,
    emptyContent: (@Composable () -> Unit)?,
    errorContent: (@Composable () -> Unit)?,
    content: (PageData<K, V>) -> Unit,
) {
    val pageData = component.pageData
    if (loadingContent != null
        && pageData.isEmpty
        && (pageData.isInitialized.not() || refreshLoading)
    ) {
        itemCentral(
            scope = scope,
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
            }
        }
    } else if (errorContent != null && refreshError && pageData.isEmpty) {
        itemCentral(
            scope = scope,
            key = "${component}-Error",
            contentType = "Error"
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
                errorContent()
            }
        }
    } else if (emptyContent != null && pageData.isEmpty) {
        itemCentral(
            scope = scope,
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
}

private fun itemCentral(
    scope: Any,
    key: Any,
    contentType: Any,
    content: @Composable () -> Unit
) {
    if (scope is LazyListScope) {
        with(scope) {
            item(key = key, contentType = contentType) {
                content()
            }
        }
    } else if (scope is LazyGridScope) {
        with(scope) {
            item(
                span = { GridItemSpan(maxLineSpan) },
                key = key,
                contentType = contentType
            ) {
                content()
            }
        }
    }
}

private fun <K, V> trySetPagingStateFooter(
    scope: Any,
    component: PagingComponent<K, V>,
    pagingFooterType: PagingFooterType,
    pagingStateFooter: PagingStateFooter
) {
    fun trySetFooter(
        scope: Any,
        component: PagingComponent<*, *>,
        content: (@Composable (PagingComponent<*, *>) -> Unit)?
    ) {
        if (content != null) {
            if (scope is LazyListScope) {
                with(scope) {
                    item(
                        key = "${component}-PagingStateFooter",
                        contentType = "PagingStateFooter"
                    ) {
                        content(component)
                    }
                }
            } else if (scope is LazyGridScope) {
                with(scope) {
                    item(
                        span = { GridItemSpan(maxLineSpan) },
                        key = "${component}-PagingStateFooter",
                        contentType = "PagingStateFooter"
                    ) {
                        content(component)
                    }
                }
            }
        }
    }
    when (pagingFooterType) {
        PagingFooterType.Loading -> trySetFooter(
            scope,
            component,
            pagingStateFooter.loading
        )
        PagingFooterType.LoadMore -> trySetFooter(
            scope,
            component,
            pagingStateFooter.loadMore
        )
        PagingFooterType.LoadError -> trySetFooter(
            scope,
            component,
            pagingStateFooter.loadError
        )
        PagingFooterType.NoMoreData -> trySetFooter(
            scope,
            component,
            pagingStateFooter.noMoreData
        )
        PagingFooterType.WaitingRefresh -> trySetFooter(
            scope,
            component,
            pagingStateFooter.waitingRefresh
        )
        else -> Unit
    }
}

private fun itemHeaderFooter(
    scope: Any,
    key: Any,
    contentType: Any,
    slotId: Any,
    onHeightChange: (heightPixels: Int) -> Unit,
    content: @Composable () -> Unit
) {
    if (scope is LazyListScope) {
        with(scope) {
            item(key = key, contentType = contentType) {
                MeasureHeightContent(
                    key = key,
                    contentType = contentType,
                    slotId = slotId,
                    onHeightChange = onHeightChange,
                    content = content
                )
            }
        }
    } else if (scope is LazyGridScope) {
        with(scope) {
            item(
                span = { GridItemSpan(maxLineSpan) },
                key = key,
                contentType = contentType
            ) {
                MeasureHeightContent(
                    key = key,
                    contentType = contentType,
                    slotId = slotId,
                    onHeightChange = onHeightChange,
                    content = content
                )
            }
        }
    }
}

@Composable
private fun MeasureHeightContent(
    key: Any,
    contentType: Any,
    slotId: Any,
    onHeightChange: (heightPixels: Int) -> Unit,
    content: @Composable () -> Unit
) {
    SubcomposeLayout { constraint ->
        var heightPixels = 0
        val placeables = subcompose(slotId, content).map {
            it.measure(constraint).apply {
                heightPixels = height.coerceAtLeast(0)
            }
        }
        onHeightChange(heightPixels)
        layout(constraint.maxWidth, heightPixels) {
            placeables.forEach {
                it.placeRelative(0, 0)
            }
        }
    }
}

@Composable
private fun ScrollToTopWhenRefreshEffect(listState: Any, loadState: CombinedLoadState) {
    LaunchedEffect(loadState.refresh, listState) {
        snapshotFlow { loadState.refresh }
            .drop(1) // 因为是监听事件，因此不需要启动监听时第一个值
            .filter {
                it is LoadState.Idle && it.loadCompleted
            }
            .collect {
                scrollToTop(listState)
            }
    }
}

private suspend fun scrollToTop(listState: Any) {
    when (listState) {
        is LazyListState -> listState.scrollToItem(0)
        is LazyGridState -> listState.scrollToItem(0)
        is LazyStaggeredGridState -> listState.scrollToItem(0)
        else -> error("Unknown listState: $listState.")
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
    onClick: (() -> Unit)? = null,
    enableClickRipple: Boolean = true,
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
        onClick = onClick,
        enableClickRipple = enableClickRipple,
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
fun PagingError(
    modifier: Modifier = Modifier,
    text: String = "加载失败",
    onClick: (() -> Unit)? = null,
    enableClickRipple: Boolean = true,
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
        onClick = onClick,
        enableClickRipple = enableClickRipple,
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

    var verticalPagingStateFooter: PagingStateFooter? = VerticalPagingStateFooter()

    var horizontalPagingStateFooter: PagingStateFooter? = HorizontalPagingStateFooter()
}

@Stable
abstract class PagingStateFooter {

    abstract val loading: (@Composable (PagingComponent<*, *>) -> Unit)?

    abstract val loadMore: (@Composable (PagingComponent<*, *>) -> Unit)?

    abstract val loadError: (@Composable (PagingComponent<*, *>) -> Unit)?

    abstract val noMoreData: (@Composable (PagingComponent<*, *>) -> Unit)?

    abstract val waitingRefresh: (@Composable (PagingComponent<*, *>) -> Unit)?
}

@Stable
open class VerticalPagingStateFooter : PagingStateFooter() {

    override val loading: (@Composable (PagingComponent<*, *>) -> Unit)? = {
        FooterText(
            text = "加载中...",
            component = it,
            footerType = PagingFooterType.Loading
        )
    }

    override val loadMore: (@Composable (PagingComponent<*, *>) -> Unit)? = {
        FooterText(
            text = "点击加载更多",
            component = it,
            footerType = PagingFooterType.LoadMore
        )
    }

    override val loadError: (@Composable (PagingComponent<*, *>) -> Unit)? = {
        FooterText(
            text = "加载失败，点击重试",
            component = it,
            footerType = PagingFooterType.LoadError
        )
    }

    override val noMoreData: (@Composable (PagingComponent<*, *>) -> Unit)? = {
        CommonNoMoreDataContent(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        )
    }

    override val waitingRefresh: (@Composable (PagingComponent<*, *>) -> Unit)? = {
        FooterText(
            text = "等待刷新完成",
            component = it,
            footerType = PagingFooterType.WaitingRefresh
        )
    }

    @Composable
    fun CommonNoMoreDataContent(modifier: Modifier = Modifier) {
        ConstraintLayout(modifier = modifier.fillMaxWidth()) {
            val (line1Ref, line2Ref, textRef) = createRefs()
            Text(
                modifier = Modifier
                    .constrainAs(textRef) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                text = "已经到底了",
                fontSize = 12.sp,
                color = Color(0xFF999999)
            )
            Box(
                modifier = Modifier
                    .constrainAs(line1Ref) {
                        width = Dimension.value(80.dp)
                        height = Dimension.value(1.dp)
                        top.linkTo(textRef.top)
                        bottom.linkTo(textRef.bottom)
                        end.linkTo(textRef.start, 16.dp)
                    }
                    .background(color = Color(0xFFF2F2F2))
            )
            Box(
                modifier = Modifier
                    .constrainAs(line2Ref) {
                        width = Dimension.value(80.dp)
                        height = Dimension.value(1.dp)
                        top.linkTo(textRef.top)
                        bottom.linkTo(textRef.bottom)
                        start.linkTo(textRef.end, 16.dp)
                    }
                    .background(color = Color(0xFFF2F2F2))
            )
        }
    }
}

@Stable
open class HorizontalPagingStateFooter : PagingStateFooter() {

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

@Composable
fun <K, V> pagingComponent(vararg item: V): PagingComponent<K, V> = object : PagingComponent<K, V> {

    override val pageData: PageData<K, V> = PageData(
        coroutineScope = MainScope(),
        onRequest = { LoadResult.Page(listOf(*item), null) }
    )
}