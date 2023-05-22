package com.aaron.fastcompose.ui.paging

import com.aaron.compose.base.BasePagingResult
import com.google.gson.annotations.SerializedName

class RepoResponse(
    @SerializedName("items") val items: List<Repo>
) : BasePagingResult<Repo> {

    override val code: Int
        get() = 200
    override val msg: String
        get() = "OK"
    override val data: List<Repo>
        get() = items
}