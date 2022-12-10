package com.aaron.compose.ktx.lazylist

import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/12/7
 */

object LazySectionDefaults {

    var spacing: Dp = 0.dp

    var backgroundColor: Color = Color.Transparent

    var shape: CornerBasedShape = RoundedCornerShape(0.dp)
}

@PublishedApi
internal val DefaultGridItemSpan = GridItemSpan(1)