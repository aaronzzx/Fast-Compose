package com.aaron.compose.ktx

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlin.math.abs

/**
 * 延迟 [PagerState.currentPage] 的读取
 */
@Composable
fun PagerState.currentPageDelayed(): State<Int> = produceState(
    // 必须用 remember 包住，否则会因为感应到状态变化而频繁重组
    initialValue = remember(this) { currentPage }
) {
    snapshotFlow { currentPage to currentPageOffsetFraction }
        .filter { abs(it.second) < 0.025f }
        .map { it.first }
        .distinctUntilChanged()
        .collect { value = it }
}

/**
 * 自动选择是动画跳页还是瞬间跳页
 */
suspend fun PagerState.smartScrollToPage(
    @IntRange(from = 0) page: Int,
    @FloatRange(from = -1.0, to = 1.0) pageOffset: Float = 0f
) {
    // pre-jump to nearby item for long jumps as an optimization
    // the same trick is done in ViewPager2
    val oldPage = currentPage
    if (abs(page - oldPage) > 3) {
        scrollToPage(page, pageOffset)
    } else {
        animateScrollToPage(page, pageOffset)
    }
}