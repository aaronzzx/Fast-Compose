package com.aaron.compose.paging

import androidx.compose.runtime.Stable

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/9/21
 */
@Stable
sealed class LoadParams<K>(
    val key: K?,
    val config: PageConfig
) {

    val pageSize: Int get() = config.pageSize

    val initialSize: Int get() = config.initialSize

    class Refresh<K>(key: K?, config: PageConfig) : LoadParams<K>(key, config)

    class LoadMore<K>(key: K?, config: PageConfig) : LoadParams<K>(key, config)
}
