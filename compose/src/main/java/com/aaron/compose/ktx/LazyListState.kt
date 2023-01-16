package com.aaron.compose.ktx

import androidx.compose.foundation.lazy.LazyListState

/**
 * 最后一个条目的索引
 */
val LazyListState.lastIndex: Int
    get() = layoutInfo.totalItemsCount - 1