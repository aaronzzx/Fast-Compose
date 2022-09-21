package com.aaron.fastcompose

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aaron.compose.architecture.paging.LoadState
import com.aaron.compose.architecture.paging.items
import com.aaron.compose.architecture.paging.itemsIndexed
import com.aaron.compose.base.BaseComposeActivity
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ktx.isNotEmpty
import com.aaron.compose.ktx.onClick
import com.aaron.compose.ktx.toPx
import com.aaron.compose.ui.SmartRefresh
import com.aaron.compose.ui.SmartRefreshState
import com.aaron.compose.ui.TopBar
import com.aaron.fastcompose.ui.theme.FastComposeTheme
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : BaseComposeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        SecondActivity.start(this)
    }

    @Composable
    override fun Content() {
        val uiController = rememberSystemUiController()
        uiController.setStatusBarColor(Color.Transparent)
        BackHandler {
            finish()
        }
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
                        title = "ComposeActivity",
                        startIcon = R.drawable.back,
                        contentPadding = WindowInsets.statusBars.asPaddingValues(),
                        onStartIconClick = {
                            finishAfterTransition()
                        }
                    )
                    MyPager()
                }
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun MyPager() {
    val pagerState = rememberPagerState()
    HorizontalPager(
        count = 3,
        state = pagerState,
    ) { page ->
        when (page) {
            0 -> {
                SmartRefreshList()
            }
            1 -> {
//                SmartRefreshGrid()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .background(
                            color = Color.Blue.copy(0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                )
            }
            else -> {
//                SmartRefresh(page + 1)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .background(
                            color = Color.Red.copy(0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                )
            }
        }
    }
}

@Composable
private fun MyIndicator(
    refreshState: SmartRefreshState,
    triggerPx: Float,
    maxDragPx: Float,
    height: Dp
) {
    val indicatorHeight = height.toPx()
    val offset =
        (maxDragPx - indicatorHeight).coerceAtMost(refreshState.indicatorOffset - indicatorHeight)
    val releaseToRefresh = offset > triggerPx - indicatorHeight

    val arrowRotation = remember { Animatable(-90f) }
    LaunchedEffect(releaseToRefresh) {
        val animSpec = tween<Float>()
        if (releaseToRefresh) {
            arrowRotation.animateTo(90f, animationSpec = animSpec)
        } else {
            arrowRotation.animateTo(-90f, animationSpec = animSpec)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .graphicsLayer {
                translationY = offset
            },
        contentAlignment = Alignment.Center
    ) {
        if (refreshState.isIdle) {
            Icon(
                modifier = Modifier
                    .size(36.dp)
                    .graphicsLayer {
                        rotationZ = arrowRotation.value
                    },
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
private fun SmartRefreshList(vm: MainVM = viewModel()) {
    val refreshState = vm.refreshState
    val listState = rememberLazyGridState()
    val articles = vm.articles
    val loadState = articles.loadState

    val loadStateRefresh = loadState.refresh
    if (refreshState.isRefreshing) {
        if (loadStateRefresh is LoadState.Idle) {
            LaunchedEffect(loadStateRefresh) {
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
            vm.refresh()
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
            LazyVerticalGrid(
                columns = GridCells.Fixed(spanCount),
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(articles, key = { index, _ -> index }) { index, article ->
                    Box(
                        modifier = Modifier
                            .clipToBackground(
                                color = Color.White,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .onClick {
                                vm.deleteItem(index)
                            }
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = article,
                            color = Color(0xFF333333),
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }
                }

                when {
                    loadState.loadMore is LoadState.Waiting -> {
                        item(
                            span = {
                                GridItemSpan(spanCount)
                            }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "等待刷新完成",
                                    fontSize = 12.sp,
                                    color = Color(0xFF666666),
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                    loadState.loadMore is LoadState.Idle
                            && !loadState.loadMore.noMoreData
                            && articles.isNotEmpty -> {
                        item(
                            span = {
                                GridItemSpan(spanCount)
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onClick(enableRipple = false) {
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
                    loadState.loadMore is LoadState.Loading -> {
                        item(
                            span = {
                                GridItemSpan(spanCount)
                            }
                        ) {
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
                    loadState.loadMore is LoadState.Error -> {
                        item(
                            span = {
                                GridItemSpan(spanCount)
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .onClick(enableRipple = false) {
                                        vm.retry()
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
                    loadState.loadMore.noMoreData -> {
                        item(
                            span = {
                                GridItemSpan(spanCount)
                            }
                        ) {
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

            if (articles.count == 0) {
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