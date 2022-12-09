package com.aaron.fastcompose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridItemInfo
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aaron.compose.component.RefreshComponent
import com.aaron.compose.component.StateComponent
import com.aaron.compose.component.StateComponent.ViewState
import com.aaron.compose.ktx.canScroll
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ktx.onClick
import com.aaron.compose.safestate.SafeState
import com.aaron.compose.safestate.SafeStateList
import com.aaron.compose.safestate.SafeStateScope
import com.aaron.compose.safestate.safeStateListOf
import com.aaron.compose.safestate.safeStateOf
import com.aaron.compose.ui.refresh.SmartRefreshType
import com.blankj.utilcode.util.ToastUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/10/14
 */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TestComposable(
    refreshComponent: RefreshComponent,
    stateComponent: StateComponent,
    onRemoveItem: (index: Int) -> Unit,
    data: List<Int>
) {
    RefreshComponent(component = refreshComponent) {
        StateComponent(
            component = stateComponent,
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(0xFFF0F0F0))
        ) {
            val listState = rememberLazyStaggeredGridState()
            LaunchedEffect(key1 = Unit) {
                launch {
                    snapshotFlow { listState.firstVisibleItemScrollOffset to listState.isScrollInProgress }
                        .filter { !listState.isScrollInProgress }
                        .filter { !listState.canScroll(-1) }
                        .collect {
                            ToastUtils.showShort("到顶啦")
                        }
                }
                launch {
                    snapshotFlow { listState.firstVisibleItemScrollOffset to listState.isScrollInProgress }
                        .filter { !listState.isScrollInProgress }
                        .filter { !listState.canScroll(1) }
                        .collect {
                            ToastUtils.showShort("到底啦")
                        }
                }
            }
            LazyHorizontalStaggeredGrid(
                rows = StaggeredGridCells.Fixed(6),
                state = listState,
                modifier = Modifier.fillMaxSize(),
//                contentPadding = PaddingValues(8.dp),
//                verticalArrangement = Arrangement.spacedBy(8.dp),
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(
                    items = data,
                    key = { index, item ->
                        item
                    }
                ) { index, item ->
                    Box(
                        modifier = Modifier
                            .clipToBackground(
                                color = kotlin.run {
                                    if (index % 3 == 1) {
                                        Color.Red.copy(0.1f)
                                    } else if (index % 3 == 2) {
                                        Color.Green.copy(0.1f)
                                    } else {
                                        Color.Blue.copy(0.1f)
                                    }
                                },
//                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = Color.Black
                            )
                            .fillMaxWidth()
                            .aspectRatio(
                                when (index % 5) {
                                    0 -> 1f
                                    1 -> 0.85f
                                    2 -> 0.7f
                                    3 -> 0.5f
                                    else -> 0.2f
                                }
                            )
                            .onClick {
                                onRemoveItem(index)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val info: LazyStaggeredGridItemInfo? = null/*listState.findItemInfo(index = index)*/
                        if (info == null) {
                            Text(text = "$index", color = Color(0xFF333333))
                        } else {
                            Text(
                                text = """
                                    ${index}${info.offset}
                                    lane: ${info.lane}
                                    itemSize: ${info.size}
                                    size: ${listState.layoutInfo.viewportSize}
                                    padding: ${listState.layoutInfo.beforeContentPadding}, ${listState.layoutInfo.afterContentPadding}
                                    offset: ${listState.layoutInfo.viewportStartOffset}, ${listState.layoutInfo.viewportEndOffset}
                                """.trimIndent(),
                                color = Color(0xFF333333),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyStaggeredGridState.findItemInfo(index: Int): LazyStaggeredGridItemInfo? {
    return layoutInfo
        .visibleItemsInfo
        .find {
            it.index == index
        }
}

class TestVM : ViewModel(), RefreshComponent, StateComponent, SafeStateScope {

    override val loading: SafeState<Boolean> = safeStateOf(false)
    override val viewState: SafeState<ViewState> = safeStateOf(ViewState.Idle)
    override val smartRefreshType: SafeState<SmartRefreshType> = safeStateOf(SmartRefreshType.Idle)

    val data: SafeStateList<Int> = safeStateListOf<Int>()

    init {
        initLoad(true)
    }

    override fun refreshIgnoreAnimation() {
        initLoad(false)
    }

    fun deleteItem(index: Int) {
        data.edit().removeAt(index)
    }

    private fun initLoad(enableLoading: Boolean) {
        viewModelScope.launchWithViewState(enableLoading = enableLoading) {
            delay(2000)
            when (Random(System.currentTimeMillis()).nextInt(0, 40)) {
                1 -> {
                    finishRefresh(false)
                    ViewState.Failure(404, "Not Found")
                }
                2 -> {
                    finishRefresh(false)
                    ViewState.Error(IllegalStateException("Internal Error"))
                }
                3 -> {
                    finishRefresh(true)
                    data.edit().clear()
                    ViewState.Empty
                }
                else -> {
                    finishRefresh(true)
                    val stIndex = Random(System.currentTimeMillis()).nextInt(0, 1000)
                    val range = stIndex..(stIndex + 500)
                    data.edit().addAll(range.toList())
                    ViewState.Idle
                }
            }
        }
    }

    override fun retry() {
        initLoad(true)
    }
}