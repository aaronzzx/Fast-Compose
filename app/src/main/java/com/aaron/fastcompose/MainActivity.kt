package com.aaron.fastcompose

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Badge
import androidx.compose.material.BadgedBox
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aaron.compose.base.BaseComposeActivity
import com.aaron.compose.ktx.onClick
import com.aaron.compose.ui.CoilImage
import com.aaron.compose.ui.ResIcon
import com.aaron.compose.ui.BaseTopBar
import com.aaron.compose.ui.TopBar
import com.aaron.fastcompose.theme.ComposeTheme
import com.blankj.utilcode.util.ToastUtils
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : BaseComposeActivity() {

    @Composable
    override fun Content() {
        BackHandler {
            finish()
        }
        ComposeTheme() {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                color = MaterialTheme.colors.background
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CompositionLocalProvider(
                        LocalTextStyle provides LocalTextStyle.current.copy(fontWeight = FontWeight.Black)
                    ) {
                        TopBar(
                            backgroundColor = Color.White,
                            title = "哈哈哈哈 Michael James",
                            titleColor = Color.Black,
                            titleSize = 18.sp,
                            backIcon = R.drawable.back
                        )

                        Text(text = "哈哈哈哈 Michael James", color = Color.Black, fontSize = 30.sp)
                        Text(text = "哈哈哈哈 Michael James", color = Color.Black)
                        Box {
                            Text(text = "哈哈哈哈 Michael James", fontWeight = FontWeight.Black, color = Color.Black)
                        }

                        CoilImage(
                            data = R.drawable.shape_round_rect_blue_4dp,
                            modifier = Modifier.size(100.dp)
                        )
                        CoilImage(
                            data = R.drawable.img,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                        )
                        CoilImage(
                            data = "https://jialai-dev.oss-cn-hangzhou.aliyuncs.com/dy/cover/2022-07-01/cover-20220701706000_720x1280.jpg",
                            modifier = Modifier
                                .clip(CircleShape)
                                .fillMaxWidth()
                                .height(300.dp)
                                .background(Color.Red.copy(0.5f)),
                            contentScale = ContentScale.Fit,
                            alignment = Alignment.BottomEnd
                        ) {
                            it.placeholder(R.drawable.ic_launcher_foreground)
                                .error(R.drawable.ic_launcher_foreground)
                                .crossfade(100)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Greeting(name: String) {
    Text(
        text = "Hello $name!"
    )
}

@Composable
private fun HelloEvent(content: (HelloEvent) -> Unit) {
    Log.d("zzx", "1")
    val currentContent by rememberUpdatedState(newValue = content)
    val subscriber = remember {
        object : Any() {
            @Subscribe(threadMode = ThreadMode.MAIN)
            fun onHelloEvent(event: HelloEvent) {
                Log.d("zzx", "2")
                currentContent(event)
            }
        }
    }
    Log.d("zzx", "3")
    EventBusComponent(subscriber = subscriber)
    Log.d("zzx", "4")
}