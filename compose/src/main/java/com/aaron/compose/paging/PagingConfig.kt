package com.aaron.compose.paging

import com.aaron.compose.paging.PagingConfigDefaults.DefaultEnableLoadMore
import com.aaron.compose.paging.PagingConfigDefaults.DefaultEnablePlaceholders
import com.aaron.compose.paging.PagingConfigDefaults.DefaultInitialSize
import com.aaron.compose.paging.PagingConfigDefaults.DefaultMaxPage
import com.aaron.compose.paging.PagingConfigDefaults.DefaultMaxSize
import com.aaron.compose.paging.PagingConfigDefaults.DefaultPageSize
import com.aaron.compose.paging.PagingConfigDefaults.DefaultPrefetchDistance
import com.aaron.compose.paging.PagingConfigDefaults.DefaultRequestTimeMillis

/**
 * 默认 Paging 配置
 */
object PagingConfigDefaults {

    /** 默认每页个数 */
    var DefaultPageSize = 15

    /** 默认最大页数 */
    var DefaultMaxPage = Int.MAX_VALUE

    /** 默认初始化个数 */
    var DefaultInitialSize = 15

    /** 默认预取距离，在倒数第几个时进行加载操作 */
    var DefaultPrefetchDistance = 1

    /** 默认 Paging 最多可以加载多少 */
    var DefaultMaxSize = Int.MAX_VALUE

    /** 默认是否提前显示占位图 */
    var DefaultEnablePlaceholders = false

    /** 默认是否开启加载更多 */
    var DefaultEnableLoadMore = true

    /** 默认最小请求时间 */
    var DefaultRequestTimeMillis = 500L
}

/**
 * 通用 Paging 配置，加入了自定义字段，更具体说明可以查看 [androidx.paging.PagingConfig]
 */
data class AppPagingConfig(
    /** 每页个数 */
    val pageSize: Int = DefaultPageSize,

    /** 最大页数 */
    val maxPage: Int = DefaultMaxPage,

    /** 初始化个数 */
    val initialLoadSize: Int = DefaultInitialSize,

    /** 预取距离，在倒数第几个时进行加载操作 */
    val prefetchDistance:Int = DefaultPrefetchDistance,

    /** Paging 最多可以加载多少 */
    val maxSize:Int = DefaultMaxSize,

    /** 提前显示空占位图，在数量已知的情况下 */
    val enablePlaceholders:Boolean = DefaultEnablePlaceholders,

    /** 是否开启加载更多 */
    val enableLoadMore: Boolean = DefaultEnableLoadMore,

    /** 设置最小需要多少毫秒才能完成一次请求 */
    val minRequestTimeMillis: Long = DefaultRequestTimeMillis
)