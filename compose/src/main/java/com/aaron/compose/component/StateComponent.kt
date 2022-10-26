package com.aaron.compose.component

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aaron.compose.R
import com.aaron.compose.base.SafeState
import com.aaron.compose.base.safeStateOf
import com.aaron.compose.component.StateComponent.ViewState.Empty
import com.aaron.compose.component.StateComponent.ViewState.Error
import com.aaron.compose.component.StateComponent.ViewState.Failure
import com.aaron.compose.component.StateComponent.ViewState.Idle
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ktx.isEmpty
import com.aaron.compose.ktx.onClick
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Composable
fun <K, V> PagingStateComponent(
    pagingComponent: PagingComponent<K, V>,
    stateComponent: StateComponent,
    empty: @Composable () -> Unit = {
        ViewStateLayout(text = "暂无数据")
    },
    content: @Composable () -> Unit
) {
    val failureErrorContent: (@Composable () -> Unit) = remember {
        {
            val pageData = pagingComponent.pageData
            if (pageData.isEmpty) {
                empty()
            } else {
                content()
            }
        }
    }
    StateComponent(
        component = stateComponent,
        failure = { component, code, msg ->
            failureErrorContent()
        },
        error = { component, ex ->
            failureErrorContent()
        },
        empty = {
            empty()
        },
        content = content
    )
}

/**
 * 感知视图状态
 */
@Composable
fun StateComponent(
    component: StateComponent,
    modifier: Modifier = Modifier,
    loading: (@Composable (StateComponent) -> Unit)? = {
        CircularLoading()
    },
    failure: (@Composable (
        stateComponent: StateComponent,
        code: Int,
        msg: String?
    ) -> Unit)? = { stateComponent, code, msg ->
        ViewStateLayout(
            text = "请求失败",
            modifier = Modifier
                .onClick(enableRipple = false) {
                    stateComponent.retry()
                }
        )
    },
    error: (@Composable (
        stateComponent: StateComponent,
        ex: Throwable
    ) -> Unit)? = { stateComponent, ex ->
        ViewStateLayout(
            text = "请求错误",
            modifier = Modifier
                .onClick(enableRipple = false) {
                    stateComponent.retry()
                }
        )
    },
    empty: (@Composable (StateComponent) -> Unit)? = {
        ViewStateLayout(text = "暂无数据")
    },
    content: @Composable () -> Unit
) {
    val showLoading by component.loading
    BackHandler(enabled = showLoading) {
        component.cancelLoading()
    }
    Box(modifier = modifier) {
        val result = component.viewState.value
        when {
            result is Failure && failure != null -> {
                failure.invoke(component, result.code, result.msg)
            }
            result is Error && error != null -> {
                error.invoke(component, result.ex)
            }
            result is Empty && empty != null -> {
                empty.invoke(component)
            }
            else -> content()
        }
        if (loading != null) {
            Crossfade(targetState = showLoading) {
                if (it) loading(component)
            }
        }
    }
}

@Composable
fun ViewStateLayout(
    text: String,
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int = R.drawable.details_image_wholea_normal,
    iconSize: Dp = 160.dp,
    spacing: Dp = 24.dp,
    textColor: Color = Color(0xFF999999),
    textSize: TextUnit = 16.sp
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
            .clipToBackground(
                color = Color.White,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        VerticalImageText(
            text = text,
            iconRes = iconRes,
            iconSize = iconSize,
            spacing = spacing,
            textColor = textColor,
            textSize = textSize
        )
    }
}

@Composable
private fun VerticalImageText(
    text: String,
    @DrawableRes iconRes: Int = R.drawable.details_image_wholea_normal,
    iconSize: Dp = 160.dp,
    spacing: Dp = 24.dp,
    textColor: Color = Color(0xFF999999),
    textSize: TextUnit = 16.sp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            modifier = Modifier.size(iconSize),
            contentDescription = null
        )
        Text(text = text, color = textColor, fontSize = textSize)
    }
}

/**
 * ViewModel 可以实现此接口接管视图状态，使用 [stateComponent] 委托一步到位。
 */
@Stable
interface StateComponent : LoadingComponent {

    val viewState: SafeState<ViewState>

    fun CoroutineScope.launchWithViewState(
        enableLoading: Boolean = true,
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> ViewState
    ): Job = run {
        if (enableLoading) {
            launchWithLoading(context = context, start = start) {
                val result = block()
                showState(result)
            }
        } else {
            launch(context = context, start = start) {
                val result = block()
                showState(result)
            }
        }
    }

    fun showState(viewState: ViewState) {
        this.viewState.setValue(viewState)
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

fun stateComponent(): StateComponent = object : StateComponent {

    override val loading: SafeState<Boolean> = safeStateOf(false)
    override val viewState: SafeState<StateComponent.ViewState> = safeStateOf(Idle)

    override fun retry() {
        error("You must implement retry function by self.")
    }
}