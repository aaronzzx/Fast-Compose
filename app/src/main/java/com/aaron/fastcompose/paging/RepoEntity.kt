package com.aaron.fastcompose.paging

import com.aaron.compose.base.BasePagingResult
import kotlinx.collections.immutable.ImmutableList

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/12/28
 */
data class RepoEntity(
    override val code: Int,
    override val msg: String?,
    override val data: ImmutableList<Repo>
) : BasePagingResult<Repo>