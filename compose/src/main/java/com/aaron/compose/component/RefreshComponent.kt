package com.aaron.compose.component

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aaron.compose.safestate.SafeState
import com.aaron.compose.safestate.safeStateOf
import com.aaron.compose.ui.refresh.SmartRefresh
import com.aaron.compose.ui.refresh.SmartRefreshState
import com.aaron.compose.ui.refresh.SmartRefreshType
import com.aaron.compose.ui.refresh.materialheader.MaterialRefreshIndicator
import com.aaron.compose.ui.refresh.rememberSmartRefreshState

/**
 * 刷新组件。
 *
 * @param state 刷新状态容器
 * @param onRefresh 触发刷新时的回调
 * @param modifier 修饰符
 * @param swipeEnabled 是否启用刷新
 * @param clipHeaderEnabled 是否启用裁剪头部
 * @param translateBodyEnabled [SmartRefreshState.indicatorOffset] 偏移时，主体部分是否也跟着偏移
 * @param triggerRatio 触发刷新的占比，基于 [indicatorHeight]
 * @param maxDragRatio 最大拖拽占比，基于 [indicatorHeight]
 * @param indicatorHeight 刷新头的高度
 * @param indicator 刷新头
 * @param content 需要使用刷新的布局
 */
@Composable
fun RefreshComponent(
    component: RefreshComponent,
    modifier: Modifier = Modifier,
    state: SmartRefreshState = rememberSmartRefreshState(type = component.smartRefreshType.value),
    onRefresh: (() -> Unit)? = null,
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
    content: @Composable BoxScope.() -> Unit
) {
    SmartRefresh(
        state = state,
        onRefresh = {
            if (onRefresh != null) {
                onRefresh()
            } else {
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
        finishRefreshDelayMillis = finishRefreshDelayMillis,
        triggerRatio = triggerRatio,
        maxDragRatio = maxDragRatio,
        indicatorHeight = indicatorHeight,
        indicator = indicator,
        content = content
    )
}

/**
 * ViewModel 可以实现此接口接管刷新，默认情况下仅实现 [refreshIgnoreAnimation] 即可，
 * 其他函数用于触发 UI 变化。
 */
@Stable
interface RefreshComponent {

    val smartRefreshType: SafeState<SmartRefreshType>

    /**
     * 自己调用刷新
     */
    fun refresh() {
        if (smartRefreshType.value == SmartRefreshType.Refreshing) {
            return
        }
        smartRefreshType.setValueInternal(SmartRefreshType.Refreshing)
        refreshIgnoreAnimation()
    }

    /**
     * 处理刷新逻辑
     */
    fun refreshIgnoreAnimation()

    /**
     * 自己决定什么时候应该结束刷新
     */
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

    /**
     * 当结束刷新时应该回到 Idle 静止状态。
     */
    fun idle() {
        smartRefreshType.setValueInternal(SmartRefreshType.Idle)
    }
}

/**
 * 用于 Compose 预览的参数占位。
 */
fun refreshComponent(
    type: SmartRefreshType = SmartRefreshType.Idle
): RefreshComponent = object : RefreshComponent {

    override val smartRefreshType: SafeState<SmartRefreshType> = safeStateOf(type)

    override fun refreshIgnoreAnimation() {
        error("You must implement refreshIgnoreAnimation function by self.")
    }
}