package com.aaron.compose.ktx

import androidx.compose.foundation.lazy.grid.LazyGridState

/**
 * 最后一个条目的索引
 */
val LazyGridState.lastIndex: Int
    get() = layoutInfo.totalItemsCount - 1