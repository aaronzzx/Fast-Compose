package com.aaron.fastcompose

import androidx.compose.runtime.Stable
import com.aaron.compose.architecture.BaseResult
import com.aaron.compose.architecture.BaseViewStateVM
import com.aaron.compose.ktx.buildPager
import com.aaron.compose.paging.PagerConfigDefaults
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
            PagerConfigDefaults.DefaultPrefetchDistance = 1
            PagerConfigDefaults.DefaultInitialSize = 10
            PagerConfigDefaults.DefaultPageSize = 10
            PagerConfigDefaults.DefaultMaxPage = 3
            PagerConfigDefaults.DefaultRequestTimeMillis = 500
        }
    }

    val refreshState = SmartRefreshState(false)

    val articles = buildPager(
        onTransform = {
            it?.text
        }
    ) { page, pageSize ->
        when (Random(System.currentTimeMillis()).nextInt(0, 10)) {
            0 -> ArticlesEntity(404, "Not Found", emptyList())
            1 -> throw IllegalStateException("Internal Error")
            else -> ArticlesEntity(200, "OK", MockData.loadArticles(page, pageSize))
        }
    }
}

data class ArticlesEntity(
    override val code: Int,
    override val msg: String?,
    val text: List<String>
) : BaseResult

object MockData {

    suspend fun loadArticles(page: Int, pageSize: Int): List<String> {
        delay(Random(System.currentTimeMillis()).nextLong(0, 2000))
        val list = arrayListOf<String>()
        val random = Random(System.currentTimeMillis()).nextInt(1, 100000)
        repeat(pageSize) {
            list.add("$page-$it-$random")
        }
        return list
    }
}