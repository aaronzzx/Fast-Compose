package com.aaron.fastcompose

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.aaron.compose.architecture.BasePagingResult
import com.aaron.compose.architecture.BaseViewStateVM
import com.aaron.compose.architecture.paging.PagingConfigDefaults
import com.aaron.compose.ui.SmartRefreshState
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/9/17
 */
@Stable
class MainVM : BaseViewStateVM() {

    companion object {
        init {
            with(PagingConfigDefaults) {
                DefaultPrefetchDistance = 5
                DefaultInitialSize = 10
                DefaultPageSize = 10
                DefaultMaxPage = 5
                DefaultRequestTimeMillis = 500
            }
        }
    }

    var init by mutableStateOf(true)

    val refreshState = SmartRefreshState(false)

    val articles = buildPager(1) { page, pageSize ->
        when (Random(System.currentTimeMillis()).nextInt(0, 10)) {
            0 -> {
                ArticlesEntity(404, "Not Found", emptyList())
            }
            1 -> {
                throw IllegalStateException("Internal Error")
            }
            else -> {
                val list = MockData.loadArticles(page, pageSize)
                ArticlesEntity(200, "OK", list)
            }
        }
    }

    fun refresh() {
        articles.refresh()
    }

    fun loadMore() {
        articles.loadMore()
    }

    fun retry() {
        articles.retry()
    }

    fun deleteItem(index: Int) {
        articles.data.removeAt(index)
    }
}

data class ArticlesEntity(
    override val code: Int,
    override val msg: String?,
    override val data: List<String>
) : BasePagingResult<String>

object MockData {

    suspend fun loadArticles(page: Int, pageSize: Int): List<String> {
        delay(Random(System.currentTimeMillis()).nextLong(0, 2000))
        val list = arrayListOf<String>()
        val random = Random(System.currentTimeMillis()).nextInt(1, 100)
        repeat(pageSize) {
            list.add("$page-$it-$random")
        }
        return list
    }
}