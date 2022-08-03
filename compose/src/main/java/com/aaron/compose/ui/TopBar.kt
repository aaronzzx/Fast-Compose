package com.aaron.compose.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.contentColorFor
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.aaron.compose.ktx.requireActivity
import com.aaron.compose.ktx.toDp

private const val DEBUG = false

@Composable
fun TopBar(
    title: String,
    @DrawableRes backIcon: Int,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.primarySurface,
    titleColor: Color = contentColorFor(backgroundColor),
    titleSize: TextUnit = 18.sp,
    titleWeight: FontWeight? = FontWeight.Bold,
    elevation: Dp = AppBarDefaults.TopAppBarElevation,
    showBottomDivider: Boolean = false,
    bottomDividerColor: Color = Color(0xFFF2F2F2),
    onBackClick: (() -> Unit)? = null,
    startLayout: (@Composable BoxScope.() -> Unit)? = null,
    endLayout: (@Composable BoxScope.() -> Unit)? = null,
    titleLayout: (@Composable BoxScope.() -> Unit)? = null
) {
    BaseTopBar(
        modifier = modifier,
        backgroundColor = backgroundColor,
        elevation = elevation,
        showBottomDivider = showBottomDivider,
        bottomDividerColor = bottomDividerColor,
        startLayout = {
            if (startLayout != null) {
                this.startLayout()
            } else {
                val currentOnBackClick by rememberUpdatedState(newValue = onBackClick)
                val activity = LocalContext.current.requireActivity<Activity>()
                IconButton(onClick = {
                    currentOnBackClick?.invoke() ?: activity.onBackPressed()
                }) {
                    ResIcon(resId = backIcon, tint = LocalContentColor.current.copy(1f))
                }
            }
        },
        endLayout = endLayout
    ) {
        if (titleLayout != null) {
            this.titleLayout()
        } else {
            Text(
                text = title,
                color = titleColor,
                fontSize = titleSize,
                fontWeight = titleWeight,
                maxLines = 1
            )
        }
    }
}

/**
 * TopBar 插槽
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
    startLayout: (@Composable BoxScope.() -> Unit)? = null,
    endLayout: (@Composable BoxScope.() -> Unit)? = null,
    titleLayout: (@Composable BoxScope.() -> Unit)? = null
) {
    TopAppBar(
        modifier = modifier,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        elevation = elevation,
        contentPadding = PaddingValues(start = 0.dp, end = 0.dp)
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