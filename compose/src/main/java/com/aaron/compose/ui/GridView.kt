package com.aaron.compose.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import kotlin.math.ceil

/**
 * 网格布局
 *
 * @param data 数据
 * @param maxColumns 最多显示多少列
 */
@Composable
fun <T> GridView(
    data: ImmutableList<T>,
    maxColumns: Int,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    items: @Composable (T) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding),
        verticalArrangement = verticalArrangement
    ) {
        val row = ceil(data.size.toFloat() / maxColumns).toInt()
        val pageSize = maxColumns * row
        val placedList = remember(data) {
            (0..pageSize).toList()
        }
        placedList.chunked(maxColumns).forEach { chunkedList ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = horizontalArrangement
            ) {
                chunkedList.forEach { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        val actualItem = data.getOrNull(placedList.indexOf(item))
                        if (actualItem != null) {
                            items(actualItem)
                        }
                    }
                }
            }
        }
    }
}