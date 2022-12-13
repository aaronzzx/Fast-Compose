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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aaron.compose.base.BaseComposeActivity
import com.aaron.compose.component.LazyPagerPagingComponent
import com.aaron.compose.component.PagingGridComponent
import com.aaron.compose.component.RefreshComponent
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ktx.lazylist.itemsIndexed
import com.aaron.compose.ktx.lazylist.sectionsIndexed
import com.aaron.compose.ktx.onClick
import com.aaron.compose.ui.TopBar
import com.aaron.compose.ui.WithDivider
import com.aaron.compose.utils.OverScrollHandler
import com.aaron.fastcompose.R
import com.aaron.fastcompose.ui.theme.FastComposeTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.collections.immutable.toPersistentList

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
                                0.5f to Color(0xFFF7F7F7)
                            )
                        )
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
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

                        val list = ArrayList<Job>()
                        repeat(5) { outer ->
                            repeat(10) { inner ->
                                list.add(Job("Job(outer-$outer, inner-$inner)", outer))
                            }
                        }

//                        LazySection(list)

                        val vm = viewModel<PagingVM>()
                        LazyLoadPagingPage(lazyPagerPagingComponent = vm)
                    }
                }
            }
        }
    }
}

@Composable
private fun LazyLoadPagingPage(lazyPagerPagingComponent: LazyPagerPagingComponent<String, Int, Repo>) {
    LazyPagerPagingComponent(component = lazyPagerPagingComponent) { lazyPagingComponent ->
        RefreshContent(
            refreshComponent = lazyPagingComponent,
            refreshEnabled = true
        ) {
            OverScrollHandler(enabled = false) {
                PagingGridComponent(
                    component = lazyPagingComponent,
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                    lazyPagingComponent.pagingRefresh()
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

@Composable
private fun RefreshContent(
    refreshEnabled: Boolean,
    refreshComponent: RefreshComponent,
    content: @Composable () -> Unit
) {
    RefreshComponent(
        component = refreshComponent,
        modifier = Modifier.fillMaxSize(),
        swipeEnabled = refreshEnabled,
        content = content
    )
}

@Composable
private fun LazySection(data: List<Job>) {
    val groups = data.groupBy { it.priority }
    val keys = groups.keys.toPersistentList()
    val sections = groups.values.toPersistentList()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
//        contentPadding = PaddingValues(8.dp),
//        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        sectionsIndexed(
            sections = sections,
            orientation = Orientation.Vertical,
            sectionBetweenPadding = 16.dp,
            itemBesidePadding = 16.dp,
            sectionShape = RoundedCornerShape(8.dp),
            sectionBackgroundColor = Color.White,
            header = {
                Box(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(horizontal = 16.dp)
                        /*.padding(vertical = 8.dp)*/,
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Header-${keys[sectionIndex]}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            footer = {
                Box(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(horizontal = 16.dp)
                        /*.padding(vertical = 8.dp)*/,
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Footer-${keys[sectionIndex]}",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF666666)
                    )
                }
            }
        ) { index, item ->
            WithDivider(
                modifier = Modifier.fillMaxWidth(),
                enabled = index != sections[sectionIndex].lastIndex,
                thickness = 1.dp,
                startIndent = 24.dp,
                color = Color(0xFFF2F2F2)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
//                        .clipToBackground(
//                            color = Color.White,
//                            shape = RoundedCornerShape(8.dp)
//                        )
                        .onClick {
                        }
                        .padding(start = 24.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(text = item.title)
                }
            }
        }
    }
}

private data class Job(
    val title: String,
    val priority: Int
)