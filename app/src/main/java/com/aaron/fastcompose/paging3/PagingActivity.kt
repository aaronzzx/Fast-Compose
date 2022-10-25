package com.aaron.fastcompose.paging3

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aaron.compose.base.BaseComposeActivity
import com.aaron.compose.component.PagingComponent
import com.aaron.compose.component.PagingComponentFooter
import com.aaron.compose.component.PagingGridComponent
import com.aaron.compose.component.PagingStateComponent
import com.aaron.compose.component.RefreshComponent
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ktx.itemsIndexed
import com.aaron.compose.ktx.onClick
import com.aaron.compose.ui.TopBar
import com.aaron.compose.ui.refresh.SmartRefreshIndicator
import com.aaron.fastcompose.R
import com.aaron.fastcompose.ui.theme.FastComposeTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

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
                modifier = Modifier
                    .fillMaxSize(),
                elevation = 4.dp,
                color = Color(0xFFF0F0F0)
            ) {
                Column {
                    TopBar(
                        modifier = Modifier.zIndex(1f),
                        title = "PagingActivity",
                        startIcon = R.drawable.back,
                        backgroundColor = Color(0x00F0F0F0),
                        elevation = 0.dp,
                        contentPadding = WindowInsets.statusBars.asPaddingValues(),
                        onStartIconClick = {
                            finishAfterTransition()
                        }
                    )
                    PagingPage()
                }
            }
        }
    }
}

private class MyFooter<K, V> : PagingComponentFooter<K, V>() {

    @Composable
    override fun LoadingContent(component: PagingComponent<K, V>) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PagingPage() {
    val vm = viewModel<PagingVM>()
    RefreshComponent(
        component = vm,
        clipHeaderEnabled = true,
        translateBody = true,
        indicator = { smartRefreshState, triggerPixels, maxDragPixels, indicatorHeight ->
            SmartRefreshIndicator(smartRefreshState, triggerPixels, maxDragPixels, indicatorHeight)
        },
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF0F0F0))
    ) {
        PagingStateComponent(pagingComponent = vm, stateComponent = vm) {
            PagingGridComponent(
                component = vm,
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                footer = { component, footerType ->
                    remember { MyFooter<Int, Repo>() }.Content(
                        component = component,
                        footerType = footerType
                    )
                }
            ) { pageData ->
                itemsIndexed(pageData, key = { _, item -> item.id }) { index, item ->
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