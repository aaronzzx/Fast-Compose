package com.aaron.compose.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.aaron.compose.R
import com.aaron.compose.ktx.isEmpty
import com.aaron.compose.ktx.isNotEmpty
import com.aaron.compose.ktx.lastIndex
import com.aaron.compose.ktx.onSingleClick
import com.aaron.compose.ktx.roundToPx
import com.aaron.compose.ktx.toDp
import com.aaron.compose.ktx.toPx
import com.aaron.compose.paging.CombinedLoadState
import com.aaron.compose.paging.LoadResult
import com.aaron.compose.paging.LoadState
import com.aaron.compose.paging.PageData
import com.aaron.compose.paging.PagingScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

/**
 * 默认的竖向分页列表组件，效果如 [androidx.recyclerview.widget.LinearLayoutManager] 。
 *
 * @param component 实现的分页组件，一般由 [androidx.lifecycle.ViewModel] 进行实现。
 * @param pagingStateFooter 实现分页加载状态的 UI 。
 * @param headerContent 列表的头部，跟随列表滑动。
 * @param footerContent 列表的尾部，跟随列表滑动。
 * @param loadingContent 视图加载。
 * @param emptyContent 空数据视图。
 * @param errorContent 请求错误视图。
 */
@Composable
fun <E> PagingComponent(
    component: PagingComponent<E>,
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
    loadingContent: (@Composable BoxScope.() -> Unit)? = {
        PagingLoading(modifier = Modifier.matchParentSize())
    },
    emptyContent: (@Composable BoxScope.() -> Unit)? = {
        PagingEmpty(modifier = Modifier.matchParentSize())
    },
    errorContent: (@Composable BoxScope.() -> Unit)? = {
        PagingError(
            modifier = Modifier.matchParentSize(),
            onClick = { component.pagingRefresh() }
        )
    },
    content: LazyListScope.(PageData<*, E>) -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        val listHeightPixels = maxHeight.roundToPx()
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
                modifier = Modifier.matchParentSize(),
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
                    verticalItemSpacing = verticalArrangement.spacing,
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
 * 竖向网格分页列表组件，效果如 [androidx.recyclerview.widget.GridLayoutManager] 。
 *
 * @param component 实现的分页组件，一般由 [androidx.lifecycle.ViewModel] 进行实现。
 * @param pagingStateFooter 实现分页加载状态的 UI 。
 * @param headerContent 列表的头部，跟随列表滑动。
 * @param footerContent 列表的尾部，跟随列表滑动。
 * @param loadingContent 视图加载。
 * @param emptyContent 空数据视图。
 * @param errorContent 请求错误视图。
 */
@Composable
fun <E> PagingGridComponent(
    component: PagingComponent<E>,
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
    loadingContent: (@Composable BoxScope.() -> Unit)? = {
        PagingLoading(modifier = Modifier.matchParentSize())
    },
    emptyContent: (@Composable BoxScope.() -> Unit)? = {
        PagingEmpty(modifier = Modifier.matchParentSize())
    },
    errorContent: (@Composable BoxScope.() -> Unit)? = {
        PagingError(
            modifier = Modifier.matchParentSize(),
            onClick = { component.pagingRefresh() }
        )
    },
    content: LazyGridScope.(PageData<*, E>) -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        val listHeightPixels = maxHeight.roundToPx()
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
                modifier = Modifier.matchParentSize(),
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
                    verticalItemSpacing = verticalArrangement.spacing,
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
 * 竖向瀑布流分页列表组件，效果如 [androidx.recyclerview.widget.StaggeredGridLayoutManager] 。
 *
 * @param component 实现的分页组件，一般由 [androidx.lifecycle.ViewModel] 进行实现。
 * @param verticalItemSpacing 子项之间的垂直间距。
 * @param pagingStateFooter 实现分页加载状态的 UI 。
 * @param headerContent 列表的头部，跟随列表滑动。
 * @param footerContent 列表的尾部，跟随列表滑动。
 * @param loadingContent 视图加载。
 * @param emptyContent 空数据视图。
 * @param errorContent 请求错误视图。
 */
@Composable
fun <E> PagingStaggeredGridComponent(
    component: PagingComponent<E>,
    columns: StaggeredGridCells,
    modifier: Modifier = Modifier,
    state: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalItemSpacing: Dp = 0.dp,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(0.dp),
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    pagingStateFooter: PagingStateFooter? = PagingComponentDefaults.verticalPagingStateFooter,
    headerContent: (@Composable () -> Unit)? = null,
    footerContent: (@Composable () -> Unit)? = null,
    loadingContent: (@Composable BoxScope.() -> Unit)? = {
        PagingLoading(modifier = Modifier.matchParentSize())
    },
    emptyContent: (@Composable BoxScope.() -> Unit)? = {
        PagingEmpty(modifier = Modifier.matchParentSize())
    },
    errorContent: (@Composable BoxScope.() -> Unit)? = {
        PagingError(
            modifier = Modifier.matchParentSize(),
            onClick = { component.pagingRefresh() }
        )
    },
    content: LazyStaggeredGridScope.(PageData<*, E>) -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        val listHeightPixels = maxHeight.roundToPx()
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
            LazyVerticalStaggeredGrid(
                modifier = Modifier.matchParentSize(),
                columns = columns,
                state = state,
                contentPadding = contentPadding,
                verticalItemSpacing = verticalItemSpacing,
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
                    verticalItemSpacing = verticalItemSpacing,
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

@Composable
private fun <E> BoxScope.LoadingBox(
    component: PagingComponent<E>,
    refreshLoading: Boolean,
    loadingContent: (@Composable BoxScope.() -> Unit)?,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = Modifier.matchParentSize()) {
        content()
        if (loadingContent != null
            && (component.isInitialized.not() || refreshLoading)
        ) {
            loadingContent()
        }
    }
}

/**
 * 横向分页。
 *
 * @param component 实现的分页组件，一般由 [androidx.lifecycle.ViewModel] 进行实现。
 */
@Composable
fun <E> PagingHorizontalComponent(
    component: PagingComponent<E>,
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
    content: LazyListScope.(PageData<*, E>) -> Unit
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
                .filter { !state.isScrollInProgress && !state.canScrollForward }
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
                    isDragged && !state.canScrollForward
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
                component: PagingComponent<*>,
                content: (@Composable (PagingComponent<*>) -> Unit)?
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

private fun <E> handleCentralContent(
    scope: Any,
    component: PagingComponent<E>,
    refreshLoading: Boolean,
    refreshError: Boolean,
    listHeightPixels: Int,
    headerHeightPixels: Int,
    footerHeightPixels: Int,
    contentPadding: PaddingValues,
    verticalItemSpacing: Dp,
    headerContent: (@Composable () -> Unit)?,
    footerContent: (@Composable () -> Unit)?,
    loadingContent: (@Composable BoxScope.() -> Unit)?,
    emptyContent: (@Composable BoxScope.() -> Unit)?,
    errorContent: (@Composable BoxScope.() -> Unit)?,
    content: (PageData<*, E>) -> Unit,
) {
    val centralContent: @Composable (content: @Composable BoxScope.() -> Unit) -> Unit = {
        CentralContent(
            contentPadding = contentPadding,
            verticalItemSpacing = verticalItemSpacing,
            existsHeader = headerContent != null,
            existsFooter = footerContent != null,
            listHeightPixels = listHeightPixels,
            headerHeightPixels = headerHeightPixels,
            footerHeightPixels = footerHeightPixels
        ) {
            it()
        }
    }
    val pageData = component.pageData
    if (loadingContent != null
        && pageData.isEmpty
        && (component.isInitialized.not() || refreshLoading)
    ) {
        itemCentral(
            scope = scope,
            key = "${component}-Loading",
            contentType = "Loading"
        ) {
            centralContent {}
        }
    } else if (errorContent != null && refreshError && pageData.isEmpty) {
        itemCentral(
            scope = scope,
            key = "${component}-Error",
            contentType = "Error"
        ) {
            centralContent {
                errorContent()
            }
        }
    } else if (emptyContent != null && pageData.isEmpty) {
        itemCentral(
            scope = scope,
            key = "${component}-Empty",
            contentType = "Empty"
        ) {
            centralContent {
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
    } else if (scope is LazyStaggeredGridScope) {
        with(scope) {
            item(
                span = StaggeredGridItemSpan.FullLine,
                key = key,
                contentType = contentType
            ) {
                content()
            }
        }
    }
}

private fun <E> trySetPagingStateFooter(
    scope: Any,
    component: PagingComponent<E>,
    pagingFooterType: PagingFooterType,
    pagingStateFooter: PagingStateFooter
) {
    fun trySetFooter(
        scope: Any,
        component: PagingComponent<*>,
        content: (@Composable (PagingComponent<*>) -> Unit)?
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
            } else if (scope is LazyStaggeredGridScope) {
                with(scope) {
                    item(
                        span = StaggeredGridItemSpan.FullLine,
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
                    slotId = slotId,
                    onHeightChange = onHeightChange,
                    content = content
                )
            }
        }
    } else if (scope is LazyStaggeredGridScope) {
        with(scope) {
            item(
                span = StaggeredGridItemSpan.FullLine,
                key = key,
                contentType = contentType
            ) {
                MeasureHeightContent(
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

/**
 * 处理当数据刷新时，自动返回列表顶部。
 */
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

/**
 * 页面正在加载的视图实现。
 */
@Composable
fun PagingLoading(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    color: Color = MaterialTheme.colors.primary,
    strokeWidth: Dp = ProgressIndicatorDefaults.StrokeWidth
) {
    Box(
        modifier = modifier.padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = color,
            strokeWidth = strokeWidth
        )
    }
}

/**
 * 无数据的视图实现。
 */
@Composable
fun PagingEmpty(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    contentOffset: DpOffset = DpOffset.Zero,
    text: String = stringResource(id = R.string.compose_component_empty_data),
    onClick: (() -> Unit)? = null,
    enableClickRipple: Boolean = true,
    backgroundColor: Color = Color.White,
    shape: Shape = RoundedCornerShape(8.dp),
    @DrawableRes iconRes: Int? = null,
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
        contentPadding = contentPadding,
        contentOffset = contentOffset,
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

/**
 * 请求错误的视图实现。
 */
@Composable
fun PagingError(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    contentOffset: DpOffset = DpOffset.Zero,
    text: String = stringResource(R.string.compose_component_load_failed),
    onClick: (() -> Unit)? = null,
    enableClickRipple: Boolean = true,
    backgroundColor: Color = Color.White,
    shape: Shape = RoundedCornerShape(8.dp),
    @DrawableRes iconRes: Int? = null,
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
        contentPadding = contentPadding,
        contentOffset = contentOffset,
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
    verticalItemSpacing: Dp,
    existsHeader: Boolean,
    existsFooter: Boolean,
    listHeightPixels: Int,
    headerHeightPixels: Int,
    footerHeightPixels: Int,
    content: @Composable BoxScope.() -> Unit
) {
    val verticalPaddingPx = with(contentPadding) {
        calculateTopPadding() + calculateBottomPadding()
    }.toPx()
    val spacingPx = verticalItemSpacing.toPx()
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

/**
 * 获取当前分页加载状态
 */
@Composable
fun <E> rememberPagingFooterType(component: PagingComponent<E>): State<PagingFooterType> {
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

/**
 * 各种分页加载状态
 */
enum class PagingFooterType {

    None, Loading, LoadMore, LoadError, NoMoreData, WaitingRefresh
}

/**
 * 全局共用的分页加载状态页脚
 */
object PagingComponentDefaults {

    var verticalPagingStateFooter: PagingStateFooter? = VerticalPagingStateFooter()

    var horizontalPagingStateFooter: PagingStateFooter? = HorizontalPagingStateFooter()
}

/**
 * 分页状态页脚，显示各种加载状态。
 *
 * [loading] 实现正在加载的 UI ；
 *
 * [loadMore] 实现需要点击加载的 UI ；
 *
 * [loadError] 实现加载错误的 UI ；
 *
 * [noMoreData] 实现暂无更多数据的 UI ；
 *
 * [waitingRefresh] 实现当准备进行加载时但此时正在刷新的 UI ，需要等待刷新结束。
 */
@Stable
abstract class PagingStateFooter {

    abstract val loading: (@Composable (PagingComponent<*>) -> Unit)?
    abstract val loadMore: (@Composable (PagingComponent<*>) -> Unit)?
    abstract val loadError: (@Composable (PagingComponent<*>) -> Unit)?
    abstract val noMoreData: (@Composable (PagingComponent<*>) -> Unit)?
    abstract val waitingRefresh: (@Composable (PagingComponent<*>) -> Unit)?
}

/**
 * 用于竖向列表的分页加载状态页脚。
 */
@Stable
open class VerticalPagingStateFooter : PagingStateFooter() {

    override val loading: (@Composable (PagingComponent<*>) -> Unit)? = {
        FooterText(
            text = stringResource(id = R.string.compose_component_loading),
            component = it,
            footerType = PagingFooterType.Loading
        )
    }
    override val loadMore: (@Composable (PagingComponent<*>) -> Unit)? = {
        FooterText(
            text = stringResource(id = R.string.compose_component_load_more),
            component = it,
            footerType = PagingFooterType.LoadMore
        )
    }
    override val loadError: (@Composable (PagingComponent<*>) -> Unit)? = {
        FooterText(
            stringResource(id = R.string.compose_component_load_failed),
            component = it,
            footerType = PagingFooterType.LoadError
        )
    }
    override val noMoreData: (@Composable (PagingComponent<*>) -> Unit)? = {
        CommonNoMoreDataContent(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        )
    }
    override val waitingRefresh: (@Composable (PagingComponent<*>) -> Unit)? = {
        FooterText(
            stringResource(id = R.string.compose_component_load_wait),
            component = it,
            footerType = PagingFooterType.WaitingRefresh
        )
    }

    @Composable
    fun CommonNoMoreDataContent(
        modifier: Modifier = Modifier,
        text: String = stringResource(id = R.string.compose_component_load_end),
        fontSize: TextUnit = 12.sp,
        fontColor: Color = Color(0xFF999999),
        fontWeight: FontWeight? = null
    ) {
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
                text = text,
                fontSize = fontSize,
                color = fontColor,
                fontWeight = fontWeight
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

/**
 * 用于横向列表的分页加载状态页脚。
 */
@Stable
open class HorizontalPagingStateFooter : PagingStateFooter() {

    override val loading: @Composable (PagingComponent<*>) -> Unit = {
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
    override val loadMore: ((PagingComponent<*>) -> Unit)? = null
    override val loadError: ((PagingComponent<*>) -> Unit)? = null
    override val noMoreData: ((PagingComponent<*>) -> Unit)? = null
    override val waitingRefresh: ((PagingComponent<*>) -> Unit)? = null
}

@Composable
private fun FooterText(
    text: String,
    component: PagingComponent<*>,
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
                    PagingFooterType.LoadMore -> it.onSingleClick(enableRipple = false) {
                        component.pagingLoadMore()
                    }
                    PagingFooterType.LoadError -> it.onSingleClick(enableRipple = false) {
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
 * 分页状态组件，默认情况下只需实现 [PageData] ，通过 [PagingScope] 内的已实现函数来
 * 构建 [PageData] ，如 [buildPageData] 、[buildMappingPageData] 。
 */
@Stable
interface PagingComponent<E> : PagingScope {

    val pageData: PageData<*, E>

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

/**
 * 用于 Compose 预览的参数占位。
 */
fun <E> pagingComponent(vararg item: E): PagingComponent<E> = object : PagingComponent<E> {

    override val pageData: PageData<*, E> = PageData(
        coroutineScope = MainScope(),
        onRequest = { LoadResult.Page(listOf(*item), null) }
    )
}