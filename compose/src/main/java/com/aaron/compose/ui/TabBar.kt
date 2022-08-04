package com.aaron.compose.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LeadingIconTab
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.TabPosition
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.contentColorFor
import androidx.compose.material.primarySurface
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun <T> TabBar(
    selectedTabIndex: Int,
    data: List<T>,
    modifier: Modifier = Modifier,
    enableRipple: Boolean = true,
    scrollable: Boolean = false,
    edgePadding: Dp = 0.dp,
    backgroundColor: Color = MaterialTheme.colors.primarySurface,
    contentColor: Color = contentColorFor(backgroundColor),
    selectedContentColor: Color = contentColor,
    unselectedContentColor: Color = selectedContentColor.copy(alpha = ContentAlpha.medium),
    onTabClick: (index: Int, item: T) -> Unit,
    indicator: @Composable (tabPositions: List<TabPosition>) -> Unit = { tabPositions ->
        TabRowDefaults.Indicator(
            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex])
        )
    },
    divider: @Composable () -> Unit = {
        TabRowDefaults.Divider()
    },
    tab: @Composable ColumnScope.(index: Int, item: T) -> Unit
) {
    BaseTabBar(
        selectedTabIndex = selectedTabIndex,
        data = data,
        modifier = modifier,
        scrollable = scrollable,
        edgePadding = edgePadding,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        indicator = indicator,
        divider = divider
    ) { index, item ->
        val isSelected = selectedTabIndex == index
        if (enableRipple) {
            Tab(
                modifier = modifier.heightIn(min = 44.dp),
                selected = isSelected,
                onClick = {
                    onTabClick(index, item)
                },
                selectedContentColor = selectedContentColor,
                unselectedContentColor = unselectedContentColor
            ) {
                this.tab(index, item)
            }
        } else {
            NonRippleTab(
                modifier = modifier.heightIn(min = 48.dp),
                selected = isSelected,
                onClick = {
                    onTabClick(index, item)
                },
                selectedContentColor = selectedContentColor,
                unselectedContentColor = unselectedContentColor
            ) {
                this.tab(index, item)
            }
        }
    }
}

@Composable
fun <T> LeadingIconTabBar(
    selectedTabIndex: Int,
    data: List<T>,
    modifier: Modifier = Modifier,
    enableRipple: Boolean = true,
    scrollable: Boolean = false,
    edgePadding: Dp = 0.dp,
    backgroundColor: Color = MaterialTheme.colors.primarySurface,
    contentColor: Color = contentColorFor(backgroundColor),
    selectedContentColor: Color = contentColor,
    unselectedContentColor: Color = selectedContentColor.copy(alpha = ContentAlpha.medium),
    onTabClick: (index: Int, item: T) -> Unit,
    indicator: @Composable (tabPositions: List<TabPosition>) -> Unit = { tabPositions ->
        TabRowDefaults.Indicator(
            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex])
        )
    },
    divider: @Composable () -> Unit = {
        TabRowDefaults.Divider()
    },
    icon: @Composable (index: Int, item: T) -> Unit,
    text: @Composable (index: Int, item: T) -> Unit
) {
    BaseTabBar(
        selectedTabIndex = selectedTabIndex,
        data = data,
        modifier = modifier,
        scrollable = scrollable,
        edgePadding = edgePadding,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        indicator = indicator,
        divider = divider
    ) { index, item ->
        val isSelected = selectedTabIndex == index
        if (enableRipple) {
            LeadingIconTab(
                selected = isSelected,
                onClick = {
                    onTabClick(index, item)
                },
                selectedContentColor = selectedContentColor,
                unselectedContentColor = unselectedContentColor,
                text = {
                    text(index, item)
                },
                icon = {
                    icon(index, item)
                }
            )
        } else {
            NonRippleLeadingIconTab(
                selected = isSelected,
                onClick = {
                    onTabClick(index, item)
                },
                selectedContentColor = selectedContentColor,
                unselectedContentColor = unselectedContentColor,
                text = {
                    text(index, item)
                },
                icon = {
                    icon(index, item)
                }
            )
        }
    }
}

/**
 * 基础 TabBar
 *
 * @param selectedTabIndex 当前选中索引
 * @param data Tab 数据
 * @param modifier 修饰符
 * @param scrollable 是否可滚动
 * @param edgePadding 边缘间距，只适用于 [scrollable] == true 的情况
 * @param backgroundColor 背景色
 * @param contentColor 内容色，应用于文本与图标
 * @param indicator 指示器
 * @param divider 分隔线
 * @param tabs Tab 创建回调
 */
@Composable
fun <T> BaseTabBar(
    selectedTabIndex: Int,
    data: List<T>,
    modifier: Modifier = Modifier,
    scrollable: Boolean = false,
    edgePadding: Dp = 0.dp,
    backgroundColor: Color = MaterialTheme.colors.primarySurface,
    contentColor: Color = contentColorFor(backgroundColor),
    indicator: @Composable (tabPositions: List<TabPosition>) -> Unit = { tabPositions ->
        TabRowDefaults.Indicator(
            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex])
        )
    },
    divider: @Composable () -> Unit = {
        TabRowDefaults.Divider()
    },
    tabs: @Composable (index: Int, item: T) -> Unit
) {
    if (scrollable) {
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = modifier,
            backgroundColor = backgroundColor,
            contentColor = contentColor,
            edgePadding = edgePadding,
            indicator = indicator,
            divider = divider
        ) {
            data.forEachIndexed { index, item ->
                tabs(index, item)
            }
        }
    } else {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = modifier,
            backgroundColor = backgroundColor,
            contentColor = contentColor,
            indicator = indicator,
            divider = divider
        ) {
            data.forEachIndexed { index, item ->
                tabs(index, item)
            }
        }
    }
}

/**
 * 禁用 Ripple 的 Tab
 */
@Composable
fun NonRippleTab(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    selectedContentColor: Color = LocalContentColor.current,
    unselectedContentColor: Color = selectedContentColor.copy(alpha = ContentAlpha.medium),
    content: @Composable ColumnScope.() -> Unit
) {
    val rippleTheme = LocalRippleTheme.current
    CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
        Tab(
            selected = selected,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            interactionSource = interactionSource,
            selectedContentColor = selectedContentColor,
            unselectedContentColor = unselectedContentColor
        ) {
            CompositionLocalProvider(LocalRippleTheme provides rippleTheme) {
                content()
            }
        }
    }
}

/**
 * 禁用 Ripple 的 LeadingTab
 */
@Composable
fun NonRippleLeadingIconTab(
    selected: Boolean,
    onClick: () -> Unit,
    text: @Composable (() -> Unit),
    icon: @Composable (() -> Unit),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    selectedContentColor: Color = LocalContentColor.current,
    unselectedContentColor: Color = selectedContentColor.copy(alpha = ContentAlpha.medium)
) {
    CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
        LeadingIconTab(
            selected = selected,
            onClick = onClick,
            text = text,
            icon = icon,
            modifier = modifier,
            enabled = enabled,
            interactionSource = interactionSource,
            selectedContentColor = selectedContentColor,
            unselectedContentColor = unselectedContentColor
        )
    }
}

private object NoRippleTheme : RippleTheme {
    @Composable
    override fun defaultColor() = Color.Unspecified

    @Composable
    override fun rippleAlpha(): RippleAlpha = RippleAlpha(
        0.0f,
        0.0f,
        0.0f,
        0.0f
    )
}