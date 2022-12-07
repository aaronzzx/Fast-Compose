package com.aaron.fastcompose.paging3

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aaron.compose.base.BaseComposeActivity
import com.aaron.compose.component.LazyLoadPagerComponent
import com.aaron.compose.component.LazyPagingComponent
import com.aaron.compose.component.PagingComponent
import com.aaron.compose.component.PagingGridComponent
import com.aaron.compose.component.RefreshComponent
import com.aaron.compose.component.VerticalPagingStateFooter
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ktx.lazylist.itemsIndexed
import com.aaron.compose.ktx.lazylist.sections
import com.aaron.compose.ktx.onClick
import com.aaron.compose.ktx.toPx
import com.aaron.compose.ui.TopBar
import com.aaron.compose.ui.WithDivider
import com.aaron.compose.ui.refresh.SmartRefreshIndicator
import com.aaron.compose.utils.OverScrollHandler
import com.aaron.fastcompose.R
import com.aaron.fastcompose.ui.theme.FastComposeTheme
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.collections.immutable.ImmutableList

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/10/24
 */
class PagingActivity : BaseComposeActivity() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, PagingActivity::class.java)
            context.startActivity(intent)
        }
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
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFFF0F0F0)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                0f to Color(0xB32196F3),
                                0.5f to Color(0xFFF0F0F0)
                            )
                        )
                ) {
                    TopBar(
                        modifier = Modifier.zIndex(1f),
                        title = "",
                        startIcon = R.drawable.back,
                        backgroundColor = Color.Transparent,
                        elevation = 0.dp,
                        contentPadding = WindowInsets.statusBars.asPaddingValues(),
                        onStartIconClick = {
                            finishAfterTransition()
                        }
                    )
                    Column(modifier = Modifier.fillMaxSize()) {

//                        LazySection()

                        val vm = viewModel<PagingVM>()
                        val pagingDelegate by vm.pagingDelegates
                        LazyLoadPagingPage(
                            lazyPagingComponents = pagingDelegate,
                            refreshComponents = pagingDelegate
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LazyLoadPagingPage(
    refreshComponents: ImmutableList<RefreshComponent>,
    lazyPagingComponents: ImmutableList<LazyPagingComponent<Int, Repo>>
) {
    val scrollPositions = remember {
        MutableList(lazyPagingComponents.size) {
            0 to 0
        }
    }
    val pagerState = rememberPagerState()
    HorizontalPager(
        modifier = Modifier.fillMaxSize(),
        count = lazyPagingComponents.size,
        state = pagerState
    ) { page ->
        val (index, offset) = scrollPositions[page]
        val listState = rememberLazyGridState(index, offset)
        val lazyPagingComponent = lazyPagingComponents[page]
        val refreshComponent = refreshComponents[page]

        LaunchedEffect(key1 = listState) {
            snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
                .collect {
                    scrollPositions[page] = it
                }
        }

        RefreshComponent(
            component = refreshComponent,
            modifier = Modifier
                .padding(top = 80.dp)
                .fillMaxSize(),
            clipHeaderEnabled = false,
            translateBodyEnabled = true,
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
            LazyLoadPagerComponent(
                modifier = Modifier.fillMaxSize(),
                component = lazyPagingComponent,
                page = page,
                pagerState = pagerState
            ) {
                OverScrollHandler(enabled = false) {
                    PagingGridComponent(
                        state = listState,
                        component = lazyPagingComponent,
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        pagingStateFooter = MyFooter
                    ) {
                        itemsIndexed(lazyPagingComponent, key = { _, item -> item.id }) { index, item ->
                            Box(
                                modifier = Modifier
                                    .animateItemPlacement()
                                    .clipToBackground(
                                        color = Color.White,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .onClick {
                                    }
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.name,
                                    color = Color(0xFF333333),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LazySection() {
    val list = remember {
        List(10) { a ->
            List(10) { b ->
                "${a + 1}-${b + 1}"
            }
        }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        sections(
            sections = list,
            orientation = Orientation.Vertical,
            sectionSpacing = 16.dp,
            sectionBackgroundColor = Color.White,
            sectionShape = RoundedCornerShape(16.dp),
            outerHeader = { sectionIndex ->
                Box(
                    modifier = Modifier.fillParentMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Header-${sectionIndex + 1}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            outerFooter = { sectionIndex ->
                Box(
                    modifier = Modifier.fillParentMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Footer-${sectionIndex + 1}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            innerHeader = { sectionIndex ->
                Box(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Title-${sectionIndex + 1}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            innerFooter = { sectionIndex ->
                Box(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ending-${sectionIndex + 1}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        ) { item ->
            WithDivider(
                color = Color(0xFFF0F0F0),
                startIndent = 24.dp,
                endIndent = 24.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .height(60.dp)
                        .onClick {
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = item)
                }
            }
        }
    }
}

private object MyFooter : VerticalPagingStateFooter() {

    override val loading: (@Composable (PagingComponent<*, *>) -> Unit) = {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
    }

    override val noMoreData: (@Composable (PagingComponent<*, *>) -> Unit) = {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val (line1Id, line2Id, text) = createRefs()
            Text(
                modifier = Modifier
                    .constrainAs(text) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                text = "我是有底线的",
                fontSize = 12.sp,
                color = Color(0xFF999999)
            )
            Box(
                modifier = Modifier
                    .constrainAs(line1Id) {
                        top.linkTo(text.top)
                        bottom.linkTo(text.bottom)
                        end.linkTo(text.start, 16.dp)
                    }
                    .height(1.dp)
                    .aspectRatio(20f)
                    .background(color = Color(0x4D999999))
            )
            Box(
                modifier = Modifier
                    .constrainAs(line2Id) {
                        top.linkTo(text.top)
                        bottom.linkTo(text.bottom)
                        start.linkTo(text.end, 16.dp)
                    }
                    .height(1.dp)
                    .aspectRatio(20f)
                    .background(color = Color(0x4D999999))
            )
        }
    }
}