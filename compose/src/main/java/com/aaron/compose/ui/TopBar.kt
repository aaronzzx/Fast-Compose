package com.aaron.compose.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.contentColorFor
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.aaron.compose.ktx.findActivity
import com.aaron.compose.ktx.toDp

/**
 * 标题居中的 TopBar
 *
 * @param title 标题
 * @param startIcon 左侧（LTR）的图标
 * @param showStartIcon 是否显示左侧（LTR）图标
 * @param backgroundColor 背景色
 * @param contentColor 内容色
 * @param titleStyle 标题文本属性
 * @param elevation 阴影高度
 * @param showBottomDivider 是否显示分割线
 * @param bottomDividerColor 分割线颜色
 * @param contentPadding 内容间距
 * @param onStartIconClick 左侧（LTR）图标点击回调
 * @param startLayout 自定义左侧（LTR）视图
 * @param endLayout 自定义右侧侧（LTR）视图
 * @param titleLayout 自定义标题视图
 */
@Composable
fun TopBar(
    title: String,
    modifier: Modifier = Modifier,
    @DrawableRes startIcon: Int? = null,
    showStartIcon: Boolean = true,
    backgroundColor: Color = Color.White,
    contentColor: Color = Color(0xFF333333),
    titleStyle: TextStyle = TextStyle(
        fontSize = 18.sp,
        fontWeight = null
    ),
    elevation: Dp = 0.dp,
    showBottomDivider: Boolean = false,
    bottomDividerColor: Color = Color(0xFFF2F2F2),
    contentPadding: PaddingValues = WindowInsets.statusBars.asPaddingValues(),
    onStartIconClick: (() -> Unit)? = null,
    startLayout: (@Composable BoxScope.() -> Unit)? = null,
    endLayout: (@Composable BoxScope.() -> Unit)? = null,
    titleLayout: (@Composable BoxScope.(String) -> Unit)? = null
) {
    BaseTopBar(
        modifier = modifier.height(44.dp + contentPadding.calculateTopPadding()),
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        elevation = elevation,
        showBottomDivider = showBottomDivider,
        bottomDividerColor = bottomDividerColor,
        contentPadding = contentPadding,
        startLayout = {
            if (startLayout != null) {
                this.startLayout()
            } else if (startIcon != null && showStartIcon) {
                val context = LocalContext.current
                IconButton(
                    onClick = {
                        if (onStartIconClick != null) {
                            onStartIconClick()
                        } else {
                            context.findActivity()?.finish()
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(id = startIcon),
                        contentDescription = null,
                        tint = contentColor
                    )
                }
            }
        },
        endLayout = endLayout
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides titleStyle
        ) {
            if (titleLayout != null) {
                this.titleLayout(title)
            } else {
                Text(
                    text = title,
                    color = LocalContentColor.current,
                    style = LocalTextStyle.current,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * TopBar 插槽
 *
 * @param backgroundColor 背景色
 * @param contentColor 内容色
 * @param elevation 阴影高度
 * @param betweenLayoutPadding [startLayout] 、[endLayout] 、[titleLayout] 之间的间距
 * @param showBottomDivider 是否显示分割线
 * @param bottomDividerHeight 分割线高度
 * @param bottomDividerColor 分割线颜色
 * @param contentPadding 内容间距
 * @param startLayout 位于左侧（LTR）的视图
 * @param endLayout 位于右侧（LTR）的视图
 * @param titleLayout 标题视图
 */
@Composable
fun BaseTopBar(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.primarySurface,
    contentColor: Color = contentColorFor(backgroundColor),
    elevation: Dp = AppBarDefaults.TopAppBarElevation,
    betweenLayoutPadding: Dp = 16.dp,
    showBottomDivider: Boolean = false,
    bottomDividerHeight: Dp = 1.dp,
    bottomDividerColor: Color = Color(0xFFF2F2F2),
    contentPadding: PaddingValues = AppBarDefaults.ContentPadding,
    startLayout: (@Composable BoxScope.() -> Unit)? = null,
    endLayout: (@Composable BoxScope.() -> Unit)? = null,
    titleLayout: (@Composable BoxScope.() -> Unit)? = null
) {
    TopAppBar(
        modifier = modifier,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        elevation = elevation,
        contentPadding = contentPadding
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxSize()
        ) {
            // 左边宽度
            var startWidth by remember {
                mutableStateOf(1)
            }

            // 右边宽度
            var endWidth by remember {
                mutableStateOf(1)
            }

            val (startLayoutId, endLayoutId, titleLayoutId, bottomDividerId) = createRefs()

            // start layout
            Box(
                modifier = Modifier
                    .background(if (DEBUG) Color.Red else Color.Unspecified)
                    .fillMaxHeight()
                    .constrainAs(startLayoutId) {
                        start.linkTo(parent.start)
                        end.linkTo(titleLayoutId.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                    .onGloballyPositioned {
                        startWidth = it.size.width
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                if (startLayout != null) {
                    this.startLayout()
                }
            }

            // end layout
            Box(
                modifier = Modifier
                    .background(if (DEBUG) Color.Blue else Color.Unspecified)
                    .fillMaxHeight()
                    .constrainAs(endLayoutId) {
                        start.linkTo(titleLayoutId.end)
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                    .onGloballyPositioned {
                        endWidth = it.size.width
                    },
                contentAlignment = Alignment.CenterEnd
            ) {
                if (endLayout != null) {
                    this.endLayout()
                }
            }

            // title layout
            val titleStartPadding = if (startWidth >= endWidth) 0 else endWidth - startWidth
            val titleEndPadding = if (startWidth < endWidth) 0 else startWidth - endWidth
            Box(
                modifier = Modifier
                    .background(if (DEBUG) Color.Green else Color.Unspecified)
                    .padding(start = titleStartPadding.toDp(), end = titleEndPadding.toDp())
                    .padding(horizontal = betweenLayoutPadding)
                    .constrainAs(titleLayoutId) {
                        start.linkTo(startLayoutId.end)
                        end.linkTo(endLayoutId.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.fillToConstraints
                    },
                contentAlignment = Alignment.Center
            ) {
                if (titleLayout != null) {
                    this.titleLayout()
                }
            }

            // divider
            if (showBottomDivider) {
                Divider(
                    modifier = Modifier.constrainAs(bottomDividerId) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.fillToConstraints
                    },
                    color = bottomDividerColor,
                    thickness = bottomDividerHeight
                )
            }
        }
    }
}

private const val DEBUG = false