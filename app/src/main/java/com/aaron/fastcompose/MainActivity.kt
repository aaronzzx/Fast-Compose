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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aaron.compose.base.BaseComposeActivity
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ktx.onClick
import com.aaron.compose.ktx.toDp
import com.aaron.compose.ktx.toPx
import com.aaron.compose.ui.CircularProgress
import com.aaron.compose.ui.SmartRefresh
import com.aaron.compose.ui.SmartRefreshState
import com.aaron.compose.ui.TopBar
import com.aaron.fastcompose.ui.theme.FastComposeTheme
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlin.math.roundToInt
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
    SmartRefresh(
        state = refreshState,
        onRefresh = {
            refreshState.refreshing()
        },
        onIdle = {
            refreshState.idle()
        },
        indicator = { _refreshState, triggerPx, maxDragPx, height ->
//            MyIndicator(_refreshState, triggerPx, maxDragPx, height)
            JialaiIndicator(
                refreshState = _refreshState,
                triggerPx = triggerPx,
                maxDragPx = maxDragPx,
                height = height,
                modifier = Modifier
                    .padding(16.dp)
                    .background(
                        color = Color.Green.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
            )
        },
        indicatorHeight = 100.dp,
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
                            when (Random(System.currentTimeMillis()).nextBoolean()) {
                                true -> refreshState.success()
                                else -> refreshState.failure()
                            }
                        }
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
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