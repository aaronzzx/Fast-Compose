package com.aaron.fastcompose

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.aaron.compose.base.BaseComposeActivity
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ktx.onClick
import com.aaron.compose.ui.LazyGridConfig
import com.aaron.compose.ui.LazyListConfig
import com.aaron.compose.ui.ScrollConfig
import com.aaron.compose.ui.SmartRefresh
import com.aaron.compose.ui.SmartRefreshGrid
import com.aaron.compose.ui.SmartRefreshList
import com.aaron.compose.ui.TopBar
import com.aaron.compose.ui.rememberSmartRefreshState
import com.aaron.fastcompose.ui.theme.FastComposeTheme
import com.blankj.utilcode.util.ToastUtils
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

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
    Log.d("zzx", "MyPager: ${LocalView.current}")
    var p = LocalView.current.parent
    do {
        Log.d("zzx", "parent: $p")
        p = p?.parent
    } while (p != null)
    Log.d("zzx", "")
    val pagerState = rememberPagerState()
    HorizontalPager(
        count = 3,
        state = pagerState,
        modifier = Modifier.nestedScroll(remember {
            object : NestedScrollConnection {
                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource
                ): Offset {
                    Log.d(
                        "zzx", """
                        HorizontalPager-onPostScroll
                        consumed: $consumed
                        available: $available
                        source: $source
                    """.trimIndent()
                    )
                    return super.onPostScroll(consumed, available, source)
                }
            }
        })
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

@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
private fun SmartRefreshList() {
    val refreshState = rememberSmartRefreshState(isRefreshing = false)
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    SmartRefreshList(
        onRefresh = {
            scope.launch {
                delay(2000)
                refreshState.finishRefresh(true)
            }
        },
        refreshState = refreshState,
        modifier = Modifier.background(color = Color(0xFFF0F0F0)),
        listConfig = LazyListConfig(
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ),
    ) {
        items(21) { index ->
            Box(
                modifier = Modifier
                    .clipToBackground(
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .onClick(
                        enableRipple = true,
                        rippleColor = Color.Red.copy(0.1f),
                        rippleBounded = true
                    ) {
                        ToastUtils.showShort("Happy everyday")
                    }
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                if (index == 0) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(21) { rowIndex ->
                            Box(
                                modifier = Modifier
                                    .clipToBackground(
                                        color = Color.Black.copy(0.1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .onClick(
                                        enableRipple = true,
                                        rippleColor = Color.Red.copy(0.1f),
                                        rippleBounded = true
                                    ) {
                                        ToastUtils.showShort("Happy row everyday")
                                    }
                                    .width(100.dp)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$index-$rowIndex",
                                    color = Color(0xFF333333),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 36.sp
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "$index",
                        color = Color(0xFF333333),
                        fontWeight = FontWeight.Bold,
                        fontSize = 56.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SmartRefreshGrid() {
    val state = rememberSmartRefreshState(isRefreshing = false)
    val scope = rememberCoroutineScope()
    SmartRefreshGrid(
        columns = GridCells.Fixed(3),
        onRefresh = {
            scope.launch {
                delay(1000)
                state.finishRefresh(Random(System.currentTimeMillis()).nextBoolean())
            }
        },
        refreshState = state,
        modifier = Modifier.background(color = Color(0xFFF0F0F0)),
        listConfig = LazyGridConfig(
            modifier = Modifier.background(color = Color(0xFFF0F0F0)),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        )
    ) {
        items(36) {
            Box(
                modifier = Modifier
                    .clipToBackground(
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .onClick(
                        enableRipple = true,
                        rippleColor = Color.Red.copy(0.1f),
                        rippleBounded = true
                    ) {
                        ToastUtils.showShort("Happy everyday")
                    }
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$it",
                    color = Color(0xFF333333),
                    fontWeight = FontWeight.Bold,
                    fontSize = 56.sp
                )
            }
        }
    }
}

@Composable
private fun SmartRefresh(page: Int) {
    val state = rememberSmartRefreshState(isRefreshing = false)
    val scope = rememberCoroutineScope()
    SmartRefresh(
        onRefresh = {
            scope.launch {
                delay(1000)
                state.finishRefresh(Random(System.currentTimeMillis()).nextBoolean())
            }
        },
        refreshState = state,
        modifier = Modifier.background(color = Color(0xFFF0F0F0)),
        listConfig = remember { ScrollConfig(contentPadding = PaddingValues(8.dp)) },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(20) {
                Box(
                    modifier = Modifier
                        .clipToBackground(
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .onClick(
                            enableRipple = true,
                            rippleColor = Color.Red.copy(0.1f),
                            rippleBounded = true
                        ) {
                            ToastUtils.showShort("Happy everyday")
                        }
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$page-$it",
                        color = Color(0xFF333333),
                        fontWeight = FontWeight.Bold,
                        fontSize = 56.sp
                    )
                }
            }
        }
    }
}