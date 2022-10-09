package com.aaron.compose.paging

import android.util.Log
import androidx.paging.LoadType
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.paging.RemoteMediator.MediatorResult
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.aaron.compose.architecture.BaseResult
import com.aaron.compose.defaults.Defaults

fun <I : BaseResult, O : Any> buildWriteablePager(
    db: RoomDatabase,
    dao: IPagingDao<Int, O>,
    config: AppPagingConfig = AppPagingConfig(),
    initialPage: Int = 1,
    successCode: Int = Defaults.SuccessCode,
    onTransform: suspend (result: I) -> Array<O>,
    onRequest: suspend (pageKey: Int, pageSize: Int) -> I
): Pager<Int, O> = buildBaseWriteablePager(
    config = config,
    initialKey = null,
    onPagingSource = { dao.pagingSource() },
    onNextKey = { pageKey, state ->
        pageKey?.plus(1) ?: (initialPage + 1)
    },
    onRequest = { params ->
        val loadType = params.loadType
        val pageKey = params.pageKey ?: initialPage
        Log.d("zzx", "page: $pageKey, loadType: $loadType")
        val pageSize = params.pageSize
        val response = onRequest(pageKey, pageSize)
        if (response.code == successCode) {
            val array = onTransform(response)
            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    dao.clearAll()
                }
                dao.insertAll(array)
            }
            val endOfPaginationReached = array.isEmpty()
                    || !config.enableLoadMore
                    || params.state.pages.size >= config.maxPage - 1
            MediatorResult.Success(endOfPaginationReached)
        } else {
            MediatorResult.Error(AppPagingException(response.code, response.msg ?: "None"))
        }
    }
)

fun <K : Any, V : Any> buildBaseWriteablePager(
    config: AppPagingConfig = AppPagingConfig(),
    initialKey: K? = null,
    onPagingSource: () -> PagingSource<K, V>,
    onNextKey: (pageKey: K?, state: PagingState<K, V>) -> K?,
    onRequest: suspend (loadParams: LoadParams<K, V>) -> MediatorResult
): Pager<K, V> = Pager(
    config = PagingConfig(
        pageSize = config.pageSize,
        prefetchDistance = config.prefetchDistance,
        // 必须开启，否则分页异常
        enablePlaceholders = true,
        initialLoadSize = config.initialLoadSize,
        maxSize = config.maxSize
    ),
    initialKey = initialKey,
    pagingSourceFactory = onPagingSource,
    remoteMediator = buildRemoteMediator(
        minRequestTimeMillis = config.minRequestTimeMillis,
        initialKey = initialKey,
        onNextKey = onNextKey,
        onRequest = onRequest
    )
)

fun <K : Any, V : Any> buildRemoteMediator(
    minRequestTimeMillis: Long = PagingConfigDefaults.DefaultRequestTimeMillis,
    initialKey: K? = null,
    onNextKey: (pageKey: K?, state: PagingState<K, V>) -> K?,
    onRequest: suspend (loadParams: LoadParams<K, V>) -> MediatorResult
): RemoteMediator<K, V> = object : RemoteMediator<K, V>() {

    private var lastPageKey: K? = null

    override suspend fun load(loadType: LoadType, state: PagingState<K, V>): MediatorResult {
        val startTime = System.currentTimeMillis()
        return try {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    state.lastItemOrNull()
                        ?: return MediatorResult.Success(endOfPaginationReached = true)
                    onNextKey(lastPageKey, state)
                }
            }
            val pageKey = loadKey ?: initialKey
            val pageSize = state.config.pageSize
            val result = onRequest(LoadParams(loadType, state, pageKey, pageSize))
            makeSureTime(startTime, minRequestTimeMillis)
            lastPageKey = pageKey
            result
        } catch (ex: Exception) {
            makeSureTime(startTime, minRequestTimeMillis)
            MediatorResult.Error(ex)
        }
    }
}

data class LoadParams<K : Any, V : Any>(
    val loadType: LoadType,
    val state: PagingState<K, V>,
    val pageKey: K?,
    val pageSize: Int
)

interface IPagingDao<K : Any, V : Any> {

    suspend fun insertAll(entities: Array<V>)

    suspend fun clearAll()

    fun pagingSource(): PagingSource<K, V>
}

private class RemoteMediatorPagingSource<K : Any, V : Any>(
    val delegate: PagingSource<K, V>
) : PagingSource<K, V>() {

    override val jumpingSupported: Boolean
        get() = delegate.jumpingSupported

    override suspend fun load(params: LoadParams<K>): LoadResult<K, V> {
        Log.d("zzx", "key: ${params.key}")
        return delegate.load(params)
    }

    override fun getRefreshKey(state: PagingState<K, V>): K? {
        return delegate.getRefreshKey(state)
    }
}