package com.aaron.fastcompose.paging3

import com.aaron.compose.base.BasePagingResult
import com.google.gson.annotations.SerializedName

class RepoResponse(
    @SerializedName("items") val items: List<Repo> = emptyList()
) : BasePagingResult<Repo> {

    override val code: Int
        get() = 200
    override val msg: String
        get() = "OK"
    override val data: List<Repo>
        get() = items
}