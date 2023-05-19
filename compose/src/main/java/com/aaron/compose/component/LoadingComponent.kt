package com.aaron.compose.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aaron.compose.safestate.SafeState
import com.aaron.compose.safestate.SafeStateMap
import com.aaron.compose.safestate.safeStateMapOf
import com.aaron.compose.safestate.safeStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 感知页面加载状态。
 */
@Composable
fun LoadingComponent(
    component: LoadingComponent,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    hideContentWhenLoading: Boolean = false,
    loading: (@Composable BoxScope.() -> Unit)? = {
        CircularLoadingLayout(
            modifier = Modifier.matchParentSize()
        )
    },
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        val showLoading by component.loading
        if (!hideContentWhenLoading || !showLoading) {
            content()
        }
        if (loading != null) {
            BackHandler(enabled = enabled && showLoading && component.loadingJobs.isNotEmpty()) {
                component.cancelLoading()
            }
            AnimatedVisibility(
                modifier = Modifier.matchParentSize(),
                visible = enabled && showLoading,
                enter = fadeIn(animationSpec = spring()),
                exit = fadeOut(animationSpec = spring()),
                label = "LoadingContentAnimation"
            ) {
                loading()
            }
        }
    }
}

/**
 * 圆形加载进度条。
 *
 * @param interceptPointerInput 是否拦截 Touch 事件。
 * @param color 进度条颜色。
 * @param strokeWidth 进度条粗度。
 */
@Composable
fun CircularLoadingLayout(
    modifier: Modifier = Modifier,
    interceptPointerInput: Boolean = true,
    color: Color = MaterialTheme.colors.primary,
    strokeWidth: Dp = ProgressIndicatorDefaults.StrokeWidth
) {
    Box(
        modifier = modifier.let { if (interceptPointerInput) it.pointerInput(Unit) {} else it },
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

/**
 * ViewModel 可以实现此接口接管页面加载状态。
 *
 * [launchWithLoading] 启动协程；
 *
 * [showLoading] 显示加载状态的 UI ；
 *
 * [cancelLoading] 取消加载状态。
 */
@Stable
interface LoadingComponent {

    val loading: SafeState<Boolean>

    /**
     * 用来存储通过 [launchWithLoading] 启动的协程作业，外部不建议使用
     */
    val loadingJobs: SafeStateMap<Any, Job?>

    /**
     * 启动协程
     *
     * @param cancelable 是否可被 [cancelLoading] 取消
     */
    fun CoroutineScope.launchWithLoading(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        cancelable: Boolean = true,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        val jobs = loadingJobs
        val curTime = System.currentTimeMillis()
        showLoading(true)
        return launch(
            context = context,
            start = start,
            block = block
        ).apply {
            if (cancelable) {
                jobs.editInternal()[curTime] = this
            }
            invokeOnCompletion {
                if (cancelable) {
                    jobs.editInternal().remove(curTime)
                }
                if (jobs.isEmpty()) {
                    showLoading(false)
                }
            }
        }
    }

    /**
     * 这个方法应该作为正常开启关闭加载的途径
     */
    fun showLoading(show: Boolean) {
        loading.setValueInternal(show)
    }

    /**
     * 这个方法应该作为中途需要取消加载的途径
     */
    fun cancelLoading() {
        val jobs = loadingJobs
        jobs.forEach {
            it.value?.cancel()
        }
        jobs.editInternal().clear()
        showLoading(false)
    }
}

/**
 * 用于 Compose 预览的参数占位。
 */
fun loadingComponent(
    loading: Boolean = false
): LoadingComponent = object : LoadingComponent {

    override val loading: SafeState<Boolean> = safeStateOf(loading)

    override val loadingJobs: SafeStateMap<Any, Job?> = safeStateMapOf()
}