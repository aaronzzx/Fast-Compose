package com.aaron.fastcompose.ui.paging

import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import com.aaron.compose.base.BaseRoute
import com.aaron.compose.base.navTo
import com.aaron.compose.component.LazyPagerPagingComponent
import com.aaron.compose.component.PagingStaggeredGridComponent
import com.aaron.compose.component.RefreshComponent
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ktx.lazylist.itemsIndexed
import com.aaron.compose.ktx.onClick
import com.aaron.compose.ui.BottomSheet
import com.aaron.compose.ui.Dialog
import com.aaron.compose.ui.Notification
import com.aaron.compose.ui.TopBar
import com.aaron.compose.ui.VisibilityContainer
import com.aaron.compose.ui.VisibilityContainerDefaults
import com.aaron.compose.ui.VisibilityContainerProperties
import com.aaron.compose.ui.VisibilityContainerState
import com.aaron.compose.ui.VisibilityScrimContainer
import com.aaron.compose.ui.rememberVisibilityContainerState
import com.aaron.compose.utils.OverScrollHandler
import com.aaron.fastcompose.R
import com.blankj.utilcode.util.ToastUtils
import com.google.accompanist.navigation.animation.composable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/12/28
 */

object PagingScreen : BaseRoute {

    override val route: String = "paging"

    fun navigate(navController: NavController, navOptions: NavOptions? = null) {
        navController.navTo(
            route = PagingScreen,
            navOptions = navOptions
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.pagingScreen(navController: NavController) {
    composable(route = PagingScreen.route) {
        PagingScreen(
            onBack = {
                navController.popBackStack()
            },
            vm = hiltViewModel()
        )
    }
}

@Composable
private fun PagingScreen(
    onBack: () -> Unit,
    vm: PagingVM = viewModel()
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colors.background
            )
            .background(
                brush = when (isSystemInDarkTheme()) {
                    true -> Brush.verticalGradient(
                        0f to Color(0xB34684B4),
                        0.5f to MaterialTheme.colors.background
                    )
                    else -> Brush.verticalGradient(
                        0f to Color(0xB338A6FD),
                        0.5f to MaterialTheme.colors.background
                    )
                }
            )
    ) {
        val visibilityContainerState = rememberVisibilityContainerState()
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                modifier = Modifier.zIndex(1f),
                title = "",
                startIcon = R.drawable.back,
                backgroundColor = Color.Transparent,
                contentColor = contentColorFor(backgroundColor = MaterialTheme.colors.background),
                elevation = 0.dp,
                contentPadding = WindowInsets.statusBars.asPaddingValues(),
                onStartIconClick = {
                    onBack()
                },
                endLayout = {
                    IconButton(
                        onClick = {
                            visibilityContainerState.show()
                        }
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = LocalContentColor.current
                        )
                    }
                }
            )

            LazyPagingContent(
                lazyPagerPagingComponent = vm.getLazyPagerPagingComponent()
            )
        }

        MyVisibilityContainer(
            visibilityContainerState = visibilityContainerState
        )
    }
}

@Composable
private fun MyVisibilityContainer(
    visibilityContainerState: VisibilityContainerState
) {
//    MyDialog(visibilityContainerState)
//    MyBottomSheet(visibilityContainerState)
    MyNotification(visibilityContainerState)
}

@Composable
private fun MyDialog(
    visibilityContainerState: VisibilityContainerState
) {
    VisibilityScrimContainer(
        state = visibilityContainerState
    ) {
        Dialog {
            DialogContent(
                onClick = {
                    visibilityContainerState.hide()
                }
            )
        }
    }
}

@Composable
private fun MyBottomSheet(
    visibilityContainerState: VisibilityContainerState
) {
    VisibilityScrimContainer(
        state = visibilityContainerState,
        properties = VisibilityContainerProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        BottomSheet(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp
            )
        ) {
            BottomSheetContent(
                onCloseClick = {
                    visibilityContainerState.hide()
                }
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun MyNotification(
    visibilityContainerState: VisibilityContainerState
) {
    LaunchedEffect(key1 = visibilityContainerState.visible) {
        if (visibilityContainerState.visible) {
            flow {
                (3 downTo 1).forEach {
                    delay(1000)
                    emit(it)
                }
            }
                .onStart {
                    ToastUtils.showShort("通知倒计时开始")
                }
                .onCompletion {
                    delay(1000)
                    visibilityContainerState.hide()
                }
                .collect {
                    ToastUtils.showShort("$it")
                }
        }
    }

    var notificationOffset by remember {
        mutableStateOf(0)
    }
    VisibilityContainer(
        state = visibilityContainerState,
        scrim = {
            val density = LocalDensity.current
            Box(
                modifier = Modifier
                    .animateEnterExit(
                        enter = VisibilityContainerDefaults.ScrimEnterTransition,
                        exit = VisibilityContainerDefaults.ScrimExitTransition
                    )
                    .fillMaxSize()
                    .graphicsLayer {
                        val fraction = notificationOffset.toFloat() / with(density) { 80.dp.toPx() }
                        alpha = 1f - fraction.coerceIn(0f, 1f)
                    }
                    .background(
                        brush = Brush.verticalGradient(
                            0f to Color(0xE6E57373),
                            0.5f to Color(0xE664B5F6)
                        )
                    )
                    .onClick(enableRipple = false) {
                        visibilityContainerState.hide()
                    }
            )
        }
    ) {
        Notification(
            onSwipeToDismiss = {
                visibilityContainerState.hide()
            },
            onOffsetChange = {
                notificationOffset = it
            }
        ) {
            NotificationContent(
                onClick = {
                    visibilityContainerState.hide()
                }
            )
        }
    }
}

@Composable
private fun DialogContent(onClick: () -> Unit) {
    Image(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.5f)
            .onClick {
                onClick()
            },
        painter = painterResource(id = R.drawable.ide_bg),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        alignment = Alignment.TopCenter
    )
}

@Composable
private fun BottomSheetContent(
    onCloseClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f),
                text = "标题",
                style = MaterialTheme.typography.h5
            )
            IconButton(
                modifier = Modifier.padding(end = 4.dp),
                onClick = onCloseClick
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.Black
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .verticalScroll(rememberScrollState())
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(10) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f)
                        .clipToBackground(
                            color = Color.Red.copy(0.5f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$it",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationContent(onClick: () -> Unit) {
    Image(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f)
            .onClick {
                onClick()
            },
        painter = painterResource(id = R.drawable.ide_bg),
        contentDescription = null,
        contentScale = ContentScale.FillWidth,
        alignment = Alignment.TopCenter
    )
}

@Composable
private fun LazyPagingContent(lazyPagerPagingComponent: LazyPagerPagingComponent<String, Any?, Repo>) {
    LazyPagerPagingComponent(component = lazyPagerPagingComponent) { lazyPagingComponent ->
        RefreshContent(
            refreshComponent = lazyPagingComponent,
            refreshEnabled = true
        ) {
            OverScrollHandler(enabled = false) {
                val listState = rememberLazyStaggeredGridState()
                LaunchedEffect(key1 = listState) {
                    snapshotFlow { listState.isScrollInProgress }
                        .filter { it }
                        .collect {
                            Log.d("zzx", "start: ${!listState.canScrollBackward}, end: ${!listState.canScrollForward}")
                        }
                }
                PagingStaggeredGridComponent(
                    component = lazyPagingComponent,
                    state = listState,
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalItemSpacing = 8.dp,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(lazyPagingComponent, key = { _, item -> item.id }) { index, item ->
                        Card(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .onClick {
                                    lazyPagingComponent.pagingRefresh()
                                }
                                .fillMaxWidth()
                                .let {
                                    if (index % 3 == 0) it.aspectRatio(1f) else {
                                        it.aspectRatio(1.5f)
                                    }
                                },
                            shape = RoundedCornerShape(8.dp),
                            backgroundColor = when (isSystemInDarkTheme()) {
                                true -> MaterialTheme.colors.surface
                                else -> Color.White
                            }
                        ) {
                            Box(
                                modifier = Modifier.padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.name,
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