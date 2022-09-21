package com.aaron.compose.ktx

import com.aaron.compose.architecture.paging.PageData

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/9/21
 */

val PageData<*, *>.lastIndex: Int get() = count - 1

val PageData<*, *>.isEmpty: Boolean get() = count == 0

val PageData<*, *>.isNotEmpty: Boolean get() = !isEmpty