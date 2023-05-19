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
import com.aaron.compose.paging.PageData

/**
 * 基于 [PageData] 扩展，用于
 * [androidx.compose.foundation.lazy.LazyColumn] 、
 *
 * [androidx.compose.foundation.lazy.grid.LazyGrid] 、
 *
 * [androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGrid]
 *
 * @author aaronzzxup@gmail.com
 * @since 2022/9/21
 */

inline fun <K, V> LazyListScope.items(
    pageData: PageData<K, V>,
    crossinline key: (item: V) -> Any? = { null },
    crossinline contentType: (item: V) -> Any? = { null },
    crossinline itemContent: @Composable LazyItemScope.(item: V) -> Unit
) {
    itemsIndexed(
        pageData = pageData,
        key = { _, item -> key(item) },
        contentType = { _, item -> contentType(item) }
    ) { _, item ->
        itemContent(item)
    }
}

inline fun <K, V> LazyListScope.itemsIndexed(
    pageData: PageData<K, V>,
    crossinline key: (index: Int, item: V) -> Any? = { _, _ -> null },
    crossinline contentType: (index: Int, item: V) -> Any? = { _, _ -> null },
    crossinline itemContent: @Composable LazyItemScope.(index: Int, item: V) -> Unit
) {
    items(
        count = pageData.itemCount,
        key = { index ->
            key(index, pageData.peek(index)) ?: ParcelableKey(index)
        },
        contentType = { index ->
            contentType(index, pageData.peek(index))
        }
    ) { index ->
        itemContent(index, pageData[index])
    }
}

inline fun <K, V> LazyGridScope.items(
    pageData: PageData<K, V>,
    crossinline key: (item: V) -> Any? = { null },
    crossinline contentType: (item: V) -> Any? = { null },
    crossinline span: LazyGridItemSpanScope.(item: V) -> GridItemSpan = { GridItemSpan(1) },
    crossinline itemContent: @Composable LazyGridItemScope.(item: V) -> Unit
) {
    itemsIndexed(
        pageData = pageData,
        key = { _, item -> key(item) },
        span = { _, item -> span(item) },
        contentType = { _, item -> contentType(item) }
    ) { _, item ->
        itemContent(item)
    }
}

inline fun <K, V> LazyGridScope.itemsIndexed(
    pageData: PageData<K, V>,
    crossinline key: (index: Int, item: V) -> Any? = { _, _ -> null },
    crossinline contentType: (index: Int, item: V) -> Any? = { _, _ -> null },
    crossinline span: LazyGridItemSpanScope.(index: Int, item: V) -> GridItemSpan = { _, _ ->
        GridItemSpan(
            1
        )
    },
    crossinline itemContent: @Composable LazyGridItemScope.(index: Int, item: V) -> Unit
) {
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
        itemContent(index, pageData[index])
    }
}

inline fun <K, V> LazyStaggeredGridScope.items(
    pageData: PageData<K, V>,
    crossinline key: (item: V) -> Any? = { null },
    crossinline contentType: (item: V) -> Any? = { null },
    crossinline itemContent: @Composable (LazyStaggeredGridItemScope.(item: V) -> Unit)
) {
    itemsIndexed(
        pageData = pageData,
        key = { _, item -> key(item) },
        contentType = { _, item -> contentType(item) }
    ) { _, item ->
        itemContent(item)
    }
}

inline fun <K, V> LazyStaggeredGridScope.itemsIndexed(
    pageData: PageData<K, V>,
    crossinline key: (index: Int, item: V) -> Any? = { _, _ -> null },
    crossinline contentType: (index: Int, item: V) -> Any? = { _, _ -> null },
    crossinline itemContent: @Composable (LazyStaggeredGridItemScope.(index: Int, item: V) -> Unit)
) {
    items(
        count = pageData.itemCount,
        key = { index ->
            key(index, pageData.peek(index)) ?: ParcelableKey(index)
        },
        contentType = { index ->
            contentType(index, pageData.peek(index))
        }
    ) { index ->
        itemContent(index, pageData[index])
    }
}