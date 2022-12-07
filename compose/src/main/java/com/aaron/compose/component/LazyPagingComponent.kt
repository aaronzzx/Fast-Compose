package com.aaron.compose.component

import androidx.compose.runtime.Stable
import com.aaron.compose.paging.PagingScope
import com.aaron.compose.safestate.SafeState
import com.aaron.compose.safestate.SafeStateScope
import kotlinx.collections.immutable.ImmutableList

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/12/8
 */

@Stable
interface LazyPagingComponent<K, V> :
    PagingComponent<K, V>,
    LazyLoadComponent,
    RefreshComponent,
    SafeStateScope

@Stable
interface PagingMultiComponent<K, V> : PagingScope {

    val lazyPagingComponents: SafeState<ImmutableList<LazyPagingComponent<K, V>>>
}