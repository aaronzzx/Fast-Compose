package com.aaron.fastcompose.paging3

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aaron.compose.component.LazyPagingComponent
import com.aaron.compose.component.PagingMultiComponent
import com.aaron.compose.component.RefreshComponent
import com.aaron.compose.component.StateComponent
import com.aaron.compose.component.StateComponent.ViewState
import com.aaron.compose.defaults.Defaults
import com.aaron.compose.paging.PageConfigDefaults
import com.aaron.compose.paging.PageData
import com.aaron.compose.safestate.SafeState
import com.aaron.compose.safestate.SafeStateScope
import com.aaron.compose.safestate.safeStateOf
import com.aaron.compose.ui.refresh.SmartRefreshType
import com.aaron.fastcompose.RepoEntity
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/10/24
 */
class PagingVM : ViewModel(),
    StateComponent,
    RefreshComponent,
    PagingMultiComponent,
    SafeStateScope {

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

    override val loading: SafeState<Boolean> = safeStateOf(false)
    override val viewState: SafeState<ViewState> = safeStateOf(ViewState.Idle)
    override val smartRefreshType: SafeState<SmartRefreshType> = safeStateOf(SmartRefreshType.Idle)

    val lazyPagingComponents: SafeState<ImmutableList<LazyPagingComponent<Int, Repo>>> = safeStateOf(persistentListOf())

    init {
        showLoading(true)
        refreshIgnoreAnimation()
    }

    private suspend fun PageData<Int, Repo>.request(page: Int, pageSize: Int): RepoEntity {
        delay(2000)

        val code = Random(System.currentTimeMillis()).nextInt(0, 4)
        val safePageData = this
        return when (code) {
            0 -> {
//                showLoading(false)
//                finishRefresh(false)
//                if (page == 1) {
//                    showState(ViewState.Failure(404, "Not Found"))
//                }
                RepoEntity(404, "Not Found", emptyList())
            }
            1 -> {
//                showLoading(false)
//                finishRefresh(false)
//                if (page == 1) {
//                    showState(ViewState.Error(IllegalStateException("Internal Error")))
//                }
                throw IllegalStateException("Internal Error")
            }
            else -> {
                val list = gitHubService.searchRepos(page, pageSize).data
//                showState(if (safePageData.isEmpty && list.isEmpty()) ViewState.Empty else ViewState.Idle)
                RepoEntity(200, "OK", list)
            }
        }
    }

    override fun refreshIgnoreAnimation() {
//        pagingRefresh()
        viewModelScope.launch {
            val list = List(5) {
                buildPageData(initialPage = 1, lazyLoad = true) { page, pageSize ->
                    request(page, pageSize)
                }.toLazyPagingComponent()
            }
            lazyPagingComponents.setValue(persistentListOf(*list.toTypedArray()))
            showLoading(false)
            finishRefresh(true)
        }
    }

    override fun finishRefresh(success: Boolean, delay: Long) {
        super.finishRefresh(success, delay.coerceAtLeast(300))
    }

    override fun retry() {
        showLoading(true)
        refreshIgnoreAnimation()
    }
}