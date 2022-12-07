package com.aaron.fastcompose

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aaron.compose.base.BaseComposeActivity
import com.aaron.compose.ktx.canScroll
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ktx.currentPageDelayed
import com.aaron.compose.ktx.isNotEmpty
import com.aaron.compose.ktx.lazylist.itemsIndexed
import com.aaron.compose.ktx.onClick
import com.aaron.compose.ktx.toPx
import com.aaron.compose.paging.LoadState
import com.aaron.compose.ui.CollapsingScroll
import com.aaron.compose.ui.TopBar
import com.aaron.compose.ui.refresh.SmartRefresh
import com.aaron.compose.ui.refresh.SmartRefreshIndicator
import com.aaron.compose.ui.refresh.SmartRefreshState
import com.aaron.compose.ui.refresh.SmartRefreshType
import com.aaron.compose.ui.refresh.rememberSmartRefreshState
import com.aaron.compose.ui.rememberCollapsingScrollState
import com.aaron.compose.ui.tabrow.NonRippleTab2
import com.aaron.compose.ui.tabrow.ScrollableTabRow2
import com.aaron.compose.ui.tabrow.pagerTabIndicatorOffset2
import com.aaron.compose.utils.OverScrollHandler
import com.aaron.fastcompose.paging3.PagingActivity
import com.aaron.fastcompose.ui.theme.FastComposeTheme
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : BaseComposeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        SecondActivity.start(this)
        PagingActivity.start(this)
    }

    @Composable
    override fun Content() {
        val uiController = rememberSystemUiController()
        uiController.setStatusBarColor(Color.Transparent)
        uiController.systemBarsDarkContentEnabled = true
        BackHandler {
            finish()
        }
        FastComposeTheme {
            Surface(
                modifier = Modifier
                    .fillMaxSize(),
                elevation = 4.dp,
                color = Color(0xFFF0F0F0)
            ) {
                Column {
                    TopBar(
                        modifier = Modifier.zIndex(1f),
                        title = "ComposeActivity",
                        startIcon = R.drawable.back,
                        backgroundColor = Color.Transparent,
                        elevation = 0.dp,
                        contentPadding = WindowInsets.statusBars.asPaddingValues(),
                        onStartIconClick = {
                            finishAfterTransition()
                        }
                    )
//                    MyPager()
//                    Pager()
//                    ViewStateComponent()
                    Collapsing()
                }
            }
        }
    }
}

@Composable
private fun Collapsing() {
    val refreshState = rememberSmartRefreshState(type = SmartRefreshType.Idle)
    val scope = rememberCoroutineScope()
    OverScrollHandler(enabled = false) {
        SmartRefresh(
            state = refreshState,
            onRefresh = {
                scope.launch {
                    refreshState.type = SmartRefreshType.Refreshing
                    delay(1000)
                    refreshState.type = SmartRefreshType.Success()
                }
            },
            onIdle = { refreshState.type = SmartRefreshType.Idle },
            clipHeaderEnabled = false,
            indicator = { smartRefreshState, triggerDistance, maxDragDistance, indicatorHeight ->
                val indicatorHeightPx = indicatorHeight.toPx()
                SmartRefreshIndicator(
                    modifier = Modifier.graphicsLayer {
                        alpha = smartRefreshState.indicatorOffset / (indicatorHeightPx / 2f)
                    },
                    state = smartRefreshState,
                    triggerDistance = triggerDistance,
                    maxDragDistance = maxDragDistance,
                    height = indicatorHeight
                )
            }
        ) {
            Box {
                val collapsingScrollState = rememberCollapsingScrollState()
//                                LaunchedEffect(key1 = collapsingScrollState) {
//                                    launch {
//                                        snapshotFlow { collapsingScrollState.animationState }
//                                            .collect {
//                                                Log.d("zzx", "animationState: $it")
//                                            }
//                                    }
//                                    launch {
//                                        snapshotFlow { collapsingScrollState.isCollapsed }
//                                            .collect {
//                                                Log.d("zzx", "isCollapsed: $it")
//                                            }
//                                    }
//                                }
                val listState = rememberLazyGridState()
                CollapsingScroll(
                    modifier = Modifier.fillMaxSize(),
                    state = collapsingScrollState,
                    allowScrollToExpandedWhenCollapsed = {
                        !listState.canScroll(-1)
                    },
                    header = {
                        NestedHeader()
                    }
                ) {
                    NestedContent(listState)
                }

                FloatingActionButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp),
                    onClick = {
                        scope.launch {
                            if (collapsingScrollState.isCollapsed) {
                                launch {
                                    listState.scrollToItem(0)
                                }
                            }
                            collapsingScrollState.toggle()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Composable
private fun NestedHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(
                color = Color.Green.copy(0.5f)
            )
    )
}

@Composable
private fun NestedContent(lazyGridState: LazyGridState) {
    Column {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f)
                .border(
                    width = 2.dp,
                    color = Color.Black
                ),
            painter = painterResource(id = R.drawable.ide_bg),
            contentScale = ContentScale.Crop,
            contentDescription = null
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = lazyGridState
        ) {
            items(24) { index ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(
                            color = when (index % 2) {
                                0 -> Color.Red.copy(0.5f)
                                else -> Color.Blue.copy(0.5f)
                            }
                        )
                        .border(
                            width = 2.dp,
                            color = Color.Black
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$index",
                        color = Color.White,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun Pager() {
    Column {
        val pagerState = rememberPagerState()
        val curPage by pagerState.currentPageDelayed()
        val pageCount = 20

        ScrollableTabRow2(
            modifier = Modifier.fillMaxWidth(),
            selectedTabIndex = curPage,
            edgePadding = 0.dp,
            backgroundColor = Color.Transparent,
            contentColor = Color.Black,
            divider = {
            },
            indicator = {
                Box(
                    modifier = Modifier.pagerTabIndicatorOffset2(pagerState, it)
                ) {
                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .width(16.dp)
                            .height(4.dp)
                            .background(
                                color = MaterialTheme.colors.primary,
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }
            }
        ) {
            val scope = rememberCoroutineScope()
            (1..pageCount).forEachIndexed { index, item ->
                val selected = curPage == index
                NonRippleTab2(
                    text = {
                        Text(
                            text = "$item",
                            fontSize = 24.sp
                        )
                    },
                    selected = selected,
                    selectedContentColor = MaterialTheme.colors.primary,
                    unselectedContentColor = Color.Black.copy(0.35f),
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                )
            }
        }
        HorizontalPager(
            count = pageCount,
            state = pagerState
        ) { page ->
            if (page % 2 == 0) {
                ViewStateComponent()
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = if (page % 2 == 0) Color.Red.copy(0.5f) else Color.Green.copy(
                                0.5f
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun ViewStateComponent() {
    val vm = viewModel<TestVM>()
    val data = vm.data
    TestComposable(
        refreshComponent = vm,
        stateComponent = vm,
        onRemoveItem = {
            vm.deleteItem(it)
        },
        data = data
    )
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
    val refreshState = rememberSmartRefreshState(type = vm.smartRefreshType)
    val listState = rememberLazyGridState()
    val articles = vm.repos
    val loadState = articles.loadState

    val loadStateRefresh = loadState.refresh
    if (refreshState.isRefreshing) {
        if (loadStateRefresh is LoadState.Idle) {
            LaunchedEffect(loadStateRefresh) {
                listState.scrollToItem(0)
            }
            vm.smartRefreshType = SmartRefreshType.Success()
        } else if (loadStateRefresh is LoadState.Error) {
            vm.smartRefreshType = SmartRefreshType.Failure()
        }
    } else if (loadStateRefresh is LoadState.Loading && !vm.init) {
        vm.smartRefreshType = SmartRefreshType.Refreshing
    }

    if (vm.init) {
        vm.init = false
    }

    SmartRefresh(
        state = refreshState,
        onRefresh = {
            vm.refresh()
        },
        onIdle = {
            vm.smartRefreshType = SmartRefreshType.Idle
        },
        translateBodyEnabled = false,
        indicator = { smartRefreshState, triggerDistance, maxDragDistance, height ->
            JialaiRefreshIndicator(
                refreshState = smartRefreshState,
                triggerDistance = triggerDistance,
                maxDragDistance = maxDragDistance,
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
                            text = article.name,
                            color = Color(0xFF333333),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
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