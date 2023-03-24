package com.aaron.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.aaron.compose.ktx.toDp
import com.google.accompanist.pager.HorizontalPagerIndicator
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlin.math.ceil

/**
 * 使用 Columns 、Row 来完成网格多页布局
 */
@Composable
fun <T> GridPage(
    data: ImmutableList<T>,
    maxColumns: Int,
    maxRows: Int,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    shape: Shape = RoundedCornerShape(4.dp),
    lockHeight: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    indicator: (@Composable ColumnScope.(PagerState, pageCount: Int) -> Unit)? = { pagerState, pageCount ->
        GridPageIndicator(
            pagerState = pagerState,
            pageCount = pageCount
        )
        Spacer(modifier = Modifier.height(12.dp))
    },
    items: @Composable (T) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = backgroundColor,
                shape = shape
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val totalCount = data.size
        if (totalCount <= 0) {
            return@Column
        }
        val pagerState = rememberPagerState()
        val pageSize = maxColumns * maxRows
        val pageCount = ceil(totalCount / pageSize.toFloat()).toInt()
        var everyPageHeight by rememberSaveable {
            mutableStateOf(0)
        }
        HorizontalPager(
            state = pagerState,
            pageCount = pageCount,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val fromIndex = page * pageSize
            val pageList = data
                .subList(fromIndex, data.size)
                .take(pageSize)
                .toPersistentList()

            GridView(
                modifier = Modifier
                    // 锁定高度的处理
                    .let {
                        if (lockHeight && page == 0) it.onGloballyPositioned { layoutCoordinates ->
                            everyPageHeight = layoutCoordinates.size.height
                        } else it
                    }
                    .let {
                        if (lockHeight && page != 0) it.height(everyPageHeight.toDp()) else it
                    },
                data = pageList,
                maxColumns = maxColumns,
                contentPadding = contentPadding,
                verticalArrangement = verticalArrangement,
                horizontalArrangement = horizontalArrangement
            ) {
                items(it)
            }
        }
        if (indicator != null && pageCount > 1) {
            indicator(pagerState, pageCount)
        }
    }
}

/**
 * 通用的 GridPage 指示器
 */
@Composable
fun GridPageIndicator(pagerState: PagerState, pageCount: Int) {
    HorizontalPagerIndicator(
        pagerState = pagerState,
        pageCount = pageCount,
        activeColor = MaterialTheme.colors.primary,
        inactiveColor = Color.LightGray,
        indicatorWidth = 8.dp,
        indicatorHeight = 4.dp,
        spacing = 8.dp,
        indicatorShape = RoundedCornerShape(4.dp)
    )
}

/**
 * 网格布局
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