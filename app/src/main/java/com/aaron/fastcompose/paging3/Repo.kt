package com.aaron.fastcompose.paging3

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "repo")
data class Repo(
    @PrimaryKey(autoGenerate = true) val primaryKey: Int,
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("stargazers_count") val starCount: Int
)