package com.aaron.compose.architecture.paging

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/9/20
 */
sealed class LoadResult<K, V> {

    data class Page<K, V>(val data: List<V>, val nextPage: K?) : LoadResult<K, V>()

    data class Error<K, V>(val throwable: Throwable) : LoadResult<K, V>()
}
