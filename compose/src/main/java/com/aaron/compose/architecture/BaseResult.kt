package com.aaron.compose.architecture

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
data class LocalResult<T>(
    override val code: Int,
    override val msg: String?,
    val data: T
) : BaseResult