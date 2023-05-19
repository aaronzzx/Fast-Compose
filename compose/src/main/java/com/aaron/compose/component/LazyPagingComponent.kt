package com.aaron.compose.component

import androidx.compose.runtime.Stable
import com.aaron.compose.paging.LazyPagingScope
import com.aaron.compose.paging.LoadResult
import com.aaron.compose.paging.PageData
import com.aaron.compose.safestate.SafeState
import com.aaron.compose.safestate.SafeStateScope
import com.aaron.compose.safestate.safeStateOf
import com.aaron.compose.ui.refresh.SmartRefreshType
import kotlinx.coroutines.MainScope

/**
 * 分页数据懒加载组件接口，实现了刷新。
 */
@Stable
interface LazyPagingComponent<K, V> : PagingComponent<K, V>,
    LazyComponent, RefreshComponent, SafeStateScope, LazyPagingScope {

    override val isInitialized: Boolean
        get() = initialized.value

    override fun initialize() {
        pagingRefresh()
    }

    override fun refreshIgnoreAnimation() {
        pagingRefresh()
    }

    override fun pagingRefresh() {
        pageData.refresh(
            onSuccess = {
                finishRefresh(true)
            },
            onFailure = {
                finishRefresh(false)
            }
        )
    }
}

/**
 * 分页懒加载接口实现类，封装 [PageData] ，将 PageData 转换为 [LazyPagingComponent] 。
 */
open class LazyPagingComponentHelper<K, V>(
    final override val pageData: PageData<K, V>
) : LazyPagingComponent<K, V> {

    override val initialized: SafeState<Boolean> = safeStateOf(false)

    override val smartRefreshType: SafeState<SmartRefreshType> = safeStateOf(SmartRefreshType.Idle)

    init {
        if (!pageData.lazyLoad) {
            error("PageData must be lazy loading.")
        }
    }
}

/**
 * 用于 Compose 预览的参数占位。
 */
fun <K, V> lazyPagingComponent(
    vararg item: V
): LazyPagingComponent<K, V> = object : LazyPagingComponent<K, V> {
    override val initialized: SafeState<Boolean> = safeStateOf(false)

    override val smartRefreshType: SafeState<SmartRefreshType> = safeStateOf(SmartRefreshType.Idle)

    override val pageData: PageData<K, V> = PageData(
        coroutineScope = MainScope(),
        onRequest = { LoadResult.Page(listOf(*item), null) }
    )
}