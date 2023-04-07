package com.aaron.compose.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aaron.compose.R
import com.aaron.compose.component.StateComponent.ViewState
import com.aaron.compose.component.StateComponent.ViewState.Empty
import com.aaron.compose.component.StateComponent.ViewState.Error
import com.aaron.compose.component.StateComponent.ViewState.Failure
import com.aaron.compose.component.StateComponent.ViewState.Idle
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ktx.onClick
import com.aaron.compose.safestate.SafeState
import com.aaron.compose.safestate.safeStateOf
import com.aaron.compose.utils.DevicePreview
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 感知视图状态
 */
@Composable
fun StateComponent(
    component: StateComponent,
    modifier: Modifier = Modifier,
    loading: (@Composable () -> Unit)? = {
        CircularLoadingLayout()
    },
    failure: (@Composable (code: Int, msg: String?) -> Unit)? = { code, msg ->
        MyStateView(
            text = "请求失败",
            modifier = Modifier
                .onClick(enableRipple = false) {
                    component.retry()
                }
        )
    },
    error: (@Composable (ex: Throwable) -> Unit)? = { ex ->
        MyStateView(
            text = "请求错误",
            modifier = Modifier
                .onClick(enableRipple = false) {
                    component.retry()
                }
        )
    },
    empty: (@Composable () -> Unit)? = {
        MyStateView(text = "暂无数据")
    },
    content: @Composable () -> Unit
) {
    LoadingComponent(
        component = component,
        modifier = modifier,
        loading = loading
    ) {
        val result = component.viewState.value
        when {
            result is Failure && failure != null -> {
                failure.invoke(result.code, result.msg)
            }
            result is Error && error != null -> {
                error.invoke(result.ex)
            }
            result is Empty && empty != null -> {
                empty.invoke()
            }
            else -> content()
        }
    }
}

@DevicePreview
@Composable
private fun StateComponent() {
    StateComponent(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = Color(0xFFF0F0F0)
            ),
        component = stateComponent(viewState = Empty)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = Color.White
                )
        )
    }
}

@Composable
private fun MyStateView(text: String, modifier: Modifier = Modifier) {
    StateView(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        text = text,
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun StateView(
    text: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enableClickRipple: Boolean = true,
    backgroundColor: Color = Color.White,
    shape: Shape = RectangleShape,
    @DrawableRes iconRes: Int = R.drawable.details_image_wholea_normal,
    iconSize: Dp = 160.dp,
    betweenPadding: Dp = 24.dp,
    textColor: Color = Color(0xFF999999),
    textSize: TextUnit = 16.sp,
    textWeight: FontWeight = FontWeight.Normal,
    textStyle: TextStyle? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBackground(
                color = backgroundColor,
                shape = shape
            )
            .onClick(
                enabled = onClick != null,
                enableRipple = enableClickRipple
            ) {
                onClick?.invoke()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(betweenPadding)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                modifier = Modifier.size(iconSize),
                contentDescription = null
            )
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
 * ViewModel 可以实现此接口接管视图状态
 */
@Stable
interface StateComponent : LoadingComponent {

    val viewState: SafeState<ViewState>

    fun CoroutineScope.launchWithViewState(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        cancelable: Boolean = true,
        block: suspend CoroutineScope.() -> ViewState
    ): Job = launchWithLoading(
        context = context,
        start = start,
        cancelable = cancelable
    ) {
        val result = block()
        showState(result)
    }

    fun showState(viewState: ViewState) {
        this.viewState.setValueInternal(viewState)
    }

    fun retry()

    @Stable
    sealed class ViewState {

        object Idle : ViewState()

        data class Failure(val code: Int, val msg: String?) : ViewState()

        data class Error(val ex: Throwable) : ViewState()

        object Empty : ViewState()
    }
}

fun stateComponent(
    loading: Boolean = false,
    viewState: ViewState = Idle
): StateComponent = object : StateComponent {

    override val loading: SafeState<Boolean> = safeStateOf(loading)
    override val viewState: SafeState<ViewState> = safeStateOf(viewState)

    override fun retry() {
        error("You must implement retry function by self.")
    }
}