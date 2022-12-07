package com.aaron.fastcompose.paging3

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aaron.compose.component.LazyPagingComponent
import com.aaron.compose.defaults.Defaults
import com.aaron.compose.paging.PageConfigDefaults
import com.aaron.compose.paging.PageData
import com.aaron.compose.safestate.SafeState
import com.aaron.compose.safestate.SafeStateScope
import com.aaron.compose.safestate.safeStateOf
import com.aaron.compose.ui.refresh.SmartRefreshType
import com.aaron.compose.ui.refresh.SmartRefreshType.FinishRefresh.Companion.DismissDelayMillis
import com.aaron.fastcompose.RepoEntity
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/10/24
 */
class PagingVM : ViewModel(), SafeStateScope {

    companion object {
        init {
            Defaults.SuccessCode = 200
            with(PageConfigDefaults) {
                DefaultPrefetchDistance = 1
                DefaultPageSize = 10
                DefaultMaxPage = 5
            }
        }
    }

    val pagingDelegates: SafeState<ImmutableList<LazyPagingComponent<Int, Repo>>> = safeStateOf(persistentListOf())

    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch {
            val list = List(5) {
                PagingDelegate(viewModelScope) { page, pageSize ->
                    request(page, pageSize)
                }
            }
            pagingDelegates.setValue(persistentListOf(*list.toTypedArray()))
        }
    }

    private suspend fun request(page: Int, pageSize: Int): RepoEntity {
        delay(2000)
        return when (Random(System.currentTimeMillis()).nextInt(0, 8)) {
            0 -> {
                RepoEntity(404, "Not Found", emptyList())
            }
            1 -> {
                throw IllegalStateException("Internal Error")
            }
            else -> {
                val list = gitHubService.searchRepos(page, pageSize).data
                RepoEntity(200, "OK", list)
            }
        }
    }
}

private class PagingDelegate(
    coroutineScope: CoroutineScope,
    onRequest: suspend PageData<Int, Repo>.(Int, Int) -> RepoEntity
) : LazyPagingComponent<Int, Repo> {

    override val pageData: PageData<Int, Repo> = coroutineScope.buildPageData(
        initialPage = 1,
        lazyLoad = true
    ) { page, pageSize ->
        onRequest(page, pageSize)
    }

    override val initialized: SafeState<Boolean> = safeStateOf(false)

    override val smartRefreshType: SafeState<SmartRefreshType> = safeStateOf(SmartRefreshType.Idle)

    init {
        coroutineScope.launch {
            snapshotFlow { pageData.isInitialized }
                .collect {
                    initialized.setValue(it)
                }
        }
    }

    override fun initialize() {
        pagingRefresh()
    }

    override fun refreshIgnoreAnimation() {
        pagingRefresh()
    }

    override fun pagingRefresh() {
        pageData.refresh(
            onSuccess = {
                finishRefresh(true)
            },
            onFailure = {
                finishRefresh(false)
            }
        )
    }

    override fun finishRefresh(success: Boolean, delay: Long) {
        super.finishRefresh(success, delay.coerceAtLeast(DismissDelayMillis))
    }
}