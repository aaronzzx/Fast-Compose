package com.aaron.compose.ktx

import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState

/**
 * 最后一个条目的索引
 */
val LazyStaggeredGridState.lastIndex: Int
    get() = layoutInfo.totalItemsCount - 1