package com.aaron.fastcompose.paging

import com.aaron.compose.base.BasePagingResult
import com.google.gson.annotations.SerializedName
import kotlinx.collections.immutable.ImmutableList

class RepoResponse(
    @SerializedName("items") val items: ImmutableList<Repo>
) : BasePagingResult<Repo> {

    override val code: Int
        get() = 200
    override val msg: String
        get() = "OK"
    override val data: ImmutableList<Repo>
        get() = items
}