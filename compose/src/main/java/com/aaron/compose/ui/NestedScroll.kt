package com.aaron.compose.ui

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp

/**
 * 嵌套滑动布局
 */
@Composable
fun NestedScroll(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    header: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled
    ) {
        item(
            key = HeaderKey,
            contentType = { HeaderKey }
        ) {
            Box(modifier = Modifier.fillParentMaxWidth()) {
                header()
            }
        }
        item(
            key = ContentKey,
            contentType = { ContentKey }
        ) {
            Box(
                modifier = Modifier
                    .nestedScroll(
                        connection = object : NestedScrollConnection {
                            override fun onPreScroll(
                                available: Offset,
                                source: NestedScrollSource
                            ): Offset {
                                if (available.y < 0f && !state.isHeaderHiding()) {
                                    state.dispatchRawDelta(-available.y)
                                    return available
                                }
                                return super.onPreScroll(available, source)
                            }
                        },
                        dispatcher = null
                    )
                    .fillParentMaxSize()
            ) {
                content()
            }
        }
    }
}

/**
 * 头部是否被完全隐藏
 */
private fun LazyListState.isHeaderHiding(): Boolean {
    return firstVisibleItemIndex == 1 && firstVisibleItemScrollOffset == 0
}

private const val HeaderKey = "NestedScroll-Header"
private const val ContentKey = "NestedScroll-Content"