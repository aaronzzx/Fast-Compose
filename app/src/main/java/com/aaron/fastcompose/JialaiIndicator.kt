package com.aaron.fastcompose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.aaron.compose.ktx.toPx
import com.aaron.compose.ui.SmartRefreshState
import com.aaron.compose.ui.SmartRefreshType.Failure
import com.aaron.compose.ui.SmartRefreshType.Idle
import com.aaron.compose.ui.SmartRefreshType.Refreshing
import com.aaron.compose.ui.SmartRefreshType.Success
import com.google.accompanist.drawablepainter.rememberDrawablePainter

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/9/19
 */

@Composable
fun JialaiIndicator(
    refreshState: SmartRefreshState,
    triggerPixels: Float,
    maxDragPixels: Float,
    height: Dp,
    modifier: Modifier = Modifier,
    drawableColor: Color = Color(0xFFF25924),
    textColor: Color = Color(0xFF666666)
) {
    val indicatorHeight = height.toPx()
    val offset =
        (maxDragPixels - indicatorHeight).coerceAtMost(refreshState.indicatorOffset - indicatorHeight)
    val releaseToRefresh = offset > triggerPixels - indicatorHeight

    val refreshStatusText = when (refreshState.type) {
        is Idle -> if (releaseToRefresh) "松开刷新" else "下拉刷新"
        is Refreshing -> "刷新中"
        is Success -> "刷新成功"
        is Failure -> "刷新失败"
    }

    val arrowRotation = remember { Animatable(180f) }
    LaunchedEffect(releaseToRefresh) {
        val animSpec = tween<Float>()
        if (releaseToRefresh) {
            arrowRotation.animateTo(0f, animationSpec = animSpec)
        } else {
            arrowRotation.animateTo(180f, animationSpec = animSpec)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .graphicsLayer {
                translationY = offset
            }
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val context = LocalContext.current
            val arrowDrawable = remember {
                ContextCompat.getDrawable(context, R.drawable.app_icon_releaserefresh_normal)
            }
            val dynamicDrawable = remember {
                ContextCompat.getDrawable(context, R.drawable.frame_mall_home_refresh)
            }
            val logo = remember {
                ContextCompat.getDrawable(context, R.drawable.shop_icon_refresh_00010)
            }
            if (refreshState.isIdle) {
                Icon(
                    modifier = Modifier
                        .graphicsLayer {
                            rotationZ = arrowRotation.value
                        },
                    painter = rememberDrawablePainter(drawable = arrowDrawable),
                    contentDescription = null,
                    tint = drawableColor
                )
            } else {
                if (refreshState.isRefreshing) {
                    Icon(
                        modifier = Modifier,
                        painter = rememberDrawablePainter(drawable = dynamicDrawable),
                        contentDescription = null,
                        tint = drawableColor
                    )
                } else {
                    Icon(
                        modifier = Modifier,
                        painter = rememberDrawablePainter(drawable = logo),
                        contentDescription = null,
                        tint = drawableColor
                    )
                }
            }
            Text(
                text = refreshStatusText,
                fontSize = 12.sp,
                color = textColor
            )
        }
    }
}