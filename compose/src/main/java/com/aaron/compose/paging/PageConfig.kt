package com.aaron.compose.paging

import androidx.compose.runtime.Stable
import com.aaron.compose.paging.PageConfigDefaults.DefaultEnableLoadMore
import com.aaron.compose.paging.PageConfigDefaults.DefaultInitialSize
import com.aaron.compose.paging.PageConfigDefaults.DefaultMaxPage
import com.aaron.compose.paging.PageConfigDefaults.DefaultPageSize
import com.aaron.compose.paging.PageConfigDefaults.DefaultPrefetchDistance
import com.aaron.compose.paging.PageConfigDefaults.DefaultPrintError

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

    /** 默认是否开启加载更多 */
    var DefaultEnableLoadMore = true

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

    /** 是否开启加载更多 */
    val enableLoadMore: Boolean = DefaultEnableLoadMore,

    /** 是否打印异常 */
    val printError: Boolean = DefaultPrintError
)