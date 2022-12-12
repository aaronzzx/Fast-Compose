package com.aaron.compose.ktx.lazylist

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.aaron.compose.ktx.clipToBackground

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/12/7
 */

inline fun <T> LazyListScope.sections(
    sections: List<List<T>>,
    orientation: Orientation,
    sectionBetweenPadding: Dp = 0.dp,
    itemBesidePadding: Dp = 0.dp,
    sectionBackgroundColor: Color = Color.Transparent,
    sectionShape: CornerBasedShape = RoundedCornerShape(0.dp),
    crossinline key: LazySectionScope.(item: T) -> Any? = { null },
    crossinline contentType: LazySectionScope.(item: T) -> Any? = { null },
    noinline header: (@Composable LazySectionItemScope.() -> Unit)? = null,
    noinline footer: (@Composable LazySectionItemScope.() -> Unit)? = null,
    crossinline itemContent: @Composable LazySectionItemScope.(item: T) -> Unit
) {
    sectionsIndexed(
        sections = sections,
        orientation = orientation,
        sectionBetweenPadding = sectionBetweenPadding,
        itemBesidePadding = itemBesidePadding,
        sectionBackgroundColor = sectionBackgroundColor,
        sectionShape = sectionShape,
        key = { _, item -> key(item) },
        contentType = { _, item -> contentType(item) },
        header = header,
        footer = footer,
    ) { _, item ->
        itemContent(item)
    }
}

inline fun <T> LazyListScope.sectionsIndexed(
    sections: List<List<T>>,
    orientation: Orientation,
    sectionBetweenPadding: Dp = 0.dp,
    itemBesidePadding: Dp = 0.dp,
    sectionBackgroundColor: Color = Color.Transparent,
    sectionShape: CornerBasedShape = RoundedCornerShape(0.dp),
    crossinline key: LazySectionScope.(index: Int, item: T) -> Any? = { _, _ -> null },
    crossinline contentType: LazySectionScope.(index: Int, item: T) -> Any? = { _, _ -> null },
    noinline header: (@Composable LazySectionItemScope.() -> Unit)? = null,
    noinline footer: (@Composable LazySectionItemScope.() -> Unit)? = null,
    crossinline itemContent: @Composable LazySectionItemScope.(index: Int, item: T) -> Unit
) {
    sections.forEachIndexed { sectionIndex, section ->
        if (header != null) {
            val sectionItemScope = LazySectionItemScopeImpl(sectionIndex)
            item(
                key = "Header-$sectionIndex",
                contentType = "Header"
            ) {
                sectionItemScope.delegate = this
                with(sectionItemScope) {
                    SectionHeader(
                        modifier = when (orientation) {
                            Orientation.Vertical -> Modifier.fillParentMaxWidth()
                            Orientation.Horizontal -> Modifier.fillParentMaxHeight()
                        },
                        sectionIndex = sectionIndex,
                        sectionBetweenPadding = sectionBetweenPadding,
                        orientation = orientation
                    ) {
                        header()
                    }
                }
            }
        }

        run {
            val sectionItemScope = LazySectionItemScopeImpl(sectionIndex)
            itemsIndexed(
                items = section,
                key = { index, item ->
                    with(sectionItemScope) {
                        key(index, item) ?: getSectionParcelableKey(sections, sectionIndex, index)
                    }
                },
                contentType = { index, item ->
                    with(sectionItemScope) {
                        contentType(index, item)
                    }
                }
            ) { index, item ->
                sectionItemScope.delegate = this
                with(sectionItemScope) {
                    SectionItem(
                        modifier = when (orientation) {
                            Orientation.Vertical -> Modifier.fillParentMaxWidth()
                            Orientation.Horizontal -> Modifier.fillParentMaxHeight()
                        },
                        sectionIndex = sectionIndex,
                        itemIndex = index,
                        lastItemIndex = section.lastIndex,
                        sectionBetweenPadding = sectionBetweenPadding,
                        itemBesidePadding = itemBesidePadding,
                        sectionBackgroundColor = sectionBackgroundColor,
                        sectionShape = sectionShape,
                        orientation = orientation,
                        existsHeader = header != null
                    ) {
                        itemContent(index, item)
                    }
                }
            }
        }

        if (footer != null) {
            val sectionItemScope = LazySectionItemScopeImpl(sectionIndex)
            item(
                key = "Footer-$sectionIndex",
                contentType = "Footer"
            ) {
                sectionItemScope.delegate = this
                with(sectionItemScope) {
                    SectionFooter(
                        modifier = when (orientation) {
                            Orientation.Vertical -> Modifier.fillParentMaxWidth()
                            Orientation.Horizontal -> Modifier.fillParentMaxHeight()
                        },
                        orientation = orientation
                    ) {
                        footer()
                    }
                }
            }
        }
    }
}

@Stable
sealed interface LazySectionScope {

    val sectionIndex: Int
}

@Stable
sealed interface LazySectionItemScope : LazySectionScope, LazyItemScope {

    override val sectionIndex: Int
}

@PublishedApi
internal class LazySectionItemScopeImpl(override val sectionIndex: Int) : LazySectionItemScope,
    LazyItemScope {

    @PublishedApi
    internal lateinit var delegate: LazyItemScope

    @ExperimentalFoundationApi
    override fun Modifier.animateItemPlacement(animationSpec: FiniteAnimationSpec<IntOffset>): Modifier {
        return with(delegate) { animateItemPlacement(animationSpec) }
    }

    override fun Modifier.fillParentMaxHeight(fraction: Float): Modifier {
        return with(delegate) { fillParentMaxHeight(fraction) }
    }

    override fun Modifier.fillParentMaxSize(fraction: Float): Modifier {
        return with(delegate) { fillParentMaxSize(fraction) }
    }

    override fun Modifier.fillParentMaxWidth(fraction: Float): Modifier {
        return with(delegate) { fillParentMaxWidth(fraction) }
    }
}

@PublishedApi
internal fun getSectionParcelableKey(
    sections: List<List<*>>,
    sectionIndex: Int,
    itemIndex: Int
): ParcelableKey {
    var itemCount = 0
    repeat(sectionIndex) {
        itemCount += sections[it].size
    }
    return ParcelableKey(itemCount + itemIndex)
}

@PublishedApi
@Composable
internal inline fun SectionItem(
    sectionIndex: Int,
    itemIndex: Int,
    lastItemIndex: Int,
    sectionBetweenPadding: Dp,
    itemBesidePadding: Dp,
    sectionBackgroundColor: Color,
    sectionShape: CornerBasedShape,
    orientation: Orientation,
    existsHeader: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .let {
                if (sectionIndex == 0 || itemIndex != 0 || existsHeader) it else {
                    when (orientation) {
                        Orientation.Vertical -> it.padding(top = sectionBetweenPadding)
                        Orientation.Horizontal -> it.padding(start = sectionBetweenPadding)
                    }
                }
            }
            .let {
                when (orientation) {
                    Orientation.Vertical -> it.padding(horizontal = itemBesidePadding)
                    Orientation.Horizontal -> it.padding(vertical = itemBesidePadding)
                }
            }
            .clipToBackground(
                color = sectionBackgroundColor,
                shape = when (itemIndex) {
                    0 -> sectionShape.copy(
                        bottomStart = ZeroCornerSize,
                        bottomEnd = ZeroCornerSize
                    )
                    lastItemIndex -> sectionShape.copy(
                        topStart = ZeroCornerSize,
                        topEnd = ZeroCornerSize
                    )
                    else -> RectangleShape
                }
            )
    ) {
        content()
    }
}

@PublishedApi
@Composable
internal inline fun SectionHeader(
    sectionIndex: Int,
    sectionBetweenPadding: Dp,
    orientation: Orientation,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .let {
                if (sectionIndex == 0) it else {
                    when (orientation) {
                        Orientation.Vertical -> it.padding(top = sectionBetweenPadding)
                        Orientation.Horizontal -> it.padding(start = sectionBetweenPadding)
                    }
                }
            }
    ) {
        content()
    }
}

@PublishedApi
@Composable
internal inline fun SectionFooter(
    orientation: Orientation,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        content()
    }
}