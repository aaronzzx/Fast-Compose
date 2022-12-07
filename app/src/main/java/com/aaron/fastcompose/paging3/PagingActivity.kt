package com.aaron.fastcompose.paging3

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aaron.compose.base.BaseComposeActivity
import com.aaron.compose.component.LoadingComponent
import com.aaron.compose.component.PagingComponent
import com.aaron.compose.component.PagingHorizontalComponent
import com.aaron.compose.component.RefreshComponent
import com.aaron.compose.component.VerticalPagingStateFooter
import com.aaron.compose.component.pagingComponent
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
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch

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
                    Column {
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
//                        PagingPage()

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
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PagingPage() {
    val vm = viewModel<PagingVM>()
    RefreshComponent(
        component = vm,
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
        },
        modifier = Modifier.fillMaxSize()
    ) {
        LoadingComponent(component = vm) {
            Box {
                val outerListState = rememberLazyListState()
                val innerListState = rememberLazyListState()
                val scope = rememberCoroutineScope()

                OverScrollHandler(enabled = false) {
                    LazyColumn(
                        state = outerListState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Image(
                                painter = painterResource(id = R.drawable.ide_bg),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                                    .background(
                                        color = Color.Red.copy(0.5f),
                                    )
                            )
                        }
                        item(key = "HorizontalList") {
                            OverScrollHandler(enabled = false) {
                                PagingHorizontalComponent(
                                    component = vm,
//                                    columns = GridCells.Fixed(2),
                                    state = innerListState,
                                    modifier = Modifier
                                        .fillParentMaxWidth()
                                        .height(150.dp)
                                        .nestedScroll(object : NestedScrollConnection {
                                            override fun onPreScroll(
                                                available: Offset,
                                                source: NestedScrollSource
                                            ): Offset {
                                                if (available.y < 0
                                                    && outerListState.firstVisibleItemIndex == 0
                                                ) {
                                                    scope.launch {
                                                        outerListState.scrollBy(-available.y)
                                                    }
                                                    return available
                                                }
                                                return super.onPreScroll(available, source)
                                            }
                                        }),
//                                    flingBehavior = rememberSnapFlingBehavior(innerListState),
                                    contentPadding = PaddingValues(8.dp),
//                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
//                                    headerContent = {
//                                        Box(
//                                            modifier = Modifier
//                                                .fillMaxWidth()
//                                                .height(100.dp)
//                                                .background(
//                                                    color = Color.Green.copy(0.5f),
//                                                    shape = RoundedCornerShape(8.dp)
//                                                )
//                                        )
//                                    },
//                                    footerContent = {
//                                        Box(
//                                            modifier = Modifier
//                                                .fillMaxWidth()
//                                                .height(100.dp)
//                                                .background(
//                                                    color = Color.Blue.copy(0.5f),
//                                                    shape = RoundedCornerShape(8.dp)
//                                                )
//                                        )
//                                    },
//                                    pagingStateFooter = MyFooter
                                ) { pageData ->
                                    itemsIndexed(vm, key = { _, item -> item.id }) { index, item ->
                                        Box(
                                            modifier = Modifier
                                                .animateItemPlacement()
                                                .clipToBackground(
                                                    color = Color.White,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .onClick {
                                                    vm.deleteItem(index)
                                                }
                                                .width(100.dp)
                                                .height(150.dp)
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

                val density = LocalDensity.current
                FloatingActionButton(
                    onClick = {
                        if (outerListState.firstVisibleItemIndex == 0) {
                            scope.launch {
                                outerListState.animateScrollToItem(1)
                            }
                        } else {
                            scope.launch {
                                innerListState.scrollToItem(0)
                                outerListState.animateScrollBy(with(density) { -250.dp.toPx() }, spring(stiffness = Spring.StiffnessMediumLow))
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(
                            horizontal = 16.dp,
                            vertical = 32.dp
                        )
                ) {
                    Icon(imageVector = Icons.Default.Done, contentDescription = null)
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

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
private fun Test() {
    val pagingComponent = pagingComponent<Int, String>(
        "Michael",
        "James",
        "Kobe",
        "ABC",
        "CBA",
        "NBA"
    )
    PagingComponent(
        component = pagingComponent,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        pagingStateFooter = MyFooter
    ) { pageData ->
        itemsIndexed(pageData, key = { _, item -> item }) { index, item ->
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
                    .height(100.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item,
                    color = Color(0xFF333333),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}