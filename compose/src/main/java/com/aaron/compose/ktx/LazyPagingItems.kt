package com.aaron.compose.ktx

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable
import androidx.paging.PagingData
import androidx.paging.PagingDataDiffer
import androidx.paging.compose.LazyPagingItems
import kotlinx.coroutines.flow.Flow

/**
 * Adds the [LazyPagingItems] and their content to the scope. The range from 0 (inclusive) to
 * [LazyPagingItems.itemCount] (exclusive) always represents the full range of presentable items,
 * because every event from [PagingDataDiffer] will trigger a recomposition.
 *
 * @sample androidx.paging.compose.samples.ItemsDemo
 *
 * @param items the items received from a [Flow] of [PagingData].
 * @param key a factory of stable and unique keys representing the item. Using the same key
 * for multiple items in the grid is not allowed. Type of the key should be saveable
 * via Bundle on Android. If null is passed the position in the grid will represent the key.
 * When you specify the key the scroll position will be maintained based on the key, which
 * means if you add/remove items before the current visible item the item with the given key
 * will be kept as the first visible one.
 * @param span define custom spans for the items. Default is 1x1. It is good practice to leave
 * it `null` when this matches the intended behavior, as providing a custom implementation
 * impacts performance
 * @param contentType a factory of the content types for the item. The item compositions of
 * the same type could be reused more efficiently. Note that null is a valid type and items of such
 * type will be considered compatible.
 * @param itemContent the content displayed by a single item
 */
fun <T : Any> LazyGridScope.items(
    items: LazyPagingItems<T>,
    key: ((item: T) -> Any)? = null,
    span: (LazyGridItemSpanScope.(item: T?) -> GridItemSpan)? = null,
    contentType: (item: T?) -> Any? = { null },
    itemContent: @Composable LazyGridItemScope.(value: T?) -> Unit
) {
    items(
        count = items.itemCount,
        key = if (key == null) null else { index ->
            val item = items.peek(index)
            if (item == null) {
                PagingPlaceholderKey(index)
            } else {
                key(item)
            }
        },
        span = if (span == null) null else { index ->
            span(items.peek(index))
        },
        contentType = { index ->
            contentType(items.peek(index))
        }
    ) { index ->
        itemContent(items[index])
    }
}

/**
 * Adds the [LazyPagingItems] and their content to the scope where the content of an item is
 * aware of its local index. The range from 0 (inclusive) to [LazyPagingItems.itemCount] (exclusive)
 * always represents the full range of presentable items, because every event from
 * [PagingDataDiffer] will trigger a recomposition.
 *
 * @sample androidx.paging.compose.samples.ItemsIndexedDemo
 *
 * @param items the items received from a [Flow] of [PagingData].
 * @param key a factory of stable and unique keys representing the item. Using the same key
 * for multiple items in the grid is not allowed. Type of the key should be saveable
 * via Bundle on Android. If null is passed the position in the grid will represent the key.
 * When you specify the key the scroll position will be maintained based on the key, which
 * means if you add/remove items before the current visible item the item with the given key
 * will be kept as the first visible one.
 * @param span define custom spans for the items. Default is 1x1. It is good practice to leave
 * it `null` when this matches the intended behavior, as providing a custom implementation
 * impacts performance
 * @param contentType a factory of the content types for the item. The item compositions of
 * the same type could be reused more efficiently. Note that null is a valid type and items of such
 * type will be considered compatible.
 * @param itemContent the content displayed by a single item
 */
fun <T : Any> LazyGridScope.itemsIndexed(
    items: LazyPagingItems<T>,
    key: ((index: Int, item: T) -> Any)? = null,
    span: (LazyGridItemSpanScope.(index: Int, item: T?) -> GridItemSpan)? = null,
    contentType: (index: Int, item: T?) -> Any? = { _, _ -> null },
    itemContent: @Composable LazyGridItemScope.(index: Int, value: T?) -> Unit
) {
    items(
        count = items.itemCount,
        key = if (key == null) null else { index ->
            val item = items.peek(index)
            if (item == null) {
                PagingPlaceholderKey(index)
            } else {
                key(index, item)
            }
        },
        span = if (span == null) null else { index ->
            span(index, items.peek(index))
        },
        contentType = { index ->
            contentType(index, items.peek(index))
        }
    ) { index ->
        itemContent(index, items[index])
    }
}

@SuppressLint("BanParcelableUsage")
private data class PagingPlaceholderKey(private val index: Int) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(index)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<PagingPlaceholderKey> =
            object : Parcelable.Creator<PagingPlaceholderKey> {
                override fun createFromParcel(parcel: Parcel) =
                    PagingPlaceholderKey(parcel.readInt())

                override fun newArray(size: Int) = arrayOfNulls<PagingPlaceholderKey?>(size)
            }
    }
}