package com.aaron.compose.ktx

import androidx.compose.foundation.lazy.LazyListState

/**
 * 最后一个条目的索引
 */
val LazyListState.lastIndex: Int
    get() = layoutInfo.totalItemsCount - 1

/**
 * 判断能否垂直、水平滚动
 *
 * @param direction 小于 0 向上滚动(向左滚动(LTR))，大于 0 向下滚动(向右滚动(LTR))
 */
fun LazyListState.canScroll(direction: Int): Boolean {
    if (direction < 0) {
        val arriveTop = firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0
        return !arriveTop
    } else if (direction > 0) {
        val viewportEndOffset = layoutInfo.viewportEndOffset
        val paddingBottom = layoutInfo.afterContentPadding
        val lastItem = layoutInfo.visibleItemsInfo.lastOrNull() ?: return false
        val arriveBottom = lastItem.index == lastIndex
                && (lastItem.offset + lastItem.size + paddingBottom) == viewportEndOffset
        return !arriveBottom
    }
    return true
}