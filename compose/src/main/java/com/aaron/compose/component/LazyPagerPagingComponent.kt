package com.aaron.compose.component

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import com.aaron.compose.ktx.rememberLazyPagingComponents
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
fun <T, K, V> LazyPagerPagingComponent(
    component: LazyPagerPagingComponent<T, K, V>,
    modifier: Modifier = Modifier,
    activeState: Lifecycle.State = Lifecycle.State.RESUMED,
    pagerState: PagerState = rememberPagerState(),
    reverseLayout: Boolean = false,
    itemSpacing: Dp = 0.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    flingBehavior: FlingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        endContentPadding = contentPadding.calculateEndPadding(LayoutDirection.Ltr),
    ),
    key: ((page: Int) -> Any)? = null,
    userScrollEnabled: Boolean = true,
    content: @Composable PagerScope.(lazyPagingComponent: LazyPagingComponent<K, V>) -> Unit
) {
    val lazyPagingComponents = component.rememberLazyPagingComponents()
    LazyPagerComponent(
        components = lazyPagingComponents,
        modifier = modifier,
        activeState = activeState,
        pagerState = pagerState,
        reverseLayout = reverseLayout,
        itemSpacing = itemSpacing,
        contentPadding = contentPadding,
        verticalAlignment = verticalAlignment,
        flingBehavior = flingBehavior,
        key = key,
        userScrollEnabled = userScrollEnabled
    ) { page ->
        val lazyPagingComponent = lazyPagingComponents[page]
        content(lazyPagingComponent)
    }
}

@Stable
interface LazyPagingComponent<K, V> : PagingComponent<K, V>,
    LazyComponent, RefreshComponent, SafeStateScope {

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
        pageData.refresh(
            onSuccess = {
                initialized.setValue(true)
                finishRefresh(true)
            },
            onFailure = {
                initialized.setValue(true)
                finishRefresh(false)
            }
        )
    }
}

/**
 * T 代表 Tab ，K 代表分页 key ，V 代表数据
 */
@Stable
interface LazyPagerPagingComponent<T, K, V> : PagingScope, SafeStateScope {

    val lazyPagingData: SafeState<LazyPagingData<T, K, V>>
}

open class LazyPagingComponentHelper<K, V>(
    final override val pageData: PageData<K, V>
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
fun <T, K, V> lazyPagerPagingComponent(
    lazyPagingData: LazyPagingData<T, K, V> = persistentListOf()
): LazyPagerPagingComponent<T, K, V> = object : LazyPagerPagingComponent<T, K, V> {

    override val lazyPagingData: SafeState<LazyPagingData<T, K, V>> =
        safeStateOf(lazyPagingData)
}

typealias LazyPagingData<T, K, V> = ImmutableList<Pair<T, LazyPagingComponent<K, V>>>