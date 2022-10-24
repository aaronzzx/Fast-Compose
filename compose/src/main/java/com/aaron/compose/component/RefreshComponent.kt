package com.aaron.compose.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aaron.compose.ktx.toDp
import com.aaron.compose.ui.refresh.SmartRefresh
import com.aaron.compose.ui.refresh.SmartRefreshState
import com.aaron.compose.ui.refresh.SmartRefreshType
import com.aaron.compose.ui.refresh.materialheader.MaterialRefreshIndicator

/**
 * 对 [com.aaron.compose.ui.refresh.SmartRefresh] 进行封装
 */
@Composable
fun RefreshComponent(
    refreshable: Refreshable,
    modifier: Modifier = Modifier,
    state: SmartRefreshState = rememberSmartRefreshStateForRefreshable(refreshable),
    onRefresh: (() -> Boolean)? = null,
    swipeEnabled: Boolean = true,
    clipHeaderEnabled: Boolean = false,
    translateBody: Boolean = false,
    triggerRatio: Float = 1f,
    maxDragRatio: Float = 2f,
    indicatorHeight: Dp = 80.dp,
    indicator: @Composable (
        smartRefreshState: SmartRefreshState,
        triggerPixels: Float,
        maxDragPixels: Float,
        indicatorHeight: Dp
    ) -> Unit = { smartRefreshState, triggerPixels, _, _ ->
        MaterialRefreshIndicator(
            state = smartRefreshState,
            refreshTriggerDistance = triggerPixels.toDp()
        )
    },
    content: @Composable () -> Unit
) {
    SmartRefresh(
        state = state,
        onRefresh = {
            if (onRefresh?.invoke() != true) {
                // 外部不处理
                refreshable.refresh()
            }
        },
        onIdle = {
            refreshable.idle()
        },
        modifier = modifier,
        swipeEnabled = swipeEnabled,
        clipHeaderEnabled = clipHeaderEnabled,
        translateBody = translateBody,
        triggerRatio = triggerRatio,
        maxDragRatio = maxDragRatio,
        indicatorHeight = indicatorHeight,
        indicator = indicator,
        content = content
    )
}

@Composable
fun rememberSmartRefreshStateForRefreshable(refreshable: Refreshable): SmartRefreshState {
    return rememberSaveable(saver = SmartRefreshState.Saver) {
        SmartRefreshState(refreshable.smartRefreshType.value)
    }.also {
        it.type = refreshable.smartRefreshType.value
    }
}

/**
 * ViewModel 可以实现此接口接管刷新
 */
@Stable
interface Refreshable {

    val smartRefreshType: MutableState<SmartRefreshType>

    fun refresh() {
        if (smartRefreshType.value == SmartRefreshType.Refreshing) {
            return
        }
        smartRefreshType.value = SmartRefreshType.Refreshing
        refreshIgnoreAnimation()
    }

    fun refreshIgnoreAnimation()

    fun finishRefresh(success: Boolean, delay: Long = 0) {
        if (smartRefreshType.value != SmartRefreshType.Refreshing) {
            return
        }
        smartRefreshType.value = if (success) {
            SmartRefreshType.Success(delay)
        } else {
            SmartRefreshType.Failure(delay)
        }
    }

    fun idle() {
        smartRefreshType.value = SmartRefreshType.Idle
    }
}