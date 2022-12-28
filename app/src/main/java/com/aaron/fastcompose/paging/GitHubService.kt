package com.aaron.fastcompose.paging

import com.aaron.compose.utils.typeadapter.ImmutableListTypeAdapterFactory
import com.google.gson.GsonBuilder
import com.google.gson.internal.ConstructorConstructor
import com.google.gson.internal.bind.CollectionTypeAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface GitHubService {

    @GET("search/repositories?sort=stars&q=Android")
    suspend fun searchRepos(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): RepoResponse

    companion object {
        private const val BASE_URL = "https://api.github.com/"

        fun create(): GitHubService {
            val gson = GsonBuilder()
                .registerTypeAdapterFactory(
                    ImmutableListTypeAdapterFactory(
                        CollectionTypeAdapterFactory(
                            ConstructorConstructor(mapOf())
                        )
                    )
                )
                .create()
            return Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
                    .create(GitHubService::class.java)
        }
    }

}

val gitHubService by lazy { GitHubService.create() }