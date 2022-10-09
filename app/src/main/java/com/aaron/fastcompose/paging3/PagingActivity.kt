package com.aaron.fastcompose.paging3

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.aaron.compose.base.BaseComposeActivity
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ktx.itemsIndexed
import com.aaron.compose.ktx.onClick
import com.aaron.compose.ui.SmartRefresh
import com.aaron.compose.ui.TopBar
import com.aaron.compose.ui.rememberSmartRefreshState
import com.aaron.fastcompose.JialaiIndicator
import com.aaron.fastcompose.R
import com.aaron.fastcompose.ui.theme.FastComposeTheme
import com.google.accompanist.placeholder.placeholder

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/9/27
 */
class PagingActivity : BaseComposeActivity() {

    @Composable
    override fun Content() {
        FastComposeTheme {
            Surface(
                modifier = Modifier
                    .fillMaxSize(),
                elevation = 4.dp,
                color = MaterialTheme.colors.background
            ) {
                Column {
                    TopBar(
                        modifier = Modifier.zIndex(1f),
                        title = "PagingActivity",
                        startIcon = R.drawable.back,
                        contentPadding = WindowInsets.statusBars.asPaddingValues(),
                        onStartIconClick = {
                            finishAfterTransition()
                        }
                    )
                    SmartRefreshList()
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SmartRefreshList(vm: PagingVM = viewModel()) {
    val refreshState = rememberSmartRefreshState(isRefreshing = false)
    val listState = rememberLazyListState()
    val articles = vm.repos.collectAsLazyPagingItems()
    val loadState = articles.loadState

    val append = loadState.append

    val loadStateRefresh = loadState.refresh
    if (refreshState.isRefreshing) {
        if (loadStateRefresh is LoadState.NotLoading) {
            LaunchedEffect(Unit) {
                listState.scrollToItem(0)
            }
            refreshState.success()
        } else if (loadStateRefresh is LoadState.Error) {
            refreshState.failure()
        }
    } else if (loadStateRefresh is LoadState.Loading && !vm.init) {
        refreshState.refresh()
    }

    if (vm.init) {
        vm.init = false
    }

    SmartRefresh(
        state = refreshState,
        onRefresh = {
            articles.refresh()
        },
        indicator = { smartRefreshState, triggerPixels, maxDragPixels, height ->
            JialaiIndicator(
                refreshState = smartRefreshState,
                triggerPixels = triggerPixels,
                maxDragPixels = maxDragPixels,
                height = height,
                modifier = Modifier
            )
        },
        indicatorHeight = 100.dp,
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF0F0F0))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val spanCount = 2
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(
                    items = articles,
                    key = { index, item ->
                        item.id
                    }
                ) { index, article ->
                    Box(
                        modifier = Modifier
//                            .animateItemPlacement()
                            .clipToBackground(
                                color = Color.White,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .onClick {
                                vm.deleteItem(article)
                            }
                            .fillMaxWidth()
                            .height(200.dp)
                            /*.placeholder(article == null, Color.White)*/,
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$index - ${article?.name}",
                            color = Color(0xFF333333),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                when {
                    loadState.append is LoadState.NotLoading
                            && !loadState.append.endOfPaginationReached
                            && articles.itemCount != 0 -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onClick(enableRipple = true) {
                                        vm.loadMore()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "点我加载更多",
                                    fontSize = 12.sp,
                                    color = Color(0xFF666666),
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                    loadState.append is LoadState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "加载中...",
                                    fontSize = 12.sp,
                                    color = Color(0xFF666666),
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                    loadState.append is LoadState.Error -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .onClick(enableRipple = false) {
                                        articles.retry()
                                    }
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "加载失败，点我重试",
                                    fontSize = 12.sp,
                                    color = Color(0xFF666666),
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                    loadState.append.endOfPaginationReached -> {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "已经到底啦",
                                    fontSize = 12.sp,
                                    color = Color(0xFF666666),
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (articles.itemCount == 0) {
                if (loadState.refresh is LoadState.Loading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                } else if (loadState.refresh is LoadState.Error) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier.size(60.dp),
                            imageVector = Icons.Filled.Home,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}