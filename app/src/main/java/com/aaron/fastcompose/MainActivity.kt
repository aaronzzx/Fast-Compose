package com.aaron.fastcompose

import android.os.Bundle
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aaron.compose.base.BaseComposeActivity
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ktx.onClick
import com.aaron.compose.ui.SmartRefresh
import com.aaron.compose.ui.SmartRefreshType
import com.aaron.compose.ui.TopBar
import com.aaron.compose.ui.rememberSmartRefreshState
import com.aaron.fastcompose.ui.theme.FastComposeTheme
import com.blankj.utilcode.util.ToastUtils
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
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
    val pagerState = rememberPagerState()
    HorizontalPager(
        count = 3,
        state = pagerState,
    ) { page ->
        when (page) {
            0 -> {
                SmartRefreshList(viewModel())
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
private fun SmartRefreshList(vm: MainViewModel) {
    val refreshState = rememberSmartRefreshState(type = vm.refreshType)
    DisposableEffect(key1 = Unit) {
        onDispose {
            vm.refreshType = SmartRefreshType.Idle()
        }
    }
    SmartRefresh(
        state = refreshState,
        onRefresh = {
            vm.refreshType = SmartRefreshType.Refresh()
        },
        onIdle = {
            vm.refreshType = SmartRefreshType.Idle()
        },
        modifier = Modifier.background(color = Color(0xFFF0F0F0))
    ) {
        LazyColumn(
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                            val success =
                                                Random(System.currentTimeMillis()).nextBoolean()
                                            vm.refreshType = when (success) {
                                                true -> SmartRefreshType.Success()
                                                else -> SmartRefreshType.Failure()
                                            }
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
}