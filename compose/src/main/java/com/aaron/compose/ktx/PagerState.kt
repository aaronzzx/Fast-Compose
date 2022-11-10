package com.aaron.compose.ktx

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import com.google.accompanist.pager.PagerState
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
    snapshotFlow { currentPage to currentPageOffset }
        .filter { abs(it.second) < 0.025f }
        .map { it.first }
        .distinctUntilChanged()
        .collect { value = it }
}