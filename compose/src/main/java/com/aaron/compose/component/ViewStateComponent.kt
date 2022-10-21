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
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aaron.compose.R
import com.aaron.compose.component.ViewStateable.Result
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ktx.onClick
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Composable
fun ViewStateComponent(
    viewStateable: ViewStateable,
    modifier: Modifier = Modifier,
    loading: (@Composable (viewState: ViewStateable) -> Unit)? = {
        CircularLoading()
    },
    failure: (@Composable (viewState: ViewStateable, code: Int, msg: String?) -> Unit)? = { viewState, code, msg ->
        FailureViewState {
            viewState.retry()
        }
    },
    error: (@Composable (viewState: ViewStateable, ex: Throwable) -> Unit)? = { viewState, ex ->
        ErrorViewState {
            viewState.retry()
        }
    },
    empty: (@Composable (viewState: ViewStateable) -> Unit)? = { viewState ->
        EmptyViewState()
    },
    content: @Composable () -> Unit
) {
    val showLoading by viewStateable.showLoading
    BackHandler(enabled = showLoading) {
        viewStateable.cancelLoading()
    }
    Box(modifier = modifier) {
        val result = viewStateable.result.value
        when {
            result is Result.Failure && failure != null -> {
                failure.invoke(viewStateable, result.code, result.msg)
            }
            result is Result.Error && error != null -> {
                error.invoke(viewStateable, result.ex)
            }
            result is Result.Empty && empty != null -> {
                empty.invoke(viewStateable)
            }
            else -> content()
        }
        if (loading != null) {
            Crossfade(targetState = showLoading) {
                if (it) loading(viewStateable)
            }
        }
    }
}

@Composable
fun FailureViewState(onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
            .clipToBackground(
                color = Color.White,
                shape = RoundedCornerShape(8.dp)
            )
            .onClick(enableRipple = false) {
                onRetry()
            },
        contentAlignment = Alignment.Center
    ) {
        VerticalImageText(text = "请求失败")
    }
}

@Composable
fun ErrorViewState(onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
            .clipToBackground(
                color = Color.White,
                shape = RoundedCornerShape(8.dp)
            )
            .onClick(enableRipple = false) {
                onRetry()
            },
        contentAlignment = Alignment.Center
    ) {
        VerticalImageText(text = "请求错误")
    }
}

@Preview
@Composable
fun EmptyViewState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
            .clipToBackground(
                color = Color.White,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        VerticalImageText(text = "暂无数据")
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

@Stable
interface ViewStateable : Loadingable {

    val result: State<Result>

    fun CoroutineScope.launchWithViewState(
        enableLoading: Boolean = true,
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Result
    ): Job = run {
        if (enableLoading) {
            launchWithLoading(context = context, start = start) {
                val result = block()
                showResult(result)
            }
        } else {
            launch(context = context, start = start) {
                val result = block()
                showResult(result)
            }
        }
    }

    fun showResult(result: Result)

    fun retry()

    @Stable
    sealed class Result {

        object Default : Result()

        data class Failure(val code: Int, val msg: String?) : Result()

        data class Error(val ex: Throwable) : Result()

        object Empty : Result()
    }
}

fun ViewStateable(): ViewStateable = ViewStateableDelegate()

private class ViewStateableDelegate : ViewStateable, Loadingable by Loadingable() {

    override val result: State<Result> get() = _result

    private val _result = mutableStateOf<Result>(Result.Default)

    override fun showResult(result: Result) {
        _result.value = result
    }

    override fun retry() {
        error("You must implement retry function by self.")
    }
}