package com.aaron.compose.ui

import android.content.Context
import android.view.View
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.OverscrollConfiguration
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.aaron.compose.R
import com.aaron.compose.ktx.canScrollVertical
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshFooter
import com.scwang.smart.refresh.layout.api.RefreshHeader
import com.scwang.smart.refresh.layout.listener.ScrollBoundaryDecider

object SmartRefreshDefaults {

    val defaultHeader: (Context) -> RefreshHeader = { context: Context -> ClassicsHeader(context) }
    val defaultFooter: (Context) -> RefreshFooter = { context: Context -> ClassicsFooter(context) }
}

@Composable
fun rememberSmartRefreshState(isRefreshing: Boolean): SmartRefreshState = remember {
    SmartRefreshState(isRefreshing)
}

@Composable
fun SmartRefreshList(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    state: SmartRefreshState = rememberSmartRefreshState(false),
    refreshEnabled: Boolean = true,
    onLoadMore: (() -> Unit)? = null,
    loadMoreEnabled: Boolean = onLoadMore != null,
    header: (Context) -> RefreshHeader = SmartRefreshDefaults.defaultHeader,
    footer: (Context) -> RefreshFooter = SmartRefreshDefaults.defaultFooter,
    listConfig: LazyListConfig = remember { LazyListConfig() },
    listState: LazyListState = rememberLazyListState(),
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    content: LazyListScope.() -> Unit
) {
    BaseSmartRefresh(
        onRefresh = onRefresh,
        canRefresh = {
            refreshEnabled && !listState.canScrollVertical(-1)
        },
        modifier = modifier,
        state = state,
        refreshEnabled = refreshEnabled,
        loadMoreEnabled = loadMoreEnabled,
        onLoadMore = onLoadMore,
        canLoadMore = {
            loadMoreEnabled && !listState.canScrollVertical(1)
        },
        header = header,
        footer = footer
    ) {
        val overScroll = if (!refreshEnabled && !loadMoreEnabled) OverscrollConfiguration() else null
        CompositionLocalProvider(
            LocalOverscrollConfiguration provides overScroll
        ) {
            LazyColumn(
                modifier = listConfig.modifier,
                state = listState,
                contentPadding = listConfig.contentPadding,
                reverseLayout = listConfig.reverseLayout,
                verticalArrangement = listConfig.verticalArrangement,
                horizontalAlignment = listConfig.horizontalAlignment,
                flingBehavior = flingBehavior,
                userScrollEnabled = listConfig.userScrollEnabled
            ) {
                content()
            }
        }
    }
}

@Composable
fun SmartRefreshGrid(
    columns: GridCells,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    state: SmartRefreshState = rememberSmartRefreshState(false),
    refreshEnabled: Boolean = true,
    onLoadMore: (() -> Unit)? = null,
    loadMoreEnabled: Boolean = onLoadMore != null,
    header: (Context) -> RefreshHeader = SmartRefreshDefaults.defaultHeader,
    footer: (Context) -> RefreshFooter = SmartRefreshDefaults.defaultFooter,
    listConfig: LazyGridConfig = remember { LazyGridConfig() },
    listState: LazyGridState = rememberLazyGridState(),
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    content: LazyGridScope.() -> Unit
) {
    BaseSmartRefresh(
        onRefresh = onRefresh,
        canRefresh = {
            refreshEnabled && !listState.canScrollVertical(-1)
        },
        modifier = modifier,
        state = state,
        refreshEnabled = refreshEnabled,
        loadMoreEnabled = loadMoreEnabled,
        onLoadMore = onLoadMore,
        canLoadMore = {
            loadMoreEnabled && !listState.canScrollVertical(1)
        },
        header = header,
        footer = footer
    ) {
        val overScroll = if (!refreshEnabled && !loadMoreEnabled) OverscrollConfiguration() else null
        CompositionLocalProvider(
            LocalOverscrollConfiguration provides overScroll
        ) {
            LazyVerticalGrid(
                columns = columns,
                modifier = listConfig.modifier,
                state = listState,
                contentPadding = listConfig.contentPadding,
                reverseLayout = listConfig.reverseLayout,
                verticalArrangement = listConfig.verticalArrangement,
                horizontalArrangement = listConfig.horizontalArrangement,
                flingBehavior = flingBehavior,
                userScrollEnabled = listConfig.userScrollEnabled
            ) {
                content()
            }
        }
    }
}

@Composable
fun SmartRefresh(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    state: SmartRefreshState = rememberSmartRefreshState(false),
    refreshEnabled: Boolean = true,
    onLoadMore: (() -> Unit)? = null,
    loadMoreEnabled: Boolean = onLoadMore != null,
    header: (Context) -> RefreshHeader = SmartRefreshDefaults.defaultHeader,
    footer: (Context) -> RefreshFooter = SmartRefreshDefaults.defaultFooter,
    listConfig: ScrollConfig = remember { ScrollConfig() },
    listState: ScrollState = rememberScrollState(),
    flingBehavior: FlingBehavior? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable () -> Unit
) {
    BaseSmartRefresh(
        onRefresh = onRefresh,
        canRefresh = {
            refreshEnabled && listState.value == 0
        },
        modifier = modifier,
        state = state,
        refreshEnabled = refreshEnabled,
        loadMoreEnabled = loadMoreEnabled,
        onLoadMore = onLoadMore,
        canLoadMore = {
            loadMoreEnabled && listState.value == listState.maxValue
        },
        header = header,
        footer = footer
    ) {
        val overScroll = if (!refreshEnabled && !loadMoreEnabled) OverscrollConfiguration() else null
        CompositionLocalProvider(
            LocalOverscrollConfiguration provides overScroll
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        state = listState,
                        enabled = listConfig.enabled,
                        flingBehavior = flingBehavior,
                        reverseScrolling = listConfig.reverseScrolling
                    )
                    .padding(contentPadding)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun BaseSmartRefresh(
    onRefresh: () -> Unit,
    canRefresh: () -> Boolean,
    modifier: Modifier = Modifier,
    state: SmartRefreshState = rememberSmartRefreshState(false),
    refreshEnabled: Boolean = true,
    loadMoreEnabled: Boolean = false,
    onLoadMore: (() -> Unit)? = null,
    canLoadMore: (() -> Boolean)? = null,
    header: (Context) -> RefreshHeader = SmartRefreshDefaults.defaultHeader,
    footer: (Context) -> RefreshFooter = SmartRefreshDefaults.defaultFooter,
    content: @Composable () -> Unit
) {
    val curOnRefresh by rememberUpdatedState(onRefresh)
    val curOnLoadMore by rememberUpdatedState(onLoadMore)
    val curCanRefresh by rememberUpdatedState(canRefresh)
    val curCanLoadMore by rememberUpdatedState(canLoadMore)
    val curHeader by rememberUpdatedState(header)
    val curFooter by rememberUpdatedState(footer)
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            SmartRefreshLayout(context).apply {
                setRefreshHeader(curHeader(context))
                setRefreshFooter(curFooter(context))
                setOnRefreshListener {
                    state.isRefreshing = true
                    curOnRefresh()
                }
                setOnLoadMoreListener {
                    state.isLoading = true
                    curOnLoadMore?.invoke()
                }
                setScrollBoundaryDecider(object : ScrollBoundaryDecider {
                    override fun canRefresh(content: View): Boolean {
                        return curCanRefresh()
                    }

                    override fun canLoadMore(content: View): Boolean {
                        return curCanLoadMore?.invoke() ?: false
                    }
                })
            }
        }
    ) { refreshLayout ->
        refreshLayout.setEnableRefresh(refreshEnabled)
        refreshLayout.setEnableLoadMore(loadMoreEnabled)
        with(state) {
            autoRefresh?.also {
                if (it.recycled) return@also
                if (it.animateOnly) {
                    refreshLayout.autoRefreshAnimationOnly()
                } else {
                    refreshLayout.autoRefresh()
                }
                it.recycled = true
            }
            autoLoadMore?.also {
                if (it.recycled) return@also
                if (it.animateOnly) {
                    refreshLayout.autoRefreshAnimationOnly()
                } else {
                    refreshLayout.autoRefresh()
                }
                it.recycled = true
            }
            finishRefresh?.also {
                if (it.recycled) return@also
                isRefreshing = false
                refreshLayout.finishRefresh(it.delayed, it.success, it.noMoreData)
                it.recycled = true
            }
            finishLoadMore?.also {
                if (it.recycled) return@also
                isLoading = false
                refreshLayout.finishLoadMore(it.delayed, it.success, it.noMoreData)
                it.recycled = true
            }
            resetNoMoreData?.also {
                if (it.recycled) return@also
                refreshLayout.resetNoMoreData()
                it.recycled = true
            }
        }
        getOrCreateComposeView(refreshLayout).setContent(content)
    }
}

private fun getOrCreateComposeView(refreshLayout: SmartRefreshLayout): ComposeView {
    var composeView = refreshLayout.findViewById<ComposeView>(R.id.refresh_layout_compose_view)
    if (composeView == null) {
        composeView = ComposeView(refreshLayout.context).also {
            it.id = R.id.refresh_layout_compose_view
        }
        refreshLayout.setRefreshContent(composeView)
    }
    return composeView
}

@Stable
class SmartRefreshState(isRefreshing: Boolean) {

    var isRefreshing: Boolean by mutableStateOf(false)
        internal set

    var isLoading: Boolean by mutableStateOf(false)
        internal set

    internal var autoRefresh: AutoRefresh? by mutableStateOf(null)
    internal var autoLoadMore: AutoLoadMore? by mutableStateOf(null)
    internal var finishRefresh: FinishRefresh? by mutableStateOf(null)
    internal var finishLoadMore: FinishLoadMore? by mutableStateOf(null)
    internal var resetNoMoreData: ResetNoMoreData? by mutableStateOf(null)

    init {
        if (isRefreshing) {
            autoRefresh()
        }
    }

    fun autoRefresh(animateOnly: Boolean = false) {
        if (!isRefreshing && !isLoading) {
            autoRefresh = AutoRefresh(animateOnly)
        }
    }

    fun autoLoadMore(animateOnly: Boolean = false) {
        if (!isRefreshing && !isLoading) {
            autoLoadMore = AutoLoadMore(animateOnly)
        }
    }

    fun finishRefresh(success: Boolean, noMoreData: Boolean = false, delayed: Int = 0) {
        if (isRefreshing) {
            finishRefresh = FinishRefresh(success, noMoreData, delayed)
        }
    }

    fun finishLoadMore(success: Boolean, noMoreData: Boolean = false, delayed: Int = 0) {
        if (isLoading) {
            finishLoadMore = FinishLoadMore(success, noMoreData, delayed)
        }
    }

    fun resetNoMoreData() {
        resetNoMoreData = ResetNoMoreData()
    }

    @Stable
    internal sealed class Action {

        internal var recycled = false
    }

    @Stable
    internal class AutoRefresh(val animateOnly: Boolean) : Action()

    @Stable
    internal class AutoLoadMore(val animateOnly: Boolean) : Action()

    @Stable
    internal class FinishRefresh(val success: Boolean, val noMoreData: Boolean, val delayed: Int) : Action()

    @Stable
    internal class FinishLoadMore(val success: Boolean, val noMoreData: Boolean, val delayed: Int) : Action()

    @Stable
    internal class ResetNoMoreData : Action()
}

@Stable
class LazyListConfig(
    val modifier: Modifier = Modifier,
    val contentPadding: PaddingValues = PaddingValues(0.dp),
    val reverseLayout: Boolean = false,
    val verticalArrangement: Arrangement.Vertical = if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    val horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    val userScrollEnabled: Boolean = true
)

@Stable
class LazyGridConfig(
    val modifier: Modifier = Modifier,
    val contentPadding: PaddingValues = PaddingValues(0.dp),
    val reverseLayout: Boolean = false,
    val verticalArrangement: Arrangement.Vertical = if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    val horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    val userScrollEnabled: Boolean = true
)

@Stable
class ScrollConfig(
    val enabled: Boolean = true,
    val reverseScrolling: Boolean = false
)