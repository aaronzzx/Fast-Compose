package com.aaron.compose.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aaron.compose.base.SafeState
import com.aaron.compose.base.safeStateOf
import com.aaron.compose.ktx.interceptClick
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 感知 loading
 */
@Composable
fun LoadingComponent(
    component: LoadingComponent,
    loading: (@Composable (LoadingComponent) -> Unit)? = {
        CircularLoading()
    },
    content: @Composable () -> Unit
) {
    content()
    if (loading != null) {
        val showLoading by component.loading
        BackHandler(enabled = showLoading) {
            component.cancelLoading()
        }
        Crossfade(targetState = showLoading) {
            if (it) loading(component)
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
        Surface(
            shape = CircleShape,
            color = Color.White,
            elevation = 6.dp
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

private const val JOB_KEY = "working-job"

/**
 * ViewModel 可以实现此接口接管 loading 状态，使用 [loadingComponent] 委托一步到位。
 */
@Stable
interface LoadingComponent {

    val loading: SafeState<Boolean>

    fun CoroutineScope.launchWithLoading(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        val loading = loading
        loading.get<Job>(JOB_KEY)?.cancel()
        showLoading(true)
        val job = launch(
            context = context,
            start = start,
            block = block
        )
        loading[JOB_KEY] = job
        job.invokeOnCompletion {
            showLoading(false)
            loading.fastRemove(JOB_KEY)
        }
        return job
    }

    /**
     * 这个方法应该作为正常开启关闭加载的途径
     */
    fun showLoading(show: Boolean) {
        loading.setValue(show)
    }

    /**
     * 这个方法应该作为中途需要取消加载的途径
     */
    fun cancelLoading() {
        loading.remove<Job>(JOB_KEY)?.cancel()
        showLoading(false)
    }
}

fun loadingComponent(): LoadingComponent = object : LoadingComponent {

    override val loading: SafeState<Boolean> = safeStateOf(false)
}