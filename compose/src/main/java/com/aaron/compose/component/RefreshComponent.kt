package com.aaron.compose.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aaron.compose.safestate.SafeState
import com.aaron.compose.safestate.safeStateOf
import com.aaron.compose.ui.refresh.SmartRefresh
import com.aaron.compose.ui.refresh.SmartRefreshState
import com.aaron.compose.ui.refresh.SmartRefreshType
import com.aaron.compose.ui.refresh.materialheader.MaterialRefreshIndicator
import com.aaron.compose.ui.refresh.rememberSmartRefreshState
import com.aaron.compose.utils.DevicePreview

/**
 * 对 [com.jialai.compose.ui.refresh.SmartRefresh] 进行封装
 */
@Composable
fun RefreshComponent(
    component: RefreshComponent,
    modifier: Modifier = Modifier,
    state: SmartRefreshState = rememberSmartRefreshState(type = component.smartRefreshType.value),
    onRefresh: (() -> Boolean)? = null,
    swipeEnabled: Boolean = true,
    clipHeaderEnabled: Boolean = true,
    translateBodyEnabled: Boolean = false,
    finishRefreshDelayMillis: Long = 0,
    triggerRatio: Float = 1f,
    maxDragRatio: Float = 2f,
    indicatorHeight: Dp = 80.dp,
    indicator: @Composable (
        smartRefreshState: SmartRefreshState,
        triggerDistance: Dp,
        maxDragDistance: Dp,
        indicatorHeight: Dp
    ) -> Unit = { smartRefreshState, triggerDistance, _, _ ->
        MaterialRefreshIndicator(
            state = smartRefreshState,
            refreshTriggerDistance = triggerDistance
        )
    },
    content: @Composable () -> Unit
) {
    SmartRefresh(
        state = state,
        onRefresh = {
            if (onRefresh?.invoke() != true) {
                // 外部不处理
                component.refresh()
            }
        },
        onIdle = {
            component.idle()
        },
        modifier = modifier,
        swipeEnabled = swipeEnabled,
        clipHeaderEnabled = clipHeaderEnabled,
        translateBodyEnabled = translateBodyEnabled,
        triggerRatio = triggerRatio,
        maxDragRatio = maxDragRatio,
        indicatorHeight = indicatorHeight,
        indicator = indicator,
        content = content
    )
}

@DevicePreview
@Composable
private fun RefreshComponent() {
    RefreshComponent(component = refreshComponent(SmartRefreshType.Refreshing)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = Color(0xFFF0F0F0)
                )
        )
    }
}

/**
 * ViewModel 可以实现此接口接管刷新
 */
@Stable
interface RefreshComponent {

    val smartRefreshType: SafeState<SmartRefreshType>

    fun refresh() {
        if (smartRefreshType.value == SmartRefreshType.Refreshing) {
            return
        }
        smartRefreshType.setValueInternal(SmartRefreshType.Refreshing)
        refreshIgnoreAnimation()
    }

    fun refreshIgnoreAnimation()

    fun finishRefresh(success: Boolean) {
        if (smartRefreshType.value != SmartRefreshType.Refreshing) {
            return
        }
        smartRefreshType.setValueInternal(
            if (success) {
                SmartRefreshType.Success
            } else {
                SmartRefreshType.Failure
            }
        )
    }

    fun idle() {
        smartRefreshType.setValueInternal(SmartRefreshType.Idle)
    }
}

@Composable
fun refreshComponent(
    type: SmartRefreshType = SmartRefreshType.Idle
): RefreshComponent = object : RefreshComponent {

    override val smartRefreshType: SafeState<SmartRefreshType> = safeStateOf(type)

    override fun refreshIgnoreAnimation() {
        error("You must implement refreshIgnoreAnimation function by self.")
    }
}