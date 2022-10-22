package com.aaron.fastcompose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.aaron.compose.base.BasePagingResult
import com.aaron.compose.defaults.Defaults
import com.aaron.compose.ktx.buildPageData
import com.aaron.compose.paging.PageConfigDefaults
import com.aaron.compose.ui.refresh.SmartRefreshType
import com.aaron.fastcompose.paging3.Repo
import com.aaron.fastcompose.paging3.gitHubService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.random.Random

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/9/17
 */
@HiltViewModel
class MainVM @Inject constructor() : ViewModel() {

    companion object {
        init {
            Defaults.SuccessCode = 200
            with(PageConfigDefaults) {
                DefaultPrefetchDistance = 1
                DefaultInitialSize = 5
                DefaultPageSize = 5
//                DefaultMaxPage = 5
                DefaultRequestTimeMillis = 1000
            }
        }
    }

    var init by mutableStateOf(true)

    val repos = buildPageData(1, onRequest = ::buildFakeData2)

    var smartRefreshType: SmartRefreshType by mutableStateOf(SmartRefreshType.Idle)

    private suspend fun buildFakeData2(page: Int, pageSize: Int): RepoEntity {
        return when (Random(System.currentTimeMillis()).nextInt(0, 10)) {
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
        repos.data.removeAt(index)
    }
}

data class RepoEntity(
    override val code: Int,
    override val msg: String?,
    override val data: List<Repo>
) : BasePagingResult<Repo>