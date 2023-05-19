package com.aaron.fastcompose.ui.textshader

import android.graphics.BitmapShader
import android.graphics.Shader
import android.os.Bundle
import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navArgument
import com.aaron.compose.base.BaseRoute
import com.aaron.compose.base.navTo
import com.aaron.compose.ui.TopBar
import com.aaron.fastcompose.R
import com.aaron.fastcompose.ui.helloworld.HelloWorldScreen
import com.aaron.fastcompose.ui.textshader.TextShaderScreen.Keys.LogContent
import com.aaron.fastcompose.ui.textshader.TextShaderScreen.Keys.ToastContent
import com.blankj.utilcode.util.ToastUtils
import com.google.accompanist.navigation.animation.composable

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/12/28
 */

object TextShaderScreen : BaseRoute {

    object Keys {
        const val ToastContent = "toastContent"
        const val LogContent = "logContent"
    }

    override val route: String = "text-shader?${ToastContent}={$ToastContent}&${LogContent}={$LogContent}"

    fun navigate(
        navController: NavController,
        toastContent: String? = null,
        logContent: String? = null,
        navOptions: NavOptions? = null
    ) {
        navController.navTo(
            route = TextShaderScreen,
            args = Bundle().apply {
                if (toastContent != null) {
                    putString(ToastContent, toastContent)
                }
                if (logContent != null) {
                    putString(LogContent, logContent)
                }
            },
            navOptions = navOptions
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.textShaderScreen(navController: NavController) {
    composable(
        route = TextShaderScreen.route,
        arguments = listOf(
            navArgument(ToastContent) {
                nullable = true
                defaultValue = "Default Toast Content"
            },
            navArgument(LogContent) {
                nullable = true
                defaultValue = "Default Log Content"
            }
        )
    ) { backStackEntry ->
        val toastContent = backStackEntry.arguments?.getString(ToastContent)
        val logContent = backStackEntry.arguments?.getString(LogContent)
        TextShaderScreen(
            toastContent = toastContent,
            logContent = logContent,
            onBack = {
                navController.popBackStack()
            },
            onNavToTextShader = {
                HelloWorldScreen.navigate(navController)
            }
        )
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun TextShaderScreen(
    toastContent: String?,
    logContent: String?,
    onBack: () -> Unit,
    onNavToTextShader: () -> Unit
) {
    LaunchedEffect(key1 = Unit) {
        ToastUtils.showShort(toastContent)
        Log.d("zzx", "logContent: $logContent")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colors.background
            )
    ) {
        TopBar(
            title = "Text Shader",
            startIcon = R.drawable.back,
            contentPadding = WindowInsets.statusBars.asPaddingValues(),
            backgroundColor = Color.Transparent,
            elevation = 0.dp,
            titleStyle = TextStyle(fontSize = 24.sp),
            contentColor = contentColorFor(backgroundColor = MaterialTheme.colors.background),
            onStartIconClick = {
                onBack()
            },
            endLayout = {
                IconButton(
                    onClick = {
                        onNavToTextShader()
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = LocalContentColor.current
                    )
                }
            }
        )
        Box(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
            ) {
                val bitmap = ImageBitmap.imageResource(id = R.drawable.wallpaper).asAndroidBitmap()
                val string = buildString {
                    repeat(304) {
                        append("啊")
                    }
                }
                Text(
                    text = string,
                    fontSize = 18.sp,
                    style = TextStyle(
                        brush = ShaderBrush(
                            BitmapShader(
                                bitmap,
                                Shader.TileMode.CLAMP,
                                Shader.TileMode.CLAMP
                            )
                        )
                    )
                )
            }
        }
    }
}