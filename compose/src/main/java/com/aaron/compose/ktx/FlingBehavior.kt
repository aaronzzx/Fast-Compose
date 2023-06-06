package com.aaron.compose.ktx

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.MotionDurationScale
import kotlinx.coroutines.withContext
import kotlin.math.abs

/**
 * 用于指定惯性滑动行为
 *
 * @param motionDurationScale 为动作(如动画)提供持续时间刻度。当 duration scaleFactor 为 0 时，运动将在下一帧回调中结束。
 * 否则 duration scaleFactor 将被用作缩放运动持续时间的乘数。尺度越大，动作完成的时间就越长，因此被感知的速度也就越慢。
 */
@Composable
fun rememberFlingBehavior(motionDurationScale: Float = 1f): FlingBehavior {
    val flingSpec = rememberSplineBasedDecay<Float>()
    return remember(flingSpec) {
        DefaultFlingBehavior(
            flingDecay = flingSpec,
            motionDurationScale = object : MotionDurationScale {
                override val scaleFactor: Float
                    get() = motionDurationScale
            }
        )
    }
}

private class DefaultFlingBehavior(
    private val flingDecay: DecayAnimationSpec<Float>,
    private val motionDurationScale: MotionDurationScale
) : FlingBehavior {

    // For Testing
    var lastAnimationCycleCount = 0

    override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
        lastAnimationCycleCount = 0
        // come up with the better threshold, but we need it since spline curve gives us NaNs
        return withContext(motionDurationScale) {
            if (abs(initialVelocity) > 1f) {
                var velocityLeft = initialVelocity
                var lastValue = 0f
                AnimationState(
                    initialValue = 0f,
                    initialVelocity = initialVelocity,
                ).animateDecay(flingDecay) {
                    val delta = value - lastValue
                    val consumed = scrollBy(delta)
                    lastValue = value
                    velocityLeft = this.velocity
                    // avoid rounding errors and stop if anything is unconsumed
                    if (abs(delta - consumed) > 0.5f) this.cancelAnimation()
                    lastAnimationCycleCount++
                }
                velocityLeft
            } else {
                initialVelocity
            }
        }
    }
}