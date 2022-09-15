package com.aaron.compose.ui

import android.content.Context
import android.view.View
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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.aaron.compose.R
import com.aaron.compose.utils.OverScrollHandler
import com.aaron.compose.ktx.canScrollVertical
import com.aaron.compose.ui.SmartRefreshState.AutoLoadMore
import com.aaron.compose.ui.SmartRefreshState.AutoRefresh
import com.aaron.compose.ui.SmartRefreshState.FinishLoadMore
import com.aaron.compose.ui.SmartRefreshState.FinishRefresh
import com.aaron.compose.ui.SmartRefreshState.ResetNoMoreData
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshFooter
import com.scwang.smart.refresh.layout.api.RefreshHeader
import com.scwang.smart.refresh.layout.listener.ScrollBoundaryDecider

@Composable
fun SmartRefreshList(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    refreshState: SmartRefreshState = rememberSmartRefreshState(false),
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
        refreshState = refreshState,
        refreshEnabled = refreshEnabled,
        loadMoreEnabled = loadMoreEnabled,
        onLoadMore = onLoadMore,
        canLoadMore = {
            loadMoreEnabled && !listState.canScrollVertical(1)
        },
        header = header,
        footer = footer
    ) {
        OverScrollHandler(enabled = !refreshEnabled && !loadMoreEnabled) {
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
    refreshState: SmartRefreshState = rememberSmartRefreshState(false),
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
        refreshState = refreshState,
        refreshEnabled = refreshEnabled,
        loadMoreEnabled = loadMoreEnabled,
        onLoadMore = onLoadMore,
        canLoadMore = {
            loadMoreEnabled && !listState.canScrollVertical(1)
        },
        header = header,
        footer = footer
    ) {
        OverScrollHandler(enabled = !refreshEnabled && !loadMoreEnabled) {
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
    refreshState: SmartRefreshState = rememberSmartRefreshState(false),
    refreshEnabled: Boolean = true,
    onLoadMore: (() -> Unit)? = null,
    loadMoreEnabled: Boolean = onLoadMore != null,
    header: (Context) -> RefreshHeader = SmartRefreshDefaults.defaultHeader,
    footer: (Context) -> RefreshFooter = SmartRefreshDefaults.defaultFooter,
    listConfig: ScrollConfig = remember { ScrollConfig() },
    listState: ScrollState = rememberScrollState(),
    flingBehavior: FlingBehavior? = null,
    content: @Composable () -> Unit
) {
    BaseSmartRefresh(
        onRefresh = onRefresh,
        canRefresh = {
            refreshEnabled && listState.value == 0
        },
        modifier = modifier,
        refreshState = refreshState,
        refreshEnabled = refreshEnabled,
        loadMoreEnabled = loadMoreEnabled,
        onLoadMore = onLoadMore,
        canLoadMore = {
            loadMoreEnabled && listState.value == listState.maxValue
        },
        header = header,
        footer = footer
    ) {
        OverScrollHandler(enabled = !refreshEnabled && !loadMoreEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        state = listState,
                        enabled = listConfig.enabled,
                        flingBehavior = flingBehavior,
                        reverseScrolling = listConfig.reverseScrolling
                    )
                    .padding(listConfig.contentPadding)
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
    refreshState: SmartRefreshState = rememberSmartRefreshState(false),
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
            ComposeNestedRefreshLayout(context).apply {
                setRefreshHeader(curHeader(context))
                setRefreshFooter(curFooter(context))
                setOnRefreshListener {
                    refreshState.isRefreshing = true
                    curOnRefresh()
                }
                setOnLoadMoreListener {
                    refreshState.isLoading = true
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
        refreshState.action?.use {
            when (it) {
                is AutoRefresh -> if (it.animateOnly) {
                    refreshLayout.autoRefreshAnimationOnly()
                } else {
                    refreshLayout.autoRefresh()
                }
                is AutoLoadMore -> if (it.animateOnly) {
                    refreshLayout.autoRefreshAnimationOnly()
                } else {
                    refreshLayout.autoRefresh()
                }
                is FinishRefresh -> {
                    refreshState.isRefreshing = false
                    refreshLayout.finishRefresh(it.delayed, it.success, it.noMoreData)
                }
                is FinishLoadMore -> {
                    refreshState.isLoading = false
                    refreshLayout.finishLoadMore(it.delayed, it.success, it.noMoreData)
                }
                is ResetNoMoreData -> refreshLayout.resetNoMoreData()
            }
        }
        refreshLayout.setContent(content)
    }
}

object SmartRefreshDefaults {

    val defaultHeader: (Context) -> RefreshHeader = { context: Context -> ClassicsHeader(context) }
    val defaultFooter: (Context) -> RefreshFooter = { context: Context -> ClassicsFooter(context) }
}

@Composable
fun rememberSmartRefreshState(
    isRefreshing: Boolean
): SmartRefreshState = remember {
    SmartRefreshState(isRefreshing)
}

@Stable
class SmartRefreshState(isRefreshing: Boolean) {

    var isRefreshing: Boolean by mutableStateOf(false)
        internal set

    var isLoading: Boolean by mutableStateOf(false)
        internal set

    internal var action: Action? by mutableStateOf(null)

    init {
        if (isRefreshing) {
            autoRefresh()
        }
    }

    fun autoRefresh(animateOnly: Boolean = false) {
        if (!isRefreshing && !isLoading) {
            action = AutoRefresh(animateOnly)
        }
    }

    fun autoLoadMore(animateOnly: Boolean = false) {
        if (!isRefreshing && !isLoading) {
            action = AutoLoadMore(animateOnly)
        }
    }

    fun finishRefresh(success: Boolean, noMoreData: Boolean = false, delayed: Int = 0) {
        if (isRefreshing) {
            action = FinishRefresh(success, noMoreData, delayed)
        }
    }

    fun finishLoadMore(success: Boolean, noMoreData: Boolean = false, delayed: Int = 0) {
        if (isLoading) {
            action = FinishLoadMore(success, noMoreData, delayed)
        }
    }

    fun resetNoMoreData() {
        action = ResetNoMoreData()
    }

    @Stable
    internal sealed class Action {

        private var used = false

        inline fun use(block: (Action) -> Unit) {
            if (!used) {
                block(this)
                used = true
            }
        }
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
data class LazyListConfig(
    val modifier: Modifier = Modifier,
    val contentPadding: PaddingValues = PaddingValues(0.dp),
    val reverseLayout: Boolean = false,
    val verticalArrangement: Arrangement.Vertical = if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    val horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    val userScrollEnabled: Boolean = true
)

@Stable
data class LazyGridConfig(
    val modifier: Modifier = Modifier,
    val contentPadding: PaddingValues = PaddingValues(0.dp),
    val reverseLayout: Boolean = false,
    val verticalArrangement: Arrangement.Vertical = if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    val horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    val userScrollEnabled: Boolean = true
)

@Stable
data class ScrollConfig(
    val enabled: Boolean = true,
    val reverseScrolling: Boolean = false,
    val contentPadding: PaddingValues = PaddingValues(0.dp)
)