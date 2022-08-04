package com.aaron.fastcompose

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aaron.compose.base.BaseComposeActivity
import com.aaron.compose.ui.LeadingIconTabBar
import com.aaron.compose.ui.TopBar
import com.aaron.fastcompose.theme.ComposeTheme
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
                elevation = 4.dp,
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
                    LeadingIconTabBar(
                        selectedTabIndex = selectedTab,
                        data = tabTexts,
                        scrollable = true,
                        enableRipple = false,
                        onTabClick = { index, item ->
                            selectedTab = index
                        },
                        backgroundColor = Color.White,
                        divider = {},
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                modifier = Modifier
                                    .tabIndicatorOffset(tabPositions[selectedTab])
                                    .requiredWidth(16.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                height = 3.dp,
                                color = MaterialTheme.colors.primarySurface
                            )
                        },
                        icon = { index, item ->
                            Icon(imageVector = Icons.Default.Face, contentDescription = null)
                        }
                    ) { index, item ->
                        Text(
                            text = item,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
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