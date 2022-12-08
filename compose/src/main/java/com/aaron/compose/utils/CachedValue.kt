package com.aaron.compose.utils

import androidx.collection.ArrayMap
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/12/8
 */

@Composable
fun rememberLazyListCachedValue(): LazyListCachedValue {
    return rememberSaveable(saver = LazyListCachedValue.Saver) {
        LazyListCachedValue(ArrayMap())
    }
}

class LazyListCachedValue(private val map: MutableMap<String, Pair<Int, Int>>) {

    companion object {
        private const val DefaultIndex = 0
        private const val DefaultOffset = 0

        val Saver: Saver<LazyListCachedValue, *> = mapSaver(
            save = {
                it.map
            },
            restore = {
                val map = (it as? Map<String, Pair<Int, Int>>)?.toMutableMap() ?: ArrayMap()
                LazyListCachedValue(map)
            }
        )
    }

    @Composable
    fun rememberLazyListState(
        key: String,
        initialFirstVisibleItemIndex: Int = DefaultIndex,
        initialFirstVisibleItemScrollOffset: Int = DefaultOffset
    ): LazyListState {
        val (index, offset) = selectScrollPosition(
            key = key,
            initialFirstVisibleItemIndex,
            initialFirstVisibleItemScrollOffset
        )
        return androidx.compose.foundation.lazy.rememberLazyListState(
            initialFirstVisibleItemIndex = index,
            initialFirstVisibleItemScrollOffset = offset
        ).apply {
            SaveScrollPositionEffect(state = this, key = key)
        }
    }

    @Composable
    fun rememberLazyGridState(
        key: String,
        initialFirstVisibleItemIndex: Int = DefaultIndex,
        initialFirstVisibleItemScrollOffset: Int = DefaultOffset
    ): LazyGridState {
        val (index, offset) = selectScrollPosition(
            key = key,
            initialFirstVisibleItemIndex,
            initialFirstVisibleItemScrollOffset
        )
        return androidx.compose.foundation.lazy.grid.rememberLazyGridState(
            initialFirstVisibleItemIndex = index,
            initialFirstVisibleItemScrollOffset = offset
        ).apply {
            SaveScrollPositionEffect(state = this, key = key)
        }
    }

    @Composable
    fun rememberLazyStaggeredGridState(
        key: String,
        initialFirstVisibleItemIndex: Int = DefaultIndex,
        initialFirstVisibleItemScrollOffset: Int = DefaultOffset
    ): LazyStaggeredGridState {
        val (index, offset) = selectScrollPosition(
            key = key,
            initialFirstVisibleItemIndex,
            initialFirstVisibleItemScrollOffset
        )
        return androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState(
            initialFirstVisibleItemIndex = index,
            initialFirstVisibleItemScrollOffset = offset
        ).apply {
            SaveScrollPositionEffect(state = this, key = key)
        }
    }

    @Composable
    private fun selectScrollPosition(
        key: String,
        initialFirstVisibleItemIndex: Int,
        initialFirstVisibleItemScrollOffset: Int
    ): Pair<Int, Int> {
        if (initialFirstVisibleItemIndex != DefaultIndex
            || initialFirstVisibleItemScrollOffset != DefaultOffset
        ) {
            return initialFirstVisibleItemIndex to initialFirstVisibleItemScrollOffset
        }
        return map[key] ?: Pair(DefaultIndex, DefaultOffset)
    }

    @Composable
    private fun SaveScrollPositionEffect(state: Any, key: String) {
        DisposableEffect(key1 = Unit) {
            onDispose {
                map[key] = getScrollPosition(state)
            }
        }
    }

    private fun getScrollPosition(listState: Any): Pair<Int, Int> = when (listState) {
        is LazyListState -> listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        is LazyGridState -> listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        is LazyStaggeredGridState -> listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        else -> error("Unknown listState: $listState")
    }
}