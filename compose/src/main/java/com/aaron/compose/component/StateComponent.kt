package com.aaron.compose.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aaron.compose.R
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ktx.onSingleClick
import com.aaron.compose.safestate.SafeState
import com.aaron.compose.safestate.SafeStateMap
import com.aaron.compose.safestate.safeStateMapOf
import com.aaron.compose.safestate.safeStateOf
import kotlinx.coroutines.Job

/**
 * 感知视图状态，显示失败、错误、空数据等页面结果。
 */
@Composable
fun StateComponent(
    component: StateComponent,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: (@Composable BoxScope.() -> Unit)? = {
        CircularLoadingLayout(
            modifier = Modifier.matchParentSize()
        )
    },
    failure: (@Composable BoxScope.(code: Int, msg: String?) -> Unit)? = { code, msg ->
        MyStateView(
            text = stringResource(R.string.compose_component_request_failure),
            modifier = Modifier
                .onSingleClick(enableRipple = false) {
                    component.retry()
                }
        )
    },
    error: (@Composable BoxScope.(ex: Throwable) -> Unit)? = { ex ->
        MyStateView(
            text = stringResource(R.string.compose_component_request_error),
            modifier = Modifier
                .onSingleClick(enableRipple = false) {
                    component.retry()
                }
        )
    },
    empty: (@Composable BoxScope.() -> Unit)? = {
        MyStateView(text = stringResource(R.string.compose_component_empty_data))
    },
    content: @Composable BoxScope.() -> Unit
) {
    LoadingComponent(
        component = component,
        modifier = modifier,
        enabled = enabled,
        loading = loading
    ) {
        val result = component.viewState.value
        when {
            enabled && result is ViewState.Failure && failure != null -> {
                failure(result.code, result.msg)
            }
            enabled && result is ViewState.Error && error != null -> {
                error(result.ex)
            }
            enabled && result is ViewState.Empty && empty != null -> {
                empty()
            }
            else -> content()
        }
    }
}

@Composable
private fun BoxScope.MyStateView(text: String, modifier: Modifier = Modifier) {
    StateView(
        modifier = modifier
            .matchParentSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        text = text,
        shape = RoundedCornerShape(8.dp)
    )
}

/**
 * 设置显示视图状态。
 *
 * @param enableClickRipple 是否启用点击水波纹效果。
 * @param shape 设置形状。
 * @param betweenPadding 图标与文字之间的间距。
 */
@Composable
fun StateView(
    text: String,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    contentOffset: DpOffset = DpOffset.Zero,
    onClick: (() -> Unit)? = null,
    enableClickRipple: Boolean = true,
    backgroundColor: Color = Color.White,
    shape: Shape = RectangleShape,
    @DrawableRes iconRes: Int? = null,
    iconSize: Dp = 160.dp,
    betweenPadding: Dp = 24.dp,
    textColor: Color = Color(0xFF999999),
    textSize: TextUnit = 16.sp,
    textWeight: FontWeight = FontWeight.Normal,
    textStyle: TextStyle? = null
) {
    Box(
        modifier = modifier
            .clipToBackground(
                color = backgroundColor,
                shape = shape
            )
            .onSingleClick(
                enabled = onClick != null,
                enableRipple = enableClickRipple
            ) {
                onClick?.invoke()
            }
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.offset(x = contentOffset.x, y = contentOffset.y),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(betweenPadding)
        ) {
            if (iconRes != null) {
                Image(
                    painter = painterResource(id = iconRes),
                    modifier = Modifier.size(iconSize),
                    contentDescription = null
                )
            }
            Text(
                text = text,
                color = textColor,
                fontSize = textSize,
                fontWeight = textWeight,
                style = textStyle ?: LocalTextStyle.current
            )
        }
    }
}

/**
 * ViewModel 可以实现此接口接管视图状态，此接口包含显示加载状态的实现。
 */
@Stable
interface StateComponent : LoadingComponent {

    val viewState: SafeState<ViewState>

    /**
     * 切换视图状态
     */
    fun showState(viewState: ViewState) {
        this.viewState.setValueInternal(viewState)
    }

    /**
     * 处理重试逻辑
     */
    fun retry()
}

/**
 * 视图状态
 */
@Stable
sealed class ViewState {

    /**
     * 页面常规状态
     */
    object Idle : ViewState()

    /**
     * 页面请求失败
     */
    data class Failure(val code: Int, val msg: String?) : ViewState()

    /**
     * 页面请求错误
     */
    data class Error(val ex: Throwable) : ViewState()

    /**
     * 页面无数据
     */
    object Empty : ViewState()
}

/**
 * 用于 Compose 预览的参数占位。
 */
fun stateComponent(
    loading: Boolean = false,
    viewState: ViewState = ViewState.Idle
): StateComponent = object : StateComponent {

    override val loading: SafeState<Boolean> = safeStateOf(loading)
    override val loadingJobs: SafeStateMap<Any, Job?> = safeStateMapOf()
    override val viewState: SafeState<ViewState> = safeStateOf(viewState)

    override fun retry() {
        error("You must implement retry function by self.")
    }
}