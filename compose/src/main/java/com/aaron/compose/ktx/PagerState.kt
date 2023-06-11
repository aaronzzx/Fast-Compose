package com.aaron.compose.ktx

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * 计算给定页偏移值
 */
fun PagerState.calculateOffsetFraction(page: Int): Float {
    return (currentPage - page) + currentPageOffsetFraction
}

/**
 * 计算给定页开始方向的偏移值，最少为 0
 */
fun PagerState.calculateStartOffsetFraction(page: Int): Float {
    return calculateOffsetFraction(page).coerceAtLeast(0f)
}

/**
 * 计算给定页结束方向的偏移值，最多为 0
 */
fun PagerState.calculateEndOffsetFraction(page: Int): Float {
    return calculateOffsetFraction(page).coerceAtMost(0f)
}

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
    scrollToPage(oldPage) // reset
    if (abs(page - oldPage) > 3) {
        scrollToPage(page, pageOffset)
    } else {
        animateScrollToPage(page, pageOffset)
    }
}

/**
 * 滑动到上一页，已处理异常
 */
fun PagerState.scrollToPrev(coroutineScope: CoroutineScope) {
    coroutineScope.launch {
        try {
            scrollToPage(currentPage - 1)
        } catch (ignored: Exception) {
        }
    }
}

/**
 * 滑动到下一页，已处理异常
 */
fun PagerState.scrollToNext(coroutineScope: CoroutineScope) {
    coroutineScope.launch {
        try {
            scrollToPage(currentPage + 1)
        } catch (ignored: Exception) {
        }
    }
}

/**
 * 动画滑动到上一页，已处理异常
 */
fun PagerState.animateScrollToPrev(
    coroutineScope: CoroutineScope,
    animationSpec: AnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow)
) {
    coroutineScope.launch {
        try {
            animateScrollToPage(currentPage - 1, animationSpec = animationSpec)
        } catch (ignored: Exception) {
        }
    }
}

/**
 * 动画滑动到下一页，已处理异常
 */
fun PagerState.animateScrollToNext(
    coroutineScope: CoroutineScope,
    animationSpec: AnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow)
) {
    coroutineScope.launch {
        try {
            animateScrollToPage(currentPage + 1, animationSpec = animationSpec)
        } catch (ignored: Exception) {
        }
    }
}