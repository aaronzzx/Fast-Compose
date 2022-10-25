package com.aaron.compose.ktx

import com.aaron.compose.paging.PageData

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/9/21
 */

val PageData<*, *>.lastIndex: Int get() = itemCount - 1

val PageData<*, *>.isEmpty: Boolean get() = itemCount == 0

val PageData<*, *>.isNotEmpty: Boolean get() = !isEmpty