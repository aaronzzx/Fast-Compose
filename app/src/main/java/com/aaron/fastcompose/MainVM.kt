package com.aaron.fastcompose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aaron.compose.base.BasePagingResult
import com.aaron.compose.defaults.Defaults
import com.aaron.compose.paging.PageConfigDefaults
import com.aaron.compose.paging.PagingScope
import com.aaron.compose.safestate.SafeStateScope
import com.aaron.compose.ui.refresh.SmartRefreshType
import com.aaron.fastcompose.paging3.Repo
import com.aaron.fastcompose.paging3.gitHubService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import javax.inject.Inject
import kotlin.random.Random

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/9/17
 */
@HiltViewModel
class MainVM @Inject constructor() : ViewModel(), SafeStateScope, PagingScope {

    companion object {
        init {
            Defaults.SuccessCode = 200
            with(PageConfigDefaults) {
                DefaultPrefetchDistance = 1
                DefaultPageSize = 5
//                DefaultMaxPage = 5
            }
        }
    }

    var init by mutableStateOf(true)

    val repos = viewModelScope.buildPageData(initialPage = 1) { page, pageSize ->
        buildFakeData2(page, pageSize)
    }

    var smartRefreshType: SmartRefreshType by mutableStateOf(SmartRefreshType.Idle)

    private suspend fun buildFakeData2(page: Int, pageSize: Int): RepoEntity {
        return when (Random(System.currentTimeMillis()).nextInt(0, 10)) {
            0 -> {
                RepoEntity(404, "Not Found", persistentListOf())
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

    fun refresh() {
        repos.refresh()
    }

    fun loadMore() {
        repos.loadMore()
    }

    fun retry() {
        repos.retry()
    }

    fun deleteItem(index: Int) {
        repos.data.edit().removeAt(index)
    }
}

data class RepoEntity(
    override val code: Int,
    override val msg: String?,
    override val data: ImmutableList<Repo>
) : BasePagingResult<Repo>