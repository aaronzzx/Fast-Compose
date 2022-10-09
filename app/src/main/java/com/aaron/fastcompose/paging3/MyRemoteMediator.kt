package com.aaron.fastcompose.paging3

import android.util.Log
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.aaron.fastcompose.appContext
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/9/27
 */
class MyRemoteMediator : RemoteMediator<Int, Repo>() {

    private val db = RepoDatabase.get(appContext)
    private val dao = db.getRepoDao()
    private val api = githubService

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Repo>): MediatorResult {
        Log.d("zzx", "loadType: $loadType")
        return try {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> null
                // In this example, you never need to prepend, since REFRESH
                // will always load the first page in the list. Immediately
                // return, reporting end of pagination.
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                        ?: return MediatorResult.Success(endOfPaginationReached = true)

                    // You must explicitly check if the last item is null when
                    // appending, since passing null to networkService is only
                    // valid for initial load. If lastItem is null it means no
                    // items were loaded after the initial REFRESH and there are
                    // no more items to load.
//                    lastItem.page + 1
                    1
                }
            }
            // Suspending network load via Retrofit. This doesn't need to be
            // wrapped in a withContext(Dispatcher.IO) { ... } block since
            // Retrofit's Coroutine CallAdapter dispatches on a worker
            // thread.
            Log.d("zzx", "loadKey: $loadKey, loadType: $loadType")
            val page = loadKey ?: 1
            val startTime = System.currentTimeMillis()
            val response = api.searchRepos(page, state.config.pageSize)

            val list = response.items

            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    dao.clearAll()
                }

                // Insert new users into database, which invalidates the
                // current PagingData, allowing Paging to present the updates
                // in the DB.
                dao.insertAll(list.toTypedArray())
            }

            val endTime = System.currentTimeMillis()
            delay((endTime - startTime).coerceAtLeast(500))

            MediatorResult.Success(
                endOfPaginationReached = list.isEmpty()
            )
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }
}