package com.aaron.compose.ktx.lazylist

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aaron.compose.ktx.clipToBackground

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/12/7
 */

inline fun <T> LazyListScope.sections(
    sections: List<List<T>>,
    orientation: Orientation,
    sectionSpacing: Dp = LazySectionDefaults.spacing,
    sectionBackgroundColor: Color = LazySectionDefaults.backgroundColor,
    sectionShape: CornerBasedShape = LazySectionDefaults.shape,
    crossinline itemKey: (item: T) -> Any? = { null },
    crossinline itemContentType: (item: T) -> Any? = { null },
    noinline outerHeader: (@Composable LazyItemScope.(sectionIndex: Int) -> Unit)? = null,
    noinline outerFooter: (@Composable LazyItemScope.(sectionIndex: Int) -> Unit)? = null,
    noinline innerHeader: (@Composable LazyItemScope.(sectionIndex: Int) -> Unit)? = null,
    noinline innerFooter: (@Composable LazyItemScope.(sectionIndex: Int) -> Unit)? = null,
    crossinline itemContent: @Composable LazyItemScope.(item: T) -> Unit
) {
    sectionsIndexed(
        sections = sections,
        orientation = orientation,
        sectionSpacing = sectionSpacing,
        sectionBackgroundColor = sectionBackgroundColor,
        sectionShape = sectionShape,
        itemKey = { _, item -> itemKey(item) },
        itemContentType = { _, item -> itemContentType(item) },
        outerHeader = outerHeader,
        outerFooter = outerFooter,
        innerHeader = innerHeader,
        innerFooter = innerFooter
    ) { _, _, item ->
        itemContent(item)
    }
}

inline fun <T> LazyListScope.sectionsIndexed(
    sections: List<List<T>>,
    orientation: Orientation,
    sectionSpacing: Dp = LazySectionDefaults.spacing,
    sectionBackgroundColor: Color = LazySectionDefaults.backgroundColor,
    sectionShape: CornerBasedShape = LazySectionDefaults.shape,
    crossinline itemKey: (index: Int, item: T) -> Any? = { _, _ -> null },
    crossinline itemContentType: (index: Int, item: T) -> Any? = { _, _ -> null },
    noinline outerHeader: (@Composable LazyItemScope.(sectionIndex: Int) -> Unit)? = null,
    noinline outerFooter: (@Composable LazyItemScope.(sectionIndex: Int) -> Unit)? = null,
    noinline innerHeader: (@Composable LazyItemScope.(sectionIndex: Int) -> Unit)? = null,
    noinline innerFooter: (@Composable LazyItemScope.(sectionIndex: Int) -> Unit)? = null,
    crossinline itemContent: @Composable LazyItemScope.(sectionIndex: Int, itemIndex: Int, item: T) -> Unit
) {
    sections.forEachIndexed { sectionIndex, section ->
        if (outerHeader != null) {
            item(
                key = "OuterHeader-$sectionIndex",
                contentType = "OuterHeader"
            ) {
                SectionHeader(
                    lazyScopeModifier = when (orientation) {
                        Orientation.Vertical -> Modifier.fillParentMaxWidth()
                        Orientation.Horizontal -> Modifier.fillParentMaxHeight()
                    },
                    outer = true,
                    sectionIndex = sectionIndex,
                    sectionSpacing = sectionSpacing,
                    sectionBackgroundColor = sectionBackgroundColor,
                    sectionShape = sectionShape,
                    orientation = orientation
                ) {
                    outerHeader(sectionIndex)
                }
            }
        }
        if (innerHeader != null) {
            item(
                key = "InnerHeader-$sectionIndex",
                contentType = "InnerHeader"
            ) {
                SectionHeader(
                    lazyScopeModifier = when (orientation) {
                        Orientation.Vertical -> Modifier.fillParentMaxWidth()
                        Orientation.Horizontal -> Modifier.fillParentMaxHeight()
                    },
                    outer = false,
                    sectionIndex = sectionIndex,
                    sectionSpacing = sectionSpacing.takeIf { outerHeader == null } ?: 0.dp,
                    sectionBackgroundColor = sectionBackgroundColor,
                    sectionShape = sectionShape,
                    orientation = orientation
                ) {
                    innerHeader(sectionIndex)
                }
            }
        }
        itemsIndexed(
            items = section,
            key = { index, item ->
                itemKey(index, item) ?: getSectionParcelableKey(sectionIndex, index, section.size)
            },
            contentType = { index, item ->
                itemContentType(index, item)
            }
        ) { index, item ->
            SectionItem(
                lazyScopeModifier = when (orientation) {
                    Orientation.Vertical -> Modifier.fillParentMaxWidth()
                    Orientation.Horizontal -> Modifier.fillParentMaxHeight()
                },
                sectionIndex = sectionIndex,
                itemIndex = index,
                lastItemIndex = section.lastIndex,
                sectionSpacing = sectionSpacing,
                sectionBackgroundColor = sectionBackgroundColor,
                sectionShape = sectionShape,
                orientation = orientation,
                existsOuterHeader = outerHeader != null,
                existsOuterFooter = outerFooter != null,
                existsInnerHeader = innerHeader != null,
                existsInnerFooter = innerFooter != null
            ) {
                itemContent(sectionIndex, index, item)
            }
        }
        if (innerFooter != null) {
            item(
                key = "InnerFooter-$sectionIndex",
                contentType = "InnerFooter"
            ) {
                SectionFooter(
                    lazyScopeModifier = when (orientation) {
                        Orientation.Vertical -> Modifier.fillParentMaxWidth()
                        Orientation.Horizontal -> Modifier.fillParentMaxHeight()
                    },
                    outer = false,
                    sectionBackgroundColor = sectionBackgroundColor,
                    sectionShape = sectionShape,
                    orientation = orientation
                ) {
                    innerFooter(sectionIndex)
                }
            }
        }
        if (outerFooter != null) {
            item(
                key = "OuterFooter-$sectionIndex",
                contentType = "OuterFooter"
            ) {
                SectionFooter(
                    lazyScopeModifier = when (orientation) {
                        Orientation.Vertical -> Modifier.fillParentMaxWidth()
                        Orientation.Horizontal -> Modifier.fillParentMaxHeight()
                    },
                    outer = true,
                    sectionBackgroundColor = sectionBackgroundColor,
                    sectionShape = sectionShape,
                    orientation = orientation,
                ) {
                    outerFooter(sectionIndex)
                }
            }
        }
    }
}

@PublishedApi
internal object LazySectionDivider

@PublishedApi
internal fun getSectionParcelableKey(
    sectionIndex: Int,
    itemIndex: Int,
    itemCount: Int
): ParcelableKey {
    // 0 * 50 + 1 = 1
    // 1 * 50 + 1 = 51
    // 2 * 50 + 1 = 101
    // 3 * 50 + 1 = 151
    return ParcelableKey(sectionIndex * itemCount + itemIndex)
}

@PublishedApi
@Composable
internal fun SectionItem(
    sectionIndex: Int,
    itemIndex: Int,
    lastItemIndex: Int,
    sectionSpacing: Dp,
    sectionBackgroundColor: Color,
    sectionShape: CornerBasedShape,
    orientation: Orientation,
    existsOuterHeader: Boolean,
    existsOuterFooter: Boolean,
    existsInnerHeader: Boolean,
    existsInnerFooter: Boolean,
    modifier: Modifier = Modifier,
    lazyScopeModifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .let {
                if (sectionIndex == 0
                    || itemIndex != 0
                    || existsOuterHeader
                    || existsInnerHeader
                    || sectionSpacing <= Dp.Hairline
                ) it else {
                    it.padding(top = sectionSpacing)
                }
            }
            .then(lazyScopeModifier)
            .let {
                if (sectionBackgroundColor.alpha == 0f) it else {
                    it.clipToBackground(
                        color = sectionBackgroundColor,
                        shape = when {
                            itemIndex == 0 && !existsInnerHeader -> sectionShape.copy(
                                bottomStart = ZeroCornerSize,
                                bottomEnd = ZeroCornerSize
                            )
                            itemIndex == lastItemIndex && !existsInnerFooter -> sectionShape.copy(
                                topStart = ZeroCornerSize,
                                topEnd = ZeroCornerSize
                            )
                            else -> RectangleShape
                        }
                    )
                }
            }
    ) {
        content()
    }
}

@PublishedApi
@Composable
internal fun SectionHeader(
    outer: Boolean,
    sectionIndex: Int,
    sectionSpacing: Dp,
    sectionBackgroundColor: Color,
    sectionShape: CornerBasedShape,
    orientation: Orientation,
    modifier: Modifier = Modifier,
    lazyScopeModifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .let {
                if (sectionIndex == 0 || sectionSpacing <= Dp.Hairline) it else {
                    when (orientation) {
                        Orientation.Vertical -> it.padding(top = sectionSpacing)
                        Orientation.Horizontal -> it.padding(start = sectionSpacing)
                    }
                }
            }
            .then(lazyScopeModifier)
            .let {
                if (outer || sectionBackgroundColor.alpha == 0f) it else {
                    when (orientation) {
                        Orientation.Vertical -> it.clipToBackground(
                            color = sectionBackgroundColor,
                            shape = sectionShape.copy(
                                bottomStart = ZeroCornerSize,
                                bottomEnd = ZeroCornerSize
                            )
                        )
                        Orientation.Horizontal -> it.clipToBackground(
                            color = sectionBackgroundColor,
                            shape = sectionShape.copy(
                                topEnd = ZeroCornerSize,
                                bottomEnd = ZeroCornerSize
                            )
                        )
                    }
                }
            }
    ) {
        content()
    }
}

@PublishedApi
@Composable
internal fun SectionFooter(
    outer: Boolean,
    sectionBackgroundColor: Color,
    sectionShape: CornerBasedShape,
    orientation: Orientation,
    modifier: Modifier = Modifier,
    lazyScopeModifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .then(lazyScopeModifier)
            .let {
                if (outer || sectionBackgroundColor.alpha == 0f) it else {
                    when (orientation) {
                        Orientation.Vertical -> it.clipToBackground(
                            color = sectionBackgroundColor,
                            shape = sectionShape.copy(
                                topStart = ZeroCornerSize,
                                topEnd = ZeroCornerSize
                            )
                        )
                        Orientation.Horizontal -> it.clipToBackground(
                            color = sectionBackgroundColor,
                            shape = sectionShape.copy(
                                topStart = ZeroCornerSize,
                                bottomStart = ZeroCornerSize
                            )
                        )
                    }
                }
            }
    ) {
        content()
    }
}