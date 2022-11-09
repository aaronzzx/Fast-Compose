package com.aaron.fastcompose.paging3

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aaron.compose.component.PagingComponent
import com.aaron.compose.component.RefreshComponent
import com.aaron.compose.component.StateComponent
import com.aaron.compose.component.StateComponent.ViewState
import com.aaron.compose.defaults.Defaults
import com.aaron.compose.ktx.buildPageData
import com.aaron.compose.ktx.isEmpty
import com.aaron.compose.paging.PageConfigDefaults
import com.aaron.compose.paging.PageData
import com.aaron.compose.safestate.SafeState
import com.aaron.compose.safestate.SafeStateScope
import com.aaron.compose.safestate.safeStateOf
import com.aaron.compose.ui.refresh.SmartRefreshType
import com.aaron.fastcompose.RepoEntity
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/10/24
 */
class PagingVM : ViewModel(), StateComponent, RefreshComponent, PagingComponent<Int, Repo>,
    SafeStateScope {

    companion object {
        init {
            Defaults.SuccessCode = 200
            with(PageConfigDefaults) {
                DefaultPrefetchDistance = 1
                DefaultInitialSize = 10
                DefaultPageSize = 10
                DefaultMaxPage = 1
            }
        }
    }

    override val loading: SafeState<Boolean> = safeStateOf(false)
    override val viewState: SafeState<ViewState> = safeStateOf(ViewState.Idle)
    override val smartRefreshType: SafeState<SmartRefreshType> = safeStateOf(SmartRefreshType.Idle)
    override val pageData: PageData<Int, Repo> = buildPageData(initialPage = 1) { page, pageSize ->
        Log.d("zzx", "page: $page")
        delay(2000)

        val code = Random(System.currentTimeMillis()).nextInt(0, 4)
        val safePageData = runCatching { pageData }.getOrNull()
        when (code) {
            0 -> {
                showLoading(false)
                finishRefresh(false)
                if (page == 1) {
                    showState(ViewState.Failure(404, "Not Found"))
                }
                RepoEntity(404, "Not Found", emptyList())
            }
            1 -> {
                showLoading(false)
                finishRefresh(false)
                if (page == 1) {
                    showState(ViewState.Error(IllegalStateException("Internal Error")))
                }
                throw IllegalStateException("Internal Error")
            }
            else -> {
                val list = gitHubService.searchRepos(page, pageSize).data
                showLoading(false)
                finishRefresh(true)
                showState(if (safePageData?.isEmpty == true && list.isEmpty()) ViewState.Empty else ViewState.Idle)
                RepoEntity(200, "OK", list)
            }
        }
    }

    init {
        showLoading(true)
    }

    override fun refreshIgnoreAnimation() {
        pagingRefresh()
    }

    override fun finishRefresh(success: Boolean, delay: Long) {
        super.finishRefresh(success, delay.coerceAtLeast(300))
    }

    fun deleteItem(index: Int) {
        viewModelScope.launchWithLoading {
//            delay(1000)
            pageData.data.edit().removeAt(index)
        }
    }

    override fun retry() {
        showLoading(true)
        refreshIgnoreAnimation()
    }
}