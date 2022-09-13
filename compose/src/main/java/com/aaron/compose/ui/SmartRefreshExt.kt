package com.aaron.compose.ui

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.aaron.compose.utils.OverScrollHandler
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerDefaults
import com.google.accompanist.pager.PagerScope
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import kotlin.math.abs

@Composable
fun SmartHorizontalPager(
    count: Int,
    modifier: Modifier = Modifier,
    offscreenPageLimit: Int = -1,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    userScrollEnabled: Boolean = true,
    content: @Composable (page: Int) -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current
    val density = LocalDensity.current
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val container = NestedViewPager2Host(context)
            val vp = ViewPager2(context).apply {
                isNestedScrollingEnabled = true
                clipToPadding = false
                clipChildren = false
                adapter = AdapterImpl(count)
            }
            container.addView(vp)
            container
        }
    ) {
        val vp = it.vp
        vp.isUserInputEnabled = userScrollEnabled
        vp.offscreenPageLimit = when {
            offscreenPageLimit < 1 -> ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
            else -> offscreenPageLimit
        }
        it.setPadding(
            with(density) { contentPadding.calculateStartPadding(layoutDirection).roundToPx() },
            with(density) { contentPadding.calculateTopPadding().roundToPx() },
            with(density) { contentPadding.calculateEndPadding(layoutDirection).roundToPx() },
            with(density) { contentPadding.calculateBottomPadding().roundToPx() }
        )
        (vp.adapter as AdapterImpl).onBind = { page ->
            content(page)
        }
    }
}

private class AdapterImpl(private val count: Int) : RecyclerView.Adapter<AdapterImpl.ViewHolder>() {

    lateinit var onBind: @Composable (page: Int) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ComposeView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(-1, -1)
        })
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setContent {
            onBind(position)
        }
    }

    override fun getItemCount(): Int {
        return count
    }

    class ViewHolder(composeView: ComposeView) : RecyclerView.ViewHolder(composeView) {

        private val composeView: ComposeView = itemView as ComposeView

        fun setContent(content: @Composable () -> Unit) {
            composeView.setContent(content)
        }
    }
}

private class NestedViewPager2Host @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    private var isUserInputEnabledCache = false

    private var lastMotionX = 0f
    private var lastMotionY = 0f

    val vp: ViewPager2
        get() = getChildAt(0) as ViewPager2

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (vp.orientation == ViewPager2.ORIENTATION_HORIZONTAL) {
            val x = ev.x
            val y = ev.y
            when (ev.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    isUserInputEnabledCache = vp.isUserInputEnabled
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = abs(lastMotionX - x)
                    val deltaY = abs(lastMotionY - y)
                    if (deltaX > touchSlop || deltaY > touchSlop) {
                        if (deltaY > deltaX) {
                            vp.isUserInputEnabled = false
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    vp.isUserInputEnabled = isUserInputEnabledCache
                }
            }
            lastMotionX = x
            lastMotionY = y
        }
        return super.onInterceptTouchEvent(ev)
    }
}