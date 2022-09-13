package com.aaron.fastcompose

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.aaron.compose.base.BaseComposeActivity
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ktx.onClick
import com.aaron.compose.ktx.requireActivity
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
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : BaseComposeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SecondActivity.start(this)
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
                        title = "",
                        contentPadding = WindowInsets.statusBars.asPaddingValues(),
                        startLayout = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TopBarBackIcon()
                                TopBarTitle()
                            }
                        }
                    )
                    MyPager()
                }
            }
        }
    }
}

@Composable
private fun TopBarBackIcon() {
    val activity = LocalContext.current.requireActivity()
    IconButton(
        onClick = {
            activity.finishAfterTransition()
        }
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = null,
            tint = LocalContentColor.current
        )
    }
}

@Composable
private fun TopBarTitle() {
    Text(
        text = "天下第一",
        color = LocalContentColor.current,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1
    )
}

@Composable
private fun MyPager() {
    val lazyListState = rememberLazyListState()
    val lazyGridState = rememberLazyGridState()
    val scrollState = rememberScrollState()
    val userScrollEnabled by remember {
        derivedStateOf {
            !lazyListState.isScrollInProgress
                    && !lazyGridState.isScrollInProgress
                    && !scrollState.isScrollInProgress
        }
    }
    HorizontalPager(
        count = 3,
        userScrollEnabled = userScrollEnabled
    ) { page ->
        when (page) {
            0 -> SmartRefreshList(lazyListState)/*JustList()*/
            1 -> SmartRefreshGrid(lazyGridState)/*JustGrid()*/
            else -> /*SmartRefresh(scrollState)*/JustScroll()
        }
    }
}

@Composable
private fun SmartRefreshList(listState: LazyListState) {
    val refreshState = rememberSmartRefreshState(isRefreshing = false)
    SmartRefreshList(
        onRefresh = { refreshState.finishRefresh(true) },
        refreshState = refreshState,
        modifier = Modifier.background(color = Color(0xFFF0F0F0)),
        listState = listState,
        listConfig = LazyListConfig(
            modifier = Modifier.background(color = Color(0xFFF0F0F0)),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        )
    ) {
        items(21) {
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
private fun JustList() {
    LazyColumn(
        modifier = Modifier.background(color = Color(0xFFF0F0F0)),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(21) {
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
private fun SmartRefreshGrid(listState: LazyGridState) {
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
        listState = listState,
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
private fun JustGrid() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.background(color = Color(0xFFF0F0F0)),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
private fun SmartRefresh(listState: ScrollState) {
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
        listState = listState,
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
                        text = "$it",
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
private fun JustScroll() {
    Box(
        modifier = Modifier
            .background(color = Color(0xFFF0F0F0))
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
            .padding(PaddingValues(8.dp))
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
                        text = "$it",
                        color = Color(0xFF333333),
                        fontWeight = FontWeight.Bold,
                        fontSize = 56.sp
                    )
                }
            }
        }
    }
}