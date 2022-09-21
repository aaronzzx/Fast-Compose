package com.aaron.compose.architecture.paging

import androidx.compose.runtime.Stable
import com.aaron.compose.architecture.paging.PageConfigDefaults.DefaultEnableLoadMore
import com.aaron.compose.architecture.paging.PageConfigDefaults.DefaultInitialSize
import com.aaron.compose.architecture.paging.PageConfigDefaults.DefaultMaxPage
import com.aaron.compose.architecture.paging.PageConfigDefaults.DefaultMaxSize
import com.aaron.compose.architecture.paging.PageConfigDefaults.DefaultPageSize
import com.aaron.compose.architecture.paging.PageConfigDefaults.DefaultPrefetchDistance
import com.aaron.compose.architecture.paging.PageConfigDefaults.DefaultPrintError
import com.aaron.compose.architecture.paging.PageConfigDefaults.DefaultRequestTimeMillis

/**
 * 默认分页配置
 */
object PageConfigDefaults {

    /** 默认每页个数 */
    var DefaultPageSize = 15

    /** 默认最大页数 */
    var DefaultMaxPage = Int.MAX_VALUE

    /** 默认初始化个数 */
    var DefaultInitialSize = 15

    /** 默认预取距离，在倒数第几个时进行加载操作 */
    var DefaultPrefetchDistance = DefaultPageSize

    /** 默认分页最多可以加载多少 */
    var DefaultMaxSize = Int.MAX_VALUE

    /** 默认是否开启加载更多 */
    var DefaultEnableLoadMore = true

    /** 默认最小请求时间 */
    var DefaultRequestTimeMillis = 500L

    /** 默认是否打印异常 */
    var DefaultPrintError = false
}

/**
 * 通用分页配置
 */
@Stable
data class PageConfig(
    /** 每页个数 */
    val pageSize: Int = DefaultPageSize,

    /** 最大页数 */
    val maxPage: Int = DefaultMaxPage,

    /** 初始化个数 */
    val initialSize: Int = DefaultInitialSize,

    /** 预取距离，在倒数第几个时进行加载操作 */
    val prefetchDistance:Int = DefaultPrefetchDistance,

    /** 最多可以加载多少 */
    val maxSize:Int = DefaultMaxSize,

    /** 是否开启加载更多 */
    val enableLoadMore: Boolean = DefaultEnableLoadMore,

    /** 设置最小需要多少毫秒才能完成一次请求 */
    val minRequestTimeMillis: Long = DefaultRequestTimeMillis,

    /** 是否打印异常 */
    val printError: Boolean = DefaultPrintError
)