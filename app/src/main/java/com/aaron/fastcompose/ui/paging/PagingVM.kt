package com.aaron.fastcompose.ui.paging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aaron.compose.defaults.Defaults
import com.aaron.compose.paging.PageConfigDefaults
import com.aaron.compose.safestate.SafeStateScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/10/24
 */
@HiltViewModel
class PagingVM @Inject constructor() : ViewModel(), SafeStateScope {

    companion object {
        init {
            Defaults.SuccessCode = 200
            with(PageConfigDefaults) {
                DefaultPrefetchDistance = 0
                DefaultPageSize = 10
                DefaultMaxPage = 3
                DefaultPrintError = true
            }
        }
    }

    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch {
//            val list = List(5) {
//                val lazyPagingComponent = viewModelScope.buildPageData(
//                    initialKey = 1,
//                    lazyLoad = true
//                ) { page, pageSize ->
//                    request(page as Int, pageSize)
//                }.toLazyPagingComponent()
//
//                "Tab$$it" to lazyPagingComponent
//            }
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