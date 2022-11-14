package com.aaron.compose.ktx

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridState

/**
 * 最后一个条目的索引
 */
val LazyGridState.lastIndex: Int
    get() = layoutInfo.totalItemsCount - 1

/**
 * 判断能否垂直、水平滚动
 *
 * @param direction 小于 0 向上滚动(向左滚动(LTR))，大于 0 向下滚动(向右滚动(LTR))
 */
fun LazyGridState.canScroll(direction: Int): Boolean {
    if (layoutInfo.orientation == Orientation.Vertical) {
        return canScroll(
            direction = direction,
            onGetOffset = { it.offset.y },
            onGetSize = { it.size.height },
            onGetLane = { it.column }
        )
    }
    return canScroll(
        direction = direction,
        onGetOffset = { it.offset.x },
        onGetSize = { it.size.width },
        onGetLane = { it.row }
    )
}

private inline fun LazyGridState.canScroll(
    direction: Int,
    onGetOffset: (LazyGridItemInfo) -> Int,
    onGetSize: (LazyGridItemInfo) -> Int,
    onGetLane: (LazyGridItemInfo) -> Int
): Boolean {
    if (direction < 0) {
        val arriveStart = firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0
        return !arriveStart
    } else if (direction > 0) {
        // 使用和瀑布流相同的判断方式，因为网格布局的 item 也可以不同大小
        val visibleItemsInfo = layoutInfo.visibleItemsInfo
        val lastItem = visibleItemsInfo.find { it.index == lastIndex } ?: return true
        var actualLastItem = lastItem
        if (onGetLane(actualLastItem) > 0) {
            // 从倒数第二个开始向前遍历
            for (i in (visibleItemsInfo.lastIndex - 1) downTo 0) {
                val item = visibleItemsInfo[i]
                // needBreak 这里不用和瀑布流判断相同，因为网格布局顺序是恒定的，不会像瀑布流错乱
                val needBreak = onGetLane(item) == 0

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
        }
        val actualEnd = onGetOffset(actualLastItem) + onGetSize(actualLastItem)
        val arriveEnd = actualEnd + layoutInfo.afterContentPadding <= layoutInfo.viewportEndOffset
        return !arriveEnd
    }
    return true
}