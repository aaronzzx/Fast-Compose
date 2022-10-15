package com.aaron.compose.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aaron.compose.component.ViewStateable.Result
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ktx.collectAsStateWithLifecycle
import com.aaron.compose.ktx.onClick
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Composable
fun ViewStateComponent(
    viewStateable: ViewStateable,
    modifier: Modifier = Modifier,
    loading: (@Composable () -> Unit)? = {
        CircularLoading()
    },
    failure: (@Composable (viewState: ViewStateable, code: Int, msg: String?) -> Unit)? = { viewState, code, msg ->
        FailureViewState(code, msg) {
            viewState.retry()
        }
    },
    error: (@Composable (viewState: ViewStateable, ex: Throwable) -> Unit)? = { viewState, ex ->
        ErrorViewState(ex) {
            viewState.retry()
        }
    },
    empty: (@Composable (viewState: ViewStateable) -> Unit)? = { viewState ->
        EmptyViewState {
            viewState.retry()
        }
    },
    content: @Composable () -> Unit
) {
    val showLoading by viewStateable.showLoading.collectAsStateWithLifecycle()
    val result by viewStateable.result.collectAsStateWithLifecycle()
    Box(modifier = modifier) {
        val castResult = result
        when {
            castResult is Result.Failure && failure != null -> {
                failure.invoke(viewStateable, castResult.code, castResult.msg)
            }
            castResult is Result.Error && error != null -> error.invoke(viewStateable, castResult.ex)
            castResult is Result.Empty && empty != null -> empty.invoke(viewStateable)
            else -> content()
        }
        if (showLoading) {
            loading?.invoke()
        }
    }
}

@Composable
fun FailureViewState(code: Int, msg: String?, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
            .clipToBackground(
                color = Color.White,
                shape = RoundedCornerShape(8.dp)
            )
            .onClick {
                onRetry()
            },
        contentAlignment = Alignment.Center
    ) {
        VerticalImageText(imageVector = Icons.Default.ThumbUp, text = "请求失败：$code, $msg")
    }
}

@Composable
fun ErrorViewState(ex: Throwable, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
            .clipToBackground(
                color = Color.White,
                shape = RoundedCornerShape(8.dp)
            )
            .onClick {
                onRetry()
            },
        contentAlignment = Alignment.Center
    ) {
        VerticalImageText(imageVector = Icons.Default.Warning, text = "请求错误：${ex.message}")
    }
}

@Composable
fun EmptyViewState(onRetry: () -> Unit) {
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
        VerticalImageText(imageVector = Icons.Default.Done, text = "暂无数据")
    }
}

@Composable
private fun VerticalImageText(imageVector: ImageVector, text: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(imageVector = imageVector, contentDescription = null)
        Text(text = text, color = Color(0xFF666666), fontSize = 14.sp)
    }
}

@Stable
interface ViewStateable : Loadingable {

    val result: StateFlow<Result>

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

    fun CoroutineScope.showResult(result: Result)

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

    override val result: StateFlow<Result> get() = _result

    private val _result = MutableStateFlow<Result>(Result.Default)

    override fun CoroutineScope.showResult(result: Result) {
        launch {
            _result.emit(result)
        }
    }

    override fun retry() {
        error("You must implement retry function by self.")
    }
}