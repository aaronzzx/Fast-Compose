package com.aaron.compose.ui

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import coil.compose.AsyncImage
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ktx.onClick
import com.google.accompanist.pager.HorizontalPagerIndicator
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter

/**
 * 无限轮播 Banner
 *
 * @param urlList 链接
 * @param modifier 修饰符
 * @param carouselTime 轮播间隔，单位毫秒
 * @param whRatio 宽高比
 * @param placeholder 等待占位图
 * @param error 错误占位图
 * @param animationSpec 动画规格，用来调整动画
 * @param indicator 指示器
 * @param onClick 点击
 */
@Composable
fun Banner(
    urlList: ImmutableList<String?>,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    shape: Shape = RoundedCornerShape(8.dp),
    carouselTime: Long = 2000,
    whRatio: Float = 3f / 1f,
    placeholder: Painter? = null,
    error: Painter? = null,
    animationSpec: AnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow),
    indicator: @Composable BoxScope.(
        pagerState: PagerState,
        startIndex: Int,
        actualPageCount: Int
    ) -> Unit = { pagerState, startIndex, actualPageCount ->
        BannerIndicator(
            pagerState = pagerState,
            startIndex = startIndex,
            actualPageCount = actualPageCount
        )
    },
    onClick: ((index: Int) -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(ratio = whRatio)
            .clipToBackground(
                color = backgroundColor,
                shape = shape
            )
    ) {
        // Display items
        val maxPage = Int.MAX_VALUE
        val actualPageCount = urlList.size
        val virtualPageCount = when (actualPageCount) {
            1 -> 1
            else -> maxPage
        }
        // We start the pager in the middle of the raw number of pages
        val startIndex = when (actualPageCount) {
            1 -> 1
            else -> maxPage / 2
        }
        val pagerState = rememberPagerState(initialPage = startIndex) { virtualPageCount }

        var bannerWidth by rememberSaveable {
            mutableStateOf(0)
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    bannerWidth = it.size.width
                }
        ) { index ->
            // We calculate the page from the given index
            val page = floorMod(index - startIndex, actualPageCount)
            AsyncImage(
                model = urlList.getOrNull(page),
                placeholder = placeholder,
                error = error,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .onClick {
                        onClick?.invoke(page)
                    }
            )
        }
        if (actualPageCount > 1) {
            indicator(pagerState, startIndex, actualPageCount)

            // 轮播
            val dragged by pagerState.interactionSource.collectIsDraggedAsState()
            val curCarouselTime by rememberUpdatedState(newValue = carouselTime)
            val lifecycleOwner = LocalLifecycleOwner.current
            LaunchedEffect(key1 = pagerState) {
                lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    // 由于 animateScroll* 是挂起函数，有可能滚动到一半就被取消了
                    // 这时候就会悬在滚动中间，因此重新启动副作用时需要让他归位
                    pagerState.animateScrollToPage(pagerState.currentPage)
                    snapshotFlow { pagerState.currentPageOffsetFraction }
                        .filter { !dragged }
                        .debounce(curCarouselTime)
                        .collectLatest {
                            if (pagerState.currentPage == maxPage) {
                                pagerState.animateScrollToPage(startIndex)
                            } else {
                                // 1. 这种无法更改滚动速度
                                //pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                // 2. 这种需要自己传滚动距离，但可以指定动画规格
                                pagerState.animateScrollBy(bannerWidth.toFloat(), animationSpec)
                            }
                        }
                }
            }
        }
    }
}

/**
 * 通用的 Banner 指示器
 */
@Composable
fun BoxScope.BannerIndicator(
    pagerState: PagerState,
    startIndex: Int,
    actualPageCount: Int
) {
    HorizontalPagerIndicator(
        pagerState = pagerState,
        pageCount = actualPageCount,
        pageIndexMapping = { index ->
            floorMod(index - startIndex, actualPageCount)
        },
        activeColor = Color.White,
        inactiveColor = Color.White.copy(0.5f),
        indicatorWidth = 6.dp,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(8.dp),
    )
}

/**
 * 映射索引，因为无限轮播是通过设置 Int.MAX_VALUE 来完成的，索引需要转换
 */
fun floorMod(value: Int, other: Int): Int = when (other) {
    0 -> value
    else -> value - value.floorDiv(other) * other
}