package com.aaron.compose.ktx

import androidx.compose.foundation.lazy.LazyListState

/**
 * 最后一个条目的索引
 */
val LazyListState.lastIndex: Int
    get() = layoutInfo.totalItemsCount - 1

/**
 * 判断能否垂直滚动
 *
 * @param direction 小于 0 向上滚动，大于 0 向下滚动
 */
fun LazyListState.canScrollVertical(direction: Int): Boolean {
    if (direction < 0) {
        val arriveTop = firstVisibleItemScrollOffset == 0
        return !arriveTop
    } else if (direction > 0) {
        val viewportEndOffset = layoutInfo.viewportEndOffset
        val paddingBottom = layoutInfo.afterContentPadding
        val lastItem = layoutInfo.visibleItemsInfo.last()
        val arriveBottom = lastItem.index == lastIndex
                && (lastItem.offset + lastItem.size + paddingBottom) == viewportEndOffset
        return !arriveBottom
    }
    return true
}

/**
 * 判断能否水平滚动
 *
 * @param direction 小于 0 向左滚动，大于 0 向右滚动（LTR）
 */
fun LazyListState.canScrollHorizontal(direction: Int): Boolean {
    if (direction < 0) {
        val arriveStart = firstVisibleItemScrollOffset == 0
        return !arriveStart
    } else if (direction > 0) {
        val viewportEndOffset = layoutInfo.viewportEndOffset
        val paddingEnd = layoutInfo.afterContentPadding
        val lastItem = layoutInfo.visibleItemsInfo.last()
        val arriveEnd = lastItem.index == lastIndex
                && (lastItem.offset + lastItem.size + paddingEnd) == viewportEndOffset
        return !arriveEnd
    }
    return true
}