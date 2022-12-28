package com.aaron.fastcompose

import android.content.Context
import android.content.Intent
import android.graphics.BitmapShader
import android.graphics.Shader
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aaron.compose.base.BaseComposeActivity
import com.aaron.compose.ktx.requireActivity
import com.aaron.compose.ui.TopBar

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/12/28
 */
class TextShaderActivity : BaseComposeActivity() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, TextShaderActivity::class.java))
        }
    }

    @Composable
    override fun Content() {
        TextShaderScreen()
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun TextShaderScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = Color.Black
            )
    ) {
        val activity = LocalContext.current.requireActivity()
        TopBar(
            title = "Text Shader",
            startIcon = R.drawable.back,
            contentPadding = WindowInsets.statusBars.asPaddingValues(),
            backgroundColor = Color.Transparent,
            elevation = 0.dp,
            titleSize = 24.sp,
            contentColor = Color.White,
            onStartIconClick = {
                activity.finish()
            },
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