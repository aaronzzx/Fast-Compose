package com.aaron.fastcompose.paging3

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aaron.compose.component.Pagingable
import com.aaron.compose.component.ViewStateable
import com.aaron.compose.component.ViewStateable.Result
import com.aaron.compose.component.viewStateable
import com.aaron.compose.defaults.Defaults
import com.aaron.compose.ktx.buildPageData
import com.aaron.compose.ktx.isEmpty
import com.aaron.compose.paging.PageConfigDefaults
import com.aaron.compose.paging.PageData
import com.aaron.compose.ui.refresh.SmartRefreshType
import com.aaron.fastcompose.RepoEntity
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/10/24
 */
class PagingVM : ViewModel(), ViewStateable by viewStateable(), Pagingable<Int, Repo> {

    companion object {
        init {
            Defaults.SuccessCode = 200
            with(PageConfigDefaults) {
                DefaultPrefetchDistance = 0
                DefaultInitialSize = 5
                DefaultPageSize = 5
                DefaultMaxPage = 5
                DefaultRequestTimeMillis = 0
            }
        }
    }

    override val smartRefreshType: MutableState<SmartRefreshType> = mutableStateOf(SmartRefreshType.Idle)

    override val pageData: PageData<Int, Repo> = buildPageData(1, onRequest = ::buildFakeData2)

    private suspend fun buildFakeData2(page: Int, pageSize: Int): RepoEntity {
        delay(2000)

        val code = Random(System.currentTimeMillis()).nextInt(0, 4)
        Log.d("zzx", "code: $code")
        val safePageData = runCatching { pageData }.getOrNull()
        Log.d("zzx", "$safePageData")
        return when (code) {
            0 -> {
                showLoading(false)
                finishRefresh(false)
                if (page == 1) {
                    showResult(Result.Failure(404, "Not Found"))
                }
                RepoEntity(404, "Not Found", emptyList())
            }
            1 -> {
                showLoading(false)
                finishRefresh(false)
                if (page == 1) {
                    showResult(Result.Error(IllegalStateException("Internal Error")))
                }
                throw IllegalStateException("Internal Error")
            }
            else -> {
                val list = gitHubService.searchRepos(page, pageSize).data
                showLoading(false)
                finishRefresh(true)
                showResult(if (safePageData?.isEmpty == true && list.isEmpty()) Result.Empty else Result.Default)
                RepoEntity(200, "OK", list)
            }
        }
    }

    init {
        showLoading(true)
    }

    override fun finishRefresh(success: Boolean, delay: Long) {
        super.finishRefresh(success, delay.coerceAtLeast(300))
    }

    fun deleteItem(index: Int) {
        viewModelScope.launchWithLoading {
            delay(1000)
            pageData.data.removeAt(index)
        }
    }

    override fun retry() {
        showLoading(true)
        refreshIgnoreAnimation()
    }
}