package com.aaron.compose.ktx

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.aaron.compose.component.LazyPagerPagingComponent
import com.aaron.compose.component.LazyPagingComponent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/12/9
 */

val LazyPagerPagingComponent<*, *, *>.isLazyPagingDataEmpty: Boolean
    get() = lazyPagingData.value.isEmpty()

val LazyPagerPagingComponent<*, *, *>.lazyPagingDataCount: Int
    get() = lazyPagingData.value.size

fun <T, K, V> LazyPagerPagingComponent<T, K, V>.lazyPagingTabAt(page: Int): T {
    return lazyPagingData.value[page].first
}

fun <T, K, V> LazyPagerPagingComponent<T, K, V>.lazyPagingComponentAt(page: Int): LazyPagingComponent<K, V> {
    return lazyPagingData.value[page].second
}

fun <T, K, V> LazyPagerPagingComponent<T, K, V>.lazyPagingTabs(): List<T> {
    return lazyPagingData.value.map { it.first }
}

fun <T, K, V> LazyPagerPagingComponent<T, K, V>.lazyPagingComponents(): List<LazyPagingComponent<K, V>> {
    return lazyPagingData.value.map { it.second }
}

@Composable
fun <T, K, V> LazyPagerPagingComponent<T, K, V>.rememberLazyPagingTabs(): ImmutableList<T> {
    val tabs by remember {
        derivedStateOf {
            lazyPagingTabs().toPersistentList()
        }
    }
    return tabs
}

@Composable
fun <T, K, V> LazyPagerPagingComponent<T, K, V>.rememberLazyPagingComponents(): ImmutableList<LazyPagingComponent<K, V>> {
    val pagingComponents by remember {
        derivedStateOf {
            lazyPagingComponents().toPersistentList()
        }
    }
    return pagingComponents
}