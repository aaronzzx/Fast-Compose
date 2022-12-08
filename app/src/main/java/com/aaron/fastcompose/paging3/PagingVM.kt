package com.aaron.fastcompose.paging3

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aaron.compose.component.LazyPagerPagingComponent
import com.aaron.compose.component.LazyPagingComponents
import com.aaron.compose.defaults.Defaults
import com.aaron.compose.ktx.toLazyPagingComponent
import com.aaron.compose.paging.PageConfigDefaults
import com.aaron.compose.safestate.SafeState
import com.aaron.compose.safestate.SafeStateScope
import com.aaron.compose.safestate.safeStateOf
import com.aaron.compose.ui.refresh.SmartRefreshType.FinishRefresh.Companion.DismissDelayMillis
import com.aaron.fastcompose.RepoEntity
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/10/24
 */
class PagingVM : ViewModel(), LazyPagerPagingComponent<Int, Repo>, SafeStateScope {

    companion object {
        init {
            Defaults.SuccessCode = 200
            with(PageConfigDefaults) {
                DefaultPrefetchDistance = 0
                DefaultPageSize = 10
                DefaultMaxPage = 3
            }
        }
    }

    override val lazyPagingComponents: SafeState<LazyPagingComponents<Int, Repo>> =
        safeStateOf(persistentListOf())

    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch {
            val list = List(5) {
                viewModelScope.buildPageData(
                    initialPage = 1,
                    lazyLoad = true
                ) { page, pageSize ->
                    request(page, pageSize)
                }.toLazyPagingComponent(finishRefreshDelayMillis = DismissDelayMillis)
            }
            lazyPagingComponents.setValue(list.toPersistentList())
        }
    }

    private suspend fun request(page: Int, pageSize: Int): RepoEntity {
        delay(2000)
        return when (Random(System.currentTimeMillis()).nextInt(0, 5)) {
//            0 -> {
//                RepoEntity(404, "Not Found", emptyList())
//            }
//            1 -> {
//                throw IllegalStateException("Internal Error")
//            }
            else -> {
                val list = gitHubService.searchRepos(page, pageSize).data
                RepoEntity(200, "OK", list)
            }
        }
    }
}