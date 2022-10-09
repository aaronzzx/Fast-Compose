package com.aaron.fastcompose.paging3

import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aaron.compose.paging.IPagingDao
import kotlinx.coroutines.flow.Flow

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/9/27
 */
@Dao
interface RepoDao : IPagingDao<Int, Repo> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insertAll(users: Array<Repo>)

    @Delete
    suspend fun delete(item: Repo)

    @Query("SELECT * FROM repo")
    override fun pagingSource(): PagingSource<Int, Repo>

    @Query("DELETE FROM repo")
    override suspend fun clearAll()
}