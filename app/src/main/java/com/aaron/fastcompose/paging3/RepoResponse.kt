package com.aaron.fastcompose.paging3

import com.aaron.compose.architecture.BaseResult
import com.google.gson.annotations.SerializedName

class RepoResponse(
    override val code: Int,
    override val msg: String?,
    @SerializedName("items") val items: List<Repo> = emptyList()
) : BaseResult