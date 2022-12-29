package com.aaron.compose.ktx

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridItemInfo
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState

/**
 * 最后一个条目的索引
 */
val LazyStaggeredGridState.lastIndex: Int
    get() = layoutInfo.totalItemsCount - 1

/**
 * 判断能否垂直、水平滚动（暂时不支持 LazyVerticalStaggeredGrid ，因为内部 item 宽高有问题）
 *
 * @param direction 小于 0 向上滚动(向左滚动(LTR))，大于 0 向下滚动(向右滚动(LTR))
 */
fun LazyStaggeredGridState.canScroll(direction: Int): Boolean {
    if (layoutInfo.orientation == Orientation.Vertical) {
        return canScroll(
            direction = direction,
            onGetOffset = { it.offset.y },
            onGetSize = { it.size.height }
        )
    }
    return canScroll(
        direction = direction,
        onGetOffset = { it.offset.x },
        onGetSize = { it.size.width }
    )
}

private inline fun LazyStaggeredGridState.canScroll(
    direction: Int,
    onGetOffset: (LazyStaggeredGridItemInfo) -> Int,
    onGetSize: (LazyStaggeredGridItemInfo) -> Int
): Boolean {
    if (direction < 0) {
        val arriveStart = firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0
        return !arriveStart
    } else if (direction > 0) {
        val visibleItemsInfo = layoutInfo.visibleItemsInfo
        val lastItem = visibleItemsInfo.find { it.index == lastIndex } ?: return true
        var actualLastItem = lastItem
        // 从倒数第二个开始向前遍历
        for (i in (visibleItemsInfo.lastIndex - 1) downTo 0) {
            val item = visibleItemsInfo[i]
            val needBreak = item.lane == actualLastItem.lane

            val actualEnd = onGetOffset(actualLastItem) + onGetSize(actualLastItem)
            val end = onGetOffset(item) + onGetSize(item)
            if (end > actualEnd) {
                // 视觉上最接近底部的 item
                actualLastItem = item
            }
            if (needBreak) {
                break
            }
        }
        val actualEnd = onGetOffset(actualLastItem) + onGetSize(actualLastItem)
        val arriveEnd = actualEnd + layoutInfo.afterContentPadding <= layoutInfo.viewportEndOffset
        return !arriveEnd
    }
    return true
}