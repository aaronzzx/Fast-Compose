package com.aaron.compose.ktx

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
 * @author aaronzzxup@gmail.com
 * @since 2022/12/5
 */

fun <K, V> LazyListScope.items(
    component: PagingComponent<K, V>,
    key: ((item: V) -> Any?)? = null,
    contentType: ((item: V) -> Any?)? = null,
    itemContent: @Composable LazyItemScope.(item: V) -> Unit
) {
    val pageData = component.pageData
    items(
        count = pageData.itemCount,
        key = if (key == null) null else { index ->
            val item = pageData.peek(index)
            if (item == null) {
                LazyListKey(index)
            } else {
                key(item) ?: LazyListKey(index)
            }
        },
        contentType = { index ->
            contentType?.invoke(pageData.peek(index))
        }
    ) { index ->
        itemContent(getItem(component, index))
    }
}

fun <K, V> LazyListScope.itemsIndexed(
    component: PagingComponent<K, V>,
    key: ((index: Int, item: V) -> Any?)? = null,
    contentType: ((index: Int, item: V) -> Any?)? = null,
    itemContent: @Composable LazyItemScope.(index: Int, item: V) -> Unit
) {
    val pageData = component.pageData
    items(
        count = pageData.itemCount,
        key = if (key == null) null else { index ->
            val item = pageData.peek(index)
            if (item == null) {
                LazyListKey(index)
            } else {
                key(index, item) ?: LazyListKey(index)
            }
        },
        contentType = { index ->
            contentType?.invoke(index, pageData.peek(index))
        }
    ) { index ->
        itemContent(index, getItem(component, index))
    }
}

fun <K, V> LazyGridScope.items(
    component: PagingComponent<K, V>,
    key: ((item: V) -> Any?)? = null,
    contentType: ((item: V) -> Any?)? = null,
    span: (LazyGridItemSpanScope.(item: V) -> GridItemSpan)? = null,
    itemContent: @Composable LazyGridItemScope.(item: V) -> Unit
) {
    val pageData = component.pageData
    items(
        count = pageData.itemCount,
        key = if (key == null) null else { index ->
            val item = pageData.peek(index)
            if (item == null) {
                LazyListKey(index)
            } else {
                key(item) ?: LazyListKey(index)
            }
        },
        span = if (span == null) null else { index ->
            span(pageData.peek(index))
        },
        contentType = { index ->
            contentType?.invoke(pageData.peek(index))
        }
    ) { index ->
        itemContent(getItem(component, index))
    }
}

fun <K, V> LazyGridScope.itemsIndexed(
    component: PagingComponent<K, V>,
    key: ((index: Int, item: V) -> Any?)? = null,
    contentType: ((index: Int, item: V) -> Any?)? = null,
    span: (LazyGridItemSpanScope.(index: Int, item: V) -> GridItemSpan)? = null,
    itemContent: @Composable LazyGridItemScope.(index: Int, item: V) -> Unit
) {
    val pageData = component.pageData
    items(
        count = pageData.itemCount,
        key = if (key == null) null else { index ->
            val item = pageData.peek(index)
            if (item == null) {
                LazyListKey(index)
            } else {
                key(index, item) ?: LazyListKey(index)
            }
        },
        span = if (span == null) null else { index ->
            span(index, pageData.peek(index))
        },
        contentType = { index ->
            contentType?.invoke(index, pageData.peek(index))
        }
    ) { index ->
        itemContent(index, getItem(component, index))
    }
}

fun <K, V> LazyStaggeredGridScope.items(
    component: PagingComponent<K, V>,
    key: ((item: V) -> Any?)? = null,
    contentType: ((item: V) -> Any?)? = null,
    itemContent: @Composable (LazyStaggeredGridItemScope.(item: V) -> Unit)
) {
    val pageData = component.pageData
    items(
        count = pageData.itemCount,
        key = if (key == null) null else { index ->
            val item = pageData.peek(index)
            if (item == null) {
                LazyListKey(index)
            } else {
                key(item) ?: LazyListKey(index)
            }
        },
        contentType = { index ->
            contentType?.invoke(pageData.peek(index))
        }
    ) { index ->
        itemContent(getItem(component, index))
    }
}

fun <K, V> LazyStaggeredGridScope.itemsIndexed(
    component: PagingComponent<K, V>,
    key: ((index: Int, item: V) -> Any?)? = null,
    contentType: ((index: Int, item: V) -> Any?)? = null,
    itemContent: @Composable (LazyStaggeredGridItemScope.(index: Int, item: V) -> Unit)
) {
    val pageData = component.pageData
    items(
        count = pageData.itemCount,
        key = if (key == null) null else { index ->
            val item = pageData.peek(index)
            if (item == null) {
                LazyListKey(index)
            } else {
                key(index, item) ?: LazyListKey(index)
            }
        },
        contentType = { index ->
            contentType?.invoke(index, pageData.peek(index))
        }
    ) { index ->
        itemContent(index, getItem(component, index))
    }
}

private fun <K, V> getItem(component: PagingComponent<K, V>, index: Int): V {
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