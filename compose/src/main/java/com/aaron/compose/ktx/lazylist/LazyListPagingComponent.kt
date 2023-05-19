package com.aaron.compose.ktx.lazylist

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridItemScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.runtime.Composable
import com.aaron.compose.component.PagingComponent

/**
 * 基于 [PagingComponent] 扩展，用于
 * [androidx.compose.foundation.lazy.LazyColumn] 、
 *
 * [androidx.compose.foundation.lazy.grid.LazyGrid] 、
 *
 * [androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGrid]
 *
 * @author aaronzzxup@gmail.com
 * @since 2022/12/5
 */

inline fun <K, V> LazyListScope.items(
    component: PagingComponent<K, V>,
    crossinline key: (item: V) -> Any? = { null },
    crossinline contentType: (item: V) -> Any? = { null },
    crossinline itemContent: @Composable LazyItemScope.(item: V) -> Unit
) {
    itemsIndexed(
        component = component,
        key = { _, item -> key(item) },
        contentType = { _, item -> contentType(item) }
    ) { _, item ->
        itemContent(item)
    }
}

inline fun <K, V> LazyListScope.itemsIndexed(
    component: PagingComponent<K, V>,
    crossinline key: (index: Int, item: V) -> Any? = { _, _ -> null },
    crossinline contentType: (index: Int, item: V) -> Any? = { _, _ -> null },
    crossinline itemContent: @Composable LazyItemScope.(index: Int, item: V) -> Unit
) {
    val pageData = component.pageData
    items(
        count = pageData.itemCount,
        key = { index ->
            key(index, pageData.peek(index)) ?: ParcelableKey(index)
        },
        contentType = { index ->
            contentType(index, pageData.peek(index))
        }
    ) { index ->
        itemContent(index, getItem(component, index))
    }
}

inline fun <K, V> LazyGridScope.items(
    component: PagingComponent<K, V>,
    crossinline key: (item: V) -> Any? = { null },
    crossinline contentType: (item: V) -> Any? = { null },
    crossinline span: LazyGridItemSpanScope.(item: V) -> GridItemSpan = { GridItemSpan(1) },
    crossinline itemContent: @Composable LazyGridItemScope.(item: V) -> Unit
) {
    itemsIndexed(
        component = component,
        key = { _, item -> key(item) },
        contentType = { _, item -> contentType(item) },
        span = { _, item -> span(item) }
    ) { _, item ->
        itemContent(item)
    }
}

inline fun <K, V> LazyGridScope.itemsIndexed(
    component: PagingComponent<K, V>,
    crossinline key: (index: Int, item: V) -> Any? = { _, _ -> null },
    crossinline contentType: (index: Int, item: V) -> Any? = { _, _ -> null },
    crossinline span: LazyGridItemSpanScope.(index: Int, item: V) -> GridItemSpan = { _, _ ->
        GridItemSpan(
            1
        )
    },
    crossinline itemContent: @Composable LazyGridItemScope.(index: Int, item: V) -> Unit
) {
    val pageData = component.pageData
    items(
        count = pageData.itemCount,
        key = { index ->
            key(index, pageData.peek(index)) ?: ParcelableKey(index)
        },
        span = { index ->
            span(index, pageData.peek(index))
        },
        contentType = { index ->
            contentType(index, pageData.peek(index))
        }
    ) { index ->
        itemContent(index, getItem(component, index))
    }
}

inline fun <K, V> LazyStaggeredGridScope.items(
    component: PagingComponent<K, V>,
    crossinline key: (item: V) -> Any? = { null },
    crossinline contentType: (item: V) -> Any? = { null },
    crossinline itemContent: @Composable (LazyStaggeredGridItemScope.(item: V) -> Unit)
) {
    itemsIndexed(
        component = component,
        key = { _, item -> key(item) },
        contentType = { _, item -> contentType(item) }
    ) { _, item ->
        itemContent(item)
    }
}

inline fun <K, V> LazyStaggeredGridScope.itemsIndexed(
    component: PagingComponent<K, V>,
    crossinline key: (index: Int, item: V) -> Any? = { _, _ -> null },
    crossinline contentType: (index: Int, item: V) -> Any? = { _, _ -> null },
    crossinline itemContent: @Composable (LazyStaggeredGridItemScope.(index: Int, item: V) -> Unit)
) {
    val pageData = component.pageData
    items(
        count = pageData.itemCount,
        key = { index ->
            key(index, pageData.peek(index)) ?: ParcelableKey(index)
        },
        contentType = { index ->
            contentType(index, pageData.peek(index))
        }
    ) { index ->
        itemContent(index, getItem(component, index))
    }
}

@PublishedApi
internal fun <K, V> getItem(component: PagingComponent<K, V>, index: Int): V {
    // 判断是否触发加载
    val pageData = component.pageData
    val config = pageData.config
    val itemCount = pageData.itemCount
    val prefetchDistance = config.prefetchDistance
    if (prefetchDistance > 0 && itemCount - index == prefetchDistance) {
        component.pagingLoadMore()
    }
    return pageData.peek(index)
}