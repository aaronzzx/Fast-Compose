package com.aaron.compose.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.aaron.compose.ktx.collectAsStateWithLifecycle
import com.aaron.compose.ktx.interceptClick
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Composable
fun LoadingComponent(
    loadingable: Loadingable,
    loading: (@Composable () -> Unit)? = { CircularLoading() },
    content: @Composable () -> Unit
) {
    content()
    if (loadingable.showLoading.collectAsStateWithLifecycle().value) {
        loading?.invoke()
    }
}

@Composable
fun CircularLoading(
    interceptClick: Boolean = false,
    color: Color = MaterialTheme.colors.primary,
    strokeWidth: Dp = ProgressIndicatorDefaults.StrokeWidth
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .let { if (interceptClick) it.interceptClick() else it },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = color,
            strokeWidth = strokeWidth
        )
    }
}

@Stable
interface Loadingable {

    val showLoading: StateFlow<Boolean>

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

    fun CoroutineScope.showLoading(show: Boolean)
}

fun Loadingable(): Loadingable = LoadingableDelegate()

private class LoadingableDelegate : Loadingable {

    override val showLoading: StateFlow<Boolean> get() = _showLoading

    private val _showLoading = MutableStateFlow(false)

    override fun CoroutineScope.showLoading(show: Boolean) {
        launch {
            _showLoading.emit(show)
        }
    }
}