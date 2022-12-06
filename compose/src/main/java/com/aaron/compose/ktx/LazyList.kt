package com.aaron.compose.ktx

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridItemScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aaron.compose.paging.PageData

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/9/21
 */

object LazyListSectionDefaults {

    var spacing: Dp = 0.dp

    var backgroundColor: Color = Color.Transparent

    var shape: CornerBasedShape = RoundedCornerShape(0.dp)
}

inline fun <T> LazyListScope.sections(
    sections: List<List<T>>,
    sectionSpacing: Dp = LazyListSectionDefaults.spacing,
    sectionBackgroundColor: Color = LazyListSectionDefaults.backgroundColor,
    sectionShape: CornerBasedShape = LazyListSectionDefaults.shape,
    crossinline itemKey: (item: T) -> Any? = { null },
    crossinline itemContentType: (item: T) -> Any? = { null },
    crossinline itemContent: @Composable LazyItemScope.(item: T) -> Unit
) {
    sectionsIndexed(
        sections = sections,
        sectionSpacing = sectionSpacing,
        sectionBackgroundColor = sectionBackgroundColor,
        sectionShape = sectionShape,
        itemKey = { _, item -> itemKey(item) },
        itemContentType = { _, item -> itemContentType(item) }
    ) { _, _, item ->
        itemContent(item)
    }
}

inline fun <T> LazyListScope.sectionsIndexed(
    sections: List<List<T>>,
    sectionSpacing: Dp = LazyListSectionDefaults.spacing,
    sectionBackgroundColor: Color = LazyListSectionDefaults.backgroundColor,
    sectionShape: CornerBasedShape = LazyListSectionDefaults.shape,
    crossinline itemKey: (index: Int, item: T) -> Any? = { _, _ -> null },
    crossinline itemContentType: (index: Int, item: T) -> Any? = { _, _ -> null },
    crossinline itemContent: @Composable LazyItemScope.(sectionIndex: Int, itemIndex: Int, item: T) -> Unit
) {
    sections.forEachIndexed { sectionIndex, section ->
        itemsIndexed(
            items = section,
            key = { index, item ->
                itemKey(index, item) ?: LazyListKey(index)
            },
            contentType = itemContentType
        ) { index, item ->
            Box(
                modifier = Modifier
                    .let {
                        if (sectionIndex == 0 || index != 0) it else {
                            it.padding(top = sectionSpacing)
                        }
                    }
                    .fillParentMaxWidth()
                    .clipToBackground(
                        color = sectionBackgroundColor,
                        shape = when (index) {
                            0 -> sectionShape.copy(
                                bottomStart = ZeroCornerSize,
                                bottomEnd = ZeroCornerSize
                            )
                            section.lastIndex -> sectionShape.copy(
                                topStart = ZeroCornerSize,
                                topEnd = ZeroCornerSize
                            )
                            else -> RectangleShape
                        }
                    )
            ) {
                itemContent(sectionIndex, index, item)
            }
        }
    }
}

fun <K, V> LazyListScope.items(
    pageData: PageData<K, V>,
    key: ((item: V) -> Any?)? = null,
    contentType: ((item: V) -> Any?)? = null,
    itemContent: @Composable LazyItemScope.(item: V) -> Unit
) {
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
        itemContent(pageData[index])
    }
}

fun <K, V> LazyListScope.itemsIndexed(
    pageData: PageData<K, V>,
    key: ((index: Int, item: V) -> Any?)? = null,
    contentType: ((index: Int, item: V) -> Any?)? = null,
    itemContent: @Composable LazyItemScope.(index: Int, item: V) -> Unit
) {
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
        itemContent(index, pageData[index])
    }
}

fun <K, V> LazyGridScope.items(
    pageData: PageData<K, V>,
    key: ((item: V) -> Any?)? = null,
    contentType: ((item: V) -> Any?)? = null,
    span: (LazyGridItemSpanScope.(item: V) -> GridItemSpan)? = null,
    itemContent: @Composable LazyGridItemScope.(item: V) -> Unit
) {
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
        itemContent(pageData[index])
    }
}

fun <K, V> LazyGridScope.itemsIndexed(
    pageData: PageData<K, V>,
    key: ((index: Int, item: V) -> Any?)? = null,
    contentType: ((index: Int, item: V) -> Any?)? = null,
    span: (LazyGridItemSpanScope.(index: Int, item: V) -> GridItemSpan)? = null,
    itemContent: @Composable LazyGridItemScope.(index: Int, item: V) -> Unit
) {
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
        itemContent(index, pageData[index])
    }
}

fun <K, V> LazyStaggeredGridScope.items(
    pageData: PageData<K, V>,
    key: ((item: V) -> Any?)? = null,
    contentType: ((item: V) -> Any?)? = null,
    itemContent: @Composable (LazyStaggeredGridItemScope.(item: V) -> Unit)
) {
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
        itemContent(pageData[index])
    }
}

fun <K, V> LazyStaggeredGridScope.itemsIndexed(
    pageData: PageData<K, V>,
    key: ((index: Int, item: V) -> Any?)? = null,
    contentType: ((index: Int, item: V) -> Any?)? = null,
    itemContent: @Composable (LazyStaggeredGridItemScope.(index: Int, item: V) -> Unit)
) {
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
        itemContent(index, pageData[index])
    }
}

data class LazyListKey(private val index: Int) : Parcelable {

    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<LazyListKey> =
            object : Parcelable.Creator<LazyListKey> {
                override fun createFromParcel(parcel: Parcel) =
                    LazyListKey(parcel.readInt())

                override fun newArray(size: Int) = arrayOfNulls<LazyListKey?>(size)
            }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(index)
    }

    override fun describeContents(): Int {
        return 0
    }
}