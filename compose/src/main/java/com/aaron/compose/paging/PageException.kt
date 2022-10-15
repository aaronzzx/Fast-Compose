package com.aaron.compose.paging

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/9/21
 */
class PageException(
    val code: Int,
    val msg: String?
) : Exception("code: $code, msg: ${msg ?: "Unknown"}")
