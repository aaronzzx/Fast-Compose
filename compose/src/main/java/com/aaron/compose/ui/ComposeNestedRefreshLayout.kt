package com.aaron.compose.ui

import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.core.view.*
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import kotlin.math.abs

/**
 * @author DS-Z
 * @since 2022/9/14
 */
internal class ComposeNestedRefreshLayout(context: Context) : SmartRefreshLayout(context),
    NestedScrollingParent3, NestedScrollingChild3 {

    private val nspHelper: NestedScrollingParentHelper
        get() = mNestedParent

    private val nscHelper: NestedScrollingChildHelper
        get() = mNestedChild

    private val composeView = ComposeView(context).apply {
        id = ViewCompat.generateViewId()
    }

    private var lastMotionX = 0f
    private var lastMotionY = 0f

    init {
        setRefreshContent(composeView)
    }

    fun setContent(content: @Composable () -> Unit) {
        composeView.setContent {
            Log.d("zzx", "MySmartRefreshLayout: ${LocalView.current}")
            var p = LocalView.current.parent
            do {
                Log.d("zzx", "parent: $p")
                p = p?.parent
            } while (p != null)
            Box(
                modifier = Modifier.nestedScroll(rememberNestedScrollInteropConnection())
            ) {
                content()
            }
        }
    }

    override fun dispatchTouchEvent(e: MotionEvent): Boolean {
        val consumed = super.dispatchTouchEvent(e)
        Log.d("zzx", "dispatchTouchEvent: $consumed")
        val x = e.x
        val y = e.y
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_OUTSIDE -> {
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }
        lastMotionX = x
        lastMotionY = y
        return consumed
    }

    override fun startNestedScroll(axes: Int, type: Int): Boolean {
        return nscHelper.startNestedScroll(axes, type)
    }

    override fun stopNestedScroll(type: Int) {
        nscHelper.stopNestedScroll(type)
    }

    override fun hasNestedScrollingParent(type: Int): Boolean {
        return nscHelper.hasNestedScrollingParent(type)
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?,
        type: Int,
        consumed: IntArray
    ) {
        nscHelper.dispatchNestedScroll(
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            offsetInWindow,
            type,
            consumed
        )
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean {
        return nscHelper.dispatchNestedScroll(
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            offsetInWindow,
            type
        )
    }

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean {
        return nscHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type)
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        return (axes and ViewCompat.SCROLL_AXIS_VERTICAL) != 0
            || (axes and ViewCompat.SCROLL_AXIS_HORIZONTAL) != 0
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        nspHelper.onNestedScrollAccepted(child, target, axes, type)
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        nspHelper.onStopNestedScroll(target, type)
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
        if (startNestedScroll(nestedScrollAxes, type)) {
            dispatchNestedScroll(
                dxConsumed,
                dyConsumed,
                dxUnconsumed,
                dyUnconsumed,
                null,
                type,
                consumed
            )
        }
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        if (startNestedScroll(nestedScrollAxes, type)) {
            dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, null, type)
        }
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if (startNestedScroll(nestedScrollAxes, type)) {
            dispatchNestedPreScroll(dx, dy, consumed, null, type)
        }
    }
}