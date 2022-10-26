package com.aaron.compose.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aaron.compose.base.SafeState
import com.aaron.compose.base.safeStateOf
import com.aaron.compose.ui.refresh.SmartRefresh
import com.aaron.compose.ui.refresh.SmartRefreshState
import com.aaron.compose.ui.refresh.SmartRefreshType
import com.aaron.compose.ui.refresh.materialheader.MaterialRefreshIndicator

/**
 * 对 [com.aaron.compose.ui.refresh.SmartRefresh] 进行封装
 */
@Composable
fun RefreshComponent(
    component: RefreshComponent,
    modifier: Modifier = Modifier,
    state: SmartRefreshState = rememberSmartRefreshStateForRefreshComponent(component),
    onRefresh: (() -> Boolean)? = null,
    swipeEnabled: Boolean = true,
    clipHeaderEnabled: Boolean = true,
    translateBody: Boolean = false,
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
        translateBody = translateBody,
        triggerRatio = triggerRatio,
        maxDragRatio = maxDragRatio,
        indicatorHeight = indicatorHeight,
        indicator = indicator,
        content = content
    )
}

@Composable
fun rememberSmartRefreshStateForRefreshComponent(component: RefreshComponent): SmartRefreshState {
    return rememberSaveable(saver = SmartRefreshState.Saver) {
        SmartRefreshState(component.smartRefreshType.value)
    }.also {
        it.type = component.smartRefreshType.value
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
        smartRefreshType.setValue(SmartRefreshType.Refreshing)
        refreshIgnoreAnimation()
    }

    fun refreshIgnoreAnimation()

    fun finishRefresh(success: Boolean, delay: Long = 0) {
        if (smartRefreshType.value != SmartRefreshType.Refreshing) {
            return
        }
        smartRefreshType.setValue(
            if (success) {
                SmartRefreshType.Success(delay)
            } else {
                SmartRefreshType.Failure(delay)
            }
        )
    }

    fun idle() {
        smartRefreshType.setValue(SmartRefreshType.Idle)
    }
}

fun refreshComponent(): RefreshComponent = object : RefreshComponent {

    override val smartRefreshType: SafeState<SmartRefreshType> = safeStateOf(SmartRefreshType.Idle)

    override fun refreshIgnoreAnimation() {
        error("You must implement refreshIgnoreAnimation function by self.")
    }
}