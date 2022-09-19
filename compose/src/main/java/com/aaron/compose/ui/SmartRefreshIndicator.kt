package com.aaron.compose.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.edit
import com.aaron.compose.drawable.ArrowDrawable
import com.aaron.compose.drawable.ProgressDrawable
import com.aaron.compose.ktx.toPx
import com.aaron.compose.ui.SmartRefreshType.Failure
import com.aaron.compose.ui.SmartRefreshType.Idle
import com.aaron.compose.ui.SmartRefreshType.Refreshing
import com.aaron.compose.ui.SmartRefreshType.Success
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import java.text.SimpleDateFormat
import java.util.*

/**
 * 刷新文案
 */
@Stable
data class SmartRefreshIndicatorText(
    /** 下拉时的文案 */
    val pullToRefreshText: String,
    /** 可以松手刷新的文案 */
    val releaseToRefreshText: String,
    /** 刷新中的文案 */
    val refreshingText: String,
    /** 刷新成功的文案 */
    val refreshSucceedText: String,
    /** 刷新失败的文案 */
    val refreshFailedText: String,
    /** 显示最后一次更新时间那个位置的文案，例如想在这个位置显示别的文案 */
    val lastRefreshTime: String? = null
)

/**
 * 默认刷新头
 */
@Composable
fun SmartRefreshIndicator(
    state: SmartRefreshState,
    triggerPx: Float,
    maxDragPx: Float,
    height: Dp,
    modifier: Modifier = Modifier,
    showLastRefreshTime: Boolean = true,
    contentColor: Color = run {
        val curContentColor = LocalContentColor.current
        if (curContentColor.alpha > 0.6f) {
            curContentColor.copy(0.6f)
        } else {
            curContentColor
        }
    },
    text: SmartRefreshIndicatorText = SmartRefreshIndicatorText(
        pullToRefreshText = "下拉可以刷新",
        releaseToRefreshText = "释放立即刷新",
        refreshingText = "正在刷新...",
        refreshSucceedText = "刷新成功",
        refreshFailedText = "刷新失败"
    )
) {
    val indicatorHeight = height.toPx()
    val offset = (maxDragPx - indicatorHeight).coerceAtMost(state.indicatorOffset - indicatorHeight)

    val releaseToRefresh = offset > triggerPx - indicatorHeight

    val refreshStatusText = when (state.type) {
        is Idle -> if (releaseToRefresh) text.releaseToRefreshText else text.pullToRefreshText
        is Refreshing -> text.refreshingText
        is Success -> text.refreshSucceedText
        is Failure -> text.refreshFailedText
    }
    val arrowRotation = remember { Animatable(0f) }
    LaunchedEffect(releaseToRefresh) {
        val animSpec = tween<Float>()
        if (releaseToRefresh) {
            arrowRotation.animateTo(180f, animationSpec = animSpec)
        } else {
            arrowRotation.animateTo(0f, animationSpec = animSpec)
        }
    }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .offset { IntOffset(0, offset.toInt()) }
            .then(modifier)
    ) {
        val (refreshText, icon) = createRefs()
        if (state.isRefreshing || state.isIdle) {
            Box(
                modifier = Modifier.constrainAs(icon) {
                    top.linkTo(refreshText.top)
                    bottom.linkTo(refreshText.bottom)
                    end.linkTo(refreshText.start)
                }
            ) {
                if (state.isRefreshing) {
                    Image(
                        painter = rememberDrawablePainter(remember {
                            ProgressDrawable().apply { setColor(contentColor.toArgb()) }
                        }),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                } else if (state.isIdle) {
                    Image(
                        painter = rememberDrawablePainter(remember {
                            ArrowDrawable().apply { setColor(contentColor.toArgb()) }
                        }),
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer {
                                rotationZ = arrowRotation.value
                            }
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .constrainAs(refreshText) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .wrapContentSize()
                .clipToBounds()
                .padding(16.dp, 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = refreshStatusText,
                color = contentColor,
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                maxLines = 1,
            )
            if (showLastRefreshTime) {
                val lastRefreshTimeString = text.lastRefreshTime ?: getLastRefreshTime(state)
                Text(
                    text = lastRefreshTimeString,
                    color = contentColor.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun getDefaultSp(): SharedPreferences {
    val context = LocalContext.current
    return remember {
        context.getSharedPreferences(SpName, Context.MODE_PRIVATE)
    }
}

@Composable
private fun SaveLastRefreshTime(time: Long) {
    val sp = getDefaultSp()
    SideEffect {
        sp.edit { putLong(LastRefreshTimeKey, time) }
    }
}

/**
 * 获取最后刷新时间，如果本地没有缓存则用当前时间，在刷新成功后会刷新本地缓存并使用当前最新时间
 */
@Composable
private fun getLastRefreshTime(state: SmartRefreshState): String {
    val sp = getDefaultSp()
    val sdf = remember {
        SimpleDateFormat(TimeFormat, Locale.getDefault())
    }
    var lastRefreshTime = when (state.type) {
        is Success -> {
            val curTime = System.currentTimeMillis()
            SaveLastRefreshTime(time = curTime)
            curTime
        }
        else -> sp.getLong(LastRefreshTimeKey, -1L)
    }
    if (lastRefreshTime == -1L) {
        lastRefreshTime = System.currentTimeMillis()
        SaveLastRefreshTime(lastRefreshTime)
    }
    return "上次更新 ${sdf.format(Date(lastRefreshTime))}"
}

private const val TimeFormat = "MM-dd HH:mm"
private const val SpName = "ComposeClassicRefreshHeader"
private const val LastRefreshTimeKey = "lastRefreshTime"