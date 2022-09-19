package com.aaron.fastcompose

import androidx.compose.runtime.Stable
import com.aaron.compose.architecture.BaseResult
import com.aaron.compose.architecture.BaseViewStateVM
import com.aaron.compose.architecture.viewStateFlow
import com.aaron.compose.ui.SmartRefreshState

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/9/17
 */
@Stable
class MainVM : BaseViewStateVM() {

    val refreshState = SmartRefreshState(false)

    val articlesEntity by viewStateFlow<ArticlesEntity>()

    init {
        launch(articlesEntity) {
            ArticlesEntity(200, "Successful", listOf(
                "123",
                "One Two Three",
                "哈哈哈"
            ))
        }
    }
}

data class ArticlesEntity(
    override val code: Int,
    override val msg: String?,
    val text: List<String>
) : BaseResult