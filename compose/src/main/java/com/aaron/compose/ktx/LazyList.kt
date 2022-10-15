package com.aaron.compose.ktx

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable
import com.aaron.compose.paging.PageData

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/9/21
 */

fun <K, V> LazyListScope.items(
    pageData: PageData<K, V>,
    key: ((item: V) -> Any)? = null,
    contentType: ((item: V) -> Any?)? = null,
    itemContent: @Composable LazyItemScope.(item: V) -> Unit
) {
    items(
        count = pageData.count,
        key = if (key == null) null else { index ->
            val item = pageData.peek(index)
            if (item == null) {
                PagingDataKey(index)
            } else {
                key(item)
            }
        },
        contentType = { index ->
            contentType?.invoke(pageData.peek(index))
        }
    ) { index ->
        itemContent(pageData[index])
    }
}

fun <K, V> LazyListScope.itemsIndexed(
    pageData: PageData<K, V>,
    key: ((index: Int, item: V) -> Any)? = null,
    contentType: ((index: Int, item: V) -> Any?)? = null,
    itemContent: @Composable LazyItemScope.(index: Int, item: V) -> Unit
) {
    items(
        count = pageData.count,
        key = if (key == null) null else { index ->
            val item = pageData.peek(index)
            if (item == null) {
                PagingDataKey(index)
            } else {
                key(index, item)
            }
        },
        contentType = { index ->
            contentType?.invoke(index, pageData.peek(index))
        }
    ) { index ->
        itemContent(index, pageData[index])
    }
}

fun <K, V> LazyGridScope.items(
    pageData: PageData<K, V>,
    key: ((item: V) -> Any)? = null,
    contentType: ((item: V) -> Any?)? = null,
    span: (LazyGridItemSpanScope.(item: V) -> GridItemSpan)? = null,
    itemContent: @Composable LazyGridItemScope.(item: V) -> Unit
) {
    items(
        count = pageData.count,
        key = if (key == null) null else { index ->
            val item = pageData.peek(index)
            if (item == null) {
                PagingDataKey(index)
            } else {
                key(item)
            }
        },
        span = if (span == null) null else { index ->
            span(pageData.peek(index))
        },
        contentType = { index ->
            contentType?.invoke(pageData.peek(index))
        }
    ) { index ->
        itemContent(pageData[index])
    }
}

fun <K, V> LazyGridScope.itemsIndexed(
    pageData: PageData<K, V>,
    key: ((index: Int, item: V) -> Any)? = null,
    contentType: ((index: Int, item: V) -> Any?)? = null,
    span: (LazyGridItemSpanScope.(index: Int, item: V) -> GridItemSpan)? = null,
    itemContent: @Composable LazyGridItemScope.(index: Int, item: V) -> Unit
) {
    items(
        count = pageData.count,
        key = if (key == null) null else { index ->
            val item = pageData.peek(index)
            if (item == null) {
                PagingDataKey(index)
            } else {
                key(index, item)
            }
        },
        span = if (span == null) null else { index ->
            span(index, pageData.peek(index))
        },
        contentType = { index ->
            contentType?.invoke(index, pageData.peek(index))
        }
    ) { index ->
        itemContent(index, pageData[index])
    }
}

private data class PagingDataKey(private val index: Int) : Parcelable {

    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<PagingDataKey> =
            object : Parcelable.Creator<PagingDataKey> {
                override fun createFromParcel(parcel: Parcel) =
                    PagingDataKey(parcel.readInt())

                override fun newArray(size: Int) = arrayOfNulls<PagingDataKey?>(size)
            }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(index)
    }

    override fun describeContents(): Int {
        return 0
    }
}