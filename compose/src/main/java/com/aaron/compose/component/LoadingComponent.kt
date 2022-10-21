package com.aaron.compose.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aaron.compose.ktx.interceptClick
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Composable
fun LoadingComponent(
    loadingable: Loadingable,
    loading: (@Composable (loadingable: Loadingable) -> Unit)? = {
        CircularLoading()
    },
    content: @Composable () -> Unit
) {
    val showLoading by loadingable.showLoading
    BackHandler(enabled = showLoading) {
        loadingable.cancelLoading()
    }
    content()
    if (loading != null) {
        Crossfade(targetState = showLoading) {
            if (it) loading(loadingable)
        }
    }
}

@Composable
fun CircularLoading(
    interceptClick: Boolean = true,
    color: Color = MaterialTheme.colors.primary,
    strokeWidth: Dp = ProgressIndicatorDefaults.StrokeWidth
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .let { if (interceptClick) it.interceptClick() else it },
        contentAlignment = Alignment.Center
    ) {
        Card(
            backgroundColor = Color.White,
            shape = CircleShape,
            elevation = 4.dp
        ) {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = color,
                    strokeWidth = strokeWidth
                )
            }
        }
    }
}

@Stable
interface Loadingable {

    val showLoading: State<Boolean>

    fun CoroutineScope.launchWithLoading(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        showLoading(true)
        val job = launch(
            context = context,
            start = start,
            block = block
        )
        job.invokeOnCompletion {
            showLoading(false)
        }
        return job
    }

    /**
     * 这个方法应该作为正常开启关闭加载的途径
     */
    fun showLoading(show: Boolean)

    /**
     * 这个方法应该作为中途需要取消加载的途径，至于具体的取消逻辑交由实现类处理
     */
    fun cancelLoading()
}

fun Loadingable(): Loadingable = LoadingableDelegate()

private class LoadingableDelegate : Loadingable {

    override val showLoading: State<Boolean> get() = _showLoading

    private val _showLoading = mutableStateOf(false)

    override fun showLoading(show: Boolean) {
        _showLoading.value = show
    }

    override fun cancelLoading() {
        showLoading(false)
    }
}