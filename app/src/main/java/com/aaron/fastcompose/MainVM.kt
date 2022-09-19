package com.aaron.fastcompose

import android.util.Log
import androidx.compose.runtime.Stable
import com.aaron.compose.architecture.BaseResult
import com.aaron.compose.architecture.BaseViewStateVM
import com.aaron.compose.architecture.viewStateFlow
import com.aaron.compose.ui.SmartRefreshState
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/9/17
 */
@Stable
class MainVM : BaseViewStateVM() {

    val refreshState = SmartRefreshState(false)

    val articlesEntity by viewStateFlow<ArticlesEntity>()

    private var page = 1

    init {
        requestData(false)
    }

    fun refresh() {
        page = 1
        requestData(false)
    }

    fun loadMore() {
        page++
        requestData(true)
    }

    private fun requestData(triggerByLoadMore: Boolean) {
        emit(articlesEntity) {
            Log.d("zzx", "page: $page")
            val articles = MockData.loadArticles(page)
            delay(1000)
            val type = Random(System.currentTimeMillis()).nextInt(0, 10)
            if (triggerByLoadMore) {
                if (type == 0 || type == 1) {
                    // failure
                    page--
                }
            }
            when (type) {
                0 -> ArticlesEntity(500, "Network error.", emptyList())
                1 -> error("Internal error.")
                else -> ArticlesEntity(200, "OK", articles)
            }
        }
    }
}

data class ArticlesEntity(
    override val code: Int,
    override val msg: String?,
    val text: List<String>
) : BaseResult

object MockData {

    fun loadArticles(page: Int): List<String> {
        val list = arrayListOf<String>()
        when (page) {
            1 -> list.addAll((1..10).map { "$it" })
            2 -> list.addAll((11..20).map { "$it" })
            3 -> list.addAll((21..30).map { "$it" })
            4 -> list.addAll((31..40).map { "$it" })
            5 -> list.addAll((41..50).map { "$it" })
        }
        return list
    }
}