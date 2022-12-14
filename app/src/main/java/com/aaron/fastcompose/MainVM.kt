package com.aaron.fastcompose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.aaron.compose.architecture.BasePagingResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.random.Random

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/9/17
 */
@HiltViewModel
class MainVM @Inject constructor() : ViewModel() {

    var init by mutableStateOf(true)

    private suspend fun buildFakeData(page: Int, pageSize: Int): ArticlesEntity {
        return when (Random(System.currentTimeMillis()).nextInt(0, 10)) {
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