package com.aaron.compose.base

import androidx.compose.runtime.Stable

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/9/18
 */
@Stable
interface BaseResult {

    val code: Int

    val msg: String?
}

@Stable
interface BasePagingResult<E> : BaseResult {

    val data: List<E>
}