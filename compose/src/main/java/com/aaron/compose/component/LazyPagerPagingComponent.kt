package com.aaron.compose.component

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import com.aaron.compose.paging.PageData
import com.aaron.compose.paging.PagingScope
import com.aaron.compose.safestate.SafeState
import com.aaron.compose.safestate.SafeStateScope
import com.aaron.compose.safestate.safeStateOf
import com.aaron.compose.ui.refresh.SmartRefreshType
import com.aaron.compose.ui.refresh.SmartRefreshType.Idle
import com.google.accompanist.pager.PagerDefaults
import com.google.accompanist.pager.PagerScope
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/12/8
 */

@Composable
fun <K, V> LazyPagerPagingComponent(
    component: LazyPagerPagingComponent<K, V>,
    modifier: Modifier = Modifier,
    activeState: Lifecycle.State = Lifecycle.State.RESUMED,
    state: PagerState = rememberPagerState(),
    reverseLayout: Boolean = false,
    itemSpacing: Dp = 0.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    flingBehavior: FlingBehavior = PagerDefaults.flingBehavior(
        state = state,
        endContentPadding = contentPadding.calculateEndPadding(LayoutDirection.Ltr),
    ),
    key: ((page: Int) -> Any)? = null,
    userScrollEnabled: Boolean = true,
    content: @Composable PagerScope.(page: Int, lazyPagingComponent: LazyPagingComponent<K, V>) -> Unit
) {
    val lazyPagingComponents by component.lazyPagingComponents
    LazyPagerComponent(
        components = lazyPagingComponents,
        modifier = modifier,
        activeState = activeState,
        state = state,
        reverseLayout = reverseLayout,
        itemSpacing = itemSpacing,
        contentPadding = contentPadding,
        verticalAlignment = verticalAlignment,
        flingBehavior = flingBehavior,
        key = key,
        userScrollEnabled = userScrollEnabled
    ) { page ->
        content(page, lazyPagingComponents[page])
    }
}

@Stable
interface LazyPagingComponent<K, V> : PagingComponent<K, V>,
    LazyComponent, RefreshComponent, SafeStateScope {

    val finishRefreshDelayMillis: Long get() = 0

    override val isInitialized: Boolean
        get() = initialized.value

    override fun initialize() {
        pagingRefresh()
    }

    override fun refreshIgnoreAnimation() {
        pagingRefresh()
    }

    override fun pagingRefresh() {
        val initialized = initialized
        val finishRefreshDelayMillis = finishRefreshDelayMillis
        pageData.refresh(
            onSuccess = {
                initialized.setValue(true)
                finishRefresh(true, finishRefreshDelayMillis)
            },
            onFailure = {
                initialized.setValue(true)
                finishRefresh(false, finishRefreshDelayMillis)
            }
        )
    }
}

@Stable
interface LazyPagerPagingComponent<K, V> : PagingScope, SafeStateScope {

    val lazyPagingComponents: SafeState<LazyPagingComponents<K, V>>
}

open class LazyPagingComponentHelper<K, V>(
    final override val pageData: PageData<K, V>,
    override val finishRefreshDelayMillis: Long = 0L
) : LazyPagingComponent<K, V> {

    override val initialized: SafeState<Boolean> = safeStateOf(pageData.isInitialized)

    override val smartRefreshType: SafeState<SmartRefreshType> = safeStateOf(Idle)

    init {
        if (!pageData.lazyLoad) {
            error("PageData must be lazy loading.")
        }
    }
}

@Composable
fun <K, V> lazyPagerPagingComponent(
    lazyPagingComponents: ImmutableList<LazyPagingComponent<K, V>> = persistentListOf()
): LazyPagerPagingComponent<K, V> = object : LazyPagerPagingComponent<K, V> {

    override val lazyPagingComponents: SafeState<LazyPagingComponents<K, V>> =
        safeStateOf(lazyPagingComponents)
}

typealias LazyPagingComponents<K, V> = ImmutableList<LazyPagingComponent<K, V>>