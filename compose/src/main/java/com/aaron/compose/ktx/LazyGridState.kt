package com.aaron.compose.ktx

import androidx.compose.foundation.lazy.grid.LazyGridState

/**
 * 最后一个条目的索引
 */
val LazyGridState.lastIndex: Int
    get() = layoutInfo.totalItemsCount - 1

/**
 * 判断能否垂直滚动
 *
 * @param direction 小于 0 向上滚动，大于 0 向下滚动
 */
fun LazyGridState.canScrollVertical(direction: Int): Boolean {
    if (direction < 0) {
        val arriveTop = firstVisibleItemScrollOffset == 0
        return !arriveTop
    } else if (direction > 0) {
        val viewportEndOffset = layoutInfo.viewportEndOffset
        val paddingBottom = layoutInfo.afterContentPadding
        val lastItem = layoutInfo.visibleItemsInfo.lastOrNull() ?: return false
        val lastItemOffset = lastItem.offset.y + lastItem.size.height + paddingBottom
        val arriveBottom = lastItem.index == lastIndex
                && lastItemOffset == viewportEndOffset
        return !arriveBottom
    }
    return true
}

/**
 * 判断能否水平滚动
 *
 * @param direction 小于 0 向左滚动，大于 0 向右滚动（LTR）
 */
fun LazyGridState.canScrollHorizontal(direction: Int): Boolean {
    if (direction < 0) {
        val arriveStart = firstVisibleItemScrollOffset == 0
        return !arriveStart
    } else if (direction > 0) {
        val viewportEndOffset = layoutInfo.viewportEndOffset
        val paddingEnd = layoutInfo.afterContentPadding
        val lastItem = layoutInfo.visibleItemsInfo.lastOrNull() ?: return false
        val lastItemOffset = lastItem.offset.x + lastItem.size.width + paddingEnd
        val arriveEnd = lastItem.index == lastIndex
                && lastItemOffset == viewportEndOffset
        return !arriveEnd
    }
    return true
}