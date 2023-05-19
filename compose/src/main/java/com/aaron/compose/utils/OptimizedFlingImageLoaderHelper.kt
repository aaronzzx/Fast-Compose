package com.aaron.compose.utils

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import coil.Coil
import coil.ImageLoader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first

/**
 * 针对列表滚动加载图片进行优化，fling 过程中或协程取消时不进行图片加载。
 */
@Composable
fun rememberOptimizedFlingImageLoader(scrollState: Any): ImageLoader {
    requireScrollState(scrollState)

    val isListDragging by scrollState.interactionSource.collectIsDraggedAsState()
    val globalImageLoader = Coil.imageLoader(LocalContext.current)
    return remember(scrollState, globalImageLoader) {
        globalImageLoader
            .newBuilder()
            .components {
                add { chain ->
                    try {
                        if (scrollState.isScrollInProgress && !isListDragging) {
                            snapshotFlow { scrollState.isScrollInProgress }
                                .combine(
                                    snapshotFlow { isListDragging }
                                ) { isScrollInProgress, isListDragging ->
                                    isScrollInProgress to isListDragging
                                }
                                .filterNot { (isScrollInProgress, isListDragging) ->
                                    isScrollInProgress && !isListDragging
                                }
                                .first()
                        }
                        chain.proceed(chain.request)
                    } catch (ex: CancellationException) {
                        throw ex
                    }
                }
            }
            .build()
    }
}

private fun requireScrollState(scrollState: Any) {
    require(
        scrollState is ScrollState
                || scrollState is LazyListState
                || scrollState is LazyGridState
                || scrollState is LazyStaggeredGridState
    )
}

private val Any.isScrollInProgress: Boolean
    get() = when (this) {
        is ScrollState -> this.isScrollInProgress
        is LazyListState -> this.isScrollInProgress
        is LazyGridState -> this.isScrollInProgress
        is LazyStaggeredGridState -> this.isScrollInProgress
        else -> error("Unknown listState: $this")
    }

private val Any.interactionSource: InteractionSource
    get() = when (this) {
        is ScrollState -> this.interactionSource
        is LazyListState -> this.interactionSource
        is LazyGridState -> this.interactionSource
        is LazyStaggeredGridState -> this.interactionSource
        else -> error("Unknown listState: $this")
    }