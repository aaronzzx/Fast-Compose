package com.aaron.compose.ui

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.annotation.ExperimentalCoilApi
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.transform.Transformation
import com.google.accompanist.drawablepainter.rememberDrawablePainter

/**
 * 使用 Coil 加载图像
 *
 * @param data 数据源
 * @param modifier 修饰符
 * @param contentDescription 用于无障碍的描述
 * @param alignment 可选的对齐参数，用于将 Painter 放置在由宽度和高度定义的给定边界内。
 * @param contentScale 图像缩放
 * @param alpha 透明度
 * @param colorFilter 改变渲染
 * @param onState 加载状态回调
 * @param imageBuilder 自定义请求
 */
@Composable
fun CoilImage(
    data: Any?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    onState: ((AsyncImagePainter.State) -> Unit)? = null,
    imageBuilder: ((ImageRequest.Builder) -> ImageRequest.Builder)? = null
) {
    var builder = ImageRequest.Builder(LocalContext.current).data(data)
    if (imageBuilder != null) {
        builder = imageBuilder(builder)
    }
    Image(
        painter = rememberAsyncImagePainter(
            model = builder.build(),
            onState = onState
        ),
        modifier = modifier,
        contentDescription = contentDescription,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter
    )
}

/**
 * 使用资源文件加载 Icon
 */
@Composable
fun ResIcon(
    @DrawableRes resId: Int,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = LocalContentColor.current
) {
    DrawableIcon(
        drawable = AppCompatResources.getDrawable(LocalContext.current, resId),
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}

/**
 * 使用 Drawable 加载 Icon
 */
@Composable
fun DrawableIcon(
    drawable: Drawable?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = LocalContentColor.current
) {
    Icon(
        painter = rememberDrawablePainter(drawable = drawable),
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}