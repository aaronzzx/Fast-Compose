package com.aaron.fastcompose

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LeadingIconTab
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.aaron.compose.base.BaseComposeActivity
import com.aaron.compose.ktx.onClick
import com.aaron.compose.ui.BaseTabBar
import com.aaron.compose.ui.CoilImage
import com.aaron.compose.ui.ResIcon
import com.aaron.compose.ui.BaseTopBar
import com.aaron.compose.ui.NonRippleLeadingTab
import com.aaron.compose.ui.NonRippleTab
import com.aaron.compose.ui.TopBar
import com.aaron.fastcompose.theme.ComposeTheme
import com.blankj.utilcode.util.ToastUtils
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : BaseComposeActivity() {

    @Composable
    override fun Content() {
        val uiController = rememberSystemUiController()
        uiController.setStatusBarColor(Color.White)
        uiController.statusBarDarkContentEnabled = true
        BackHandler {
            finish()
        }
        ComposeTheme() {
            Surface(
                modifier = Modifier
                    .fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {
                Column(modifier = Modifier.statusBarsPadding()) {
                    TopBar(
                        backgroundColor = Color.White,
                        title = "天下第一",
                        backIcon = R.drawable.back
                    )

                    val tabTexts = listOf("家族", "推荐", "活动", "家族", "推荐", "活动")
                    var selectedTab by remember {
                        mutableStateOf(0)
                    }
                    BaseTabBar(
                        selectedTabIndex = selectedTab,
                        backgroundColor = Color.White,
                        scrollable = true
                    ) {
                        tabTexts.forEachIndexed { index, str ->
                            val isSelected = selectedTab == index
                            NonRippleLeadingTab(
                                selected = isSelected,
                                onClick = {
                                    selectedTab = index
                                },
                                text = {
                                    Text(
                                        text = str,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                icon = {
                                    Icon(imageVector = Icons.Default.Face, contentDescription = null)
                                }
                            )
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