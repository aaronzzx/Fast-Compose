package com.aaron.fastcompose.ui.paging

import com.aaron.compose.base.BasePagingResult

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/12/28
 */
data class RepoEntity(
    override val code: Int,
    override val msg: String?,
    override val data: List<Repo>
) : BasePagingResult<Repo>