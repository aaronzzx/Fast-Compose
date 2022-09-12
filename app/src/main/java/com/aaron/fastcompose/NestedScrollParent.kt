package com.aaron.fastcompose

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/9/11
 */
class NestedScrollParent @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs), NestedScrollingParent3 {

    private val scrollOffset by lazy { IntArray(2) }

    private val nspHelper = NestedScrollingParentHelper(this)

    private var headerHeight = 0

    init {
        orientation = VERTICAL
        setWillNotDraw(false)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        headerHeight = getChildAt(0).measuredHeight
        getChildAt(1).updateLayoutParams {
            height = measuredHeight
        }
    }

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        scrollTo(0, scrollY)
    }

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        return onStartNestedScroll(child, target, nestedScrollAxes, ViewCompat.TYPE_TOUCH)
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int) {
        onNestedScrollAccepted(child, target, axes, ViewCompat.TYPE_TOUCH)
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        nspHelper.onNestedScrollAccepted(child, target, axes, type)
    }

    override fun onStopNestedScroll(child: View) {
        onStopNestedScroll(child, ViewCompat.TYPE_TOUCH)
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        nspHelper.onStopNestedScroll(target, type)
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int
    ) {
        onNestedScroll(
            target,
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            ViewCompat.TYPE_TOUCH
        )
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        scrollOffset.fill(0)
        onNestedScroll(
            target,
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            type,
            scrollOffset
        )
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        val curScrollY = scrollY
        if (dyUnconsumed < 0 && curScrollY != 0) {
            overScrollBy(
                0,
                dyUnconsumed,
                scrollX,
                scrollY,
                0,
                headerHeight,
                0,
                0,
                type == ViewCompat.TYPE_TOUCH
            )
            val newScrollY = scrollY
            val consumedY = newScrollY - curScrollY
            consumed[1] = consumedY
        }
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        onNestedPreScroll(target, dx, dy, consumed, ViewCompat.TYPE_TOUCH)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        val curScrollY = scrollY
        if (dy > 0 && curScrollY != headerHeight) {
            overScrollBy(
                0,
                dy,
                scrollX,
                scrollY,
                0,
                headerHeight,
                0,
                0,
                type == ViewCompat.TYPE_TOUCH
            )
            val newScrollY = scrollY
            val consumedY = newScrollY - curScrollY
            consumed[1] = consumedY
        }
    }
}