package com.aaron.fastcompose.paging3

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.map
import com.aaron.compose.paging.buildBaseReadablePager
import com.aaron.compose.paging.buildReadablePager
import com.aaron.compose.paging.buildWriteablePager
import com.aaron.fastcompose.appContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/9/27
 */
@HiltViewModel
class PagingVM @Inject constructor() : ViewModel() {

    val api = githubService
    val db = RepoDatabase.get(appContext)
    val userDao = db.getRepoDao()

    val repos = buildReadablePager(successCode = 0, onTransform = {
        it.items
    }) { pageKey, pageSize ->
        api.searchRepos(pageKey, pageSize)
    }.flow.cachedIn(viewModelScope)

    var init by mutableStateOf(true)

    fun deleteItem(item: Repo?) {
//        item ?: return
//        viewModelScope.launch {
//            userDao.delete(item)
//        }
    }
}