package com.aaron.fastcompose

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.aaron.compose.base.BaseComposeActivity
import com.aaron.compose.ktx.requireActivity
import com.aaron.compose.ui.TopBar
import com.aaron.fastcompose.ui.theme.FastComposeTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : BaseComposeActivity() {

    @Composable
    override fun Content() {
        val uiController = rememberSystemUiController()
        uiController.setStatusBarColor(Color.Transparent)
        BackHandler {
            finish()
        }
        FastComposeTheme() {
            Surface(
                modifier = Modifier
                    .fillMaxSize(),
                elevation = 4.dp,
                color = MaterialTheme.colors.background
            ) {
                Column {
                    val activity = LocalContext.current.requireActivity()
                    TopBar(
                        title = "天下第一",
                        contentPadding = WindowInsets.statusBars.asPaddingValues(),
                        startIcon = R.drawable.back,
                        onStartIconClick = {
                            activity.finishAfterTransition()
                        }
                    )
                    var i by remember {
                        mutableStateOf(0)
                    }
                    Button(onClick = { i++ }) {
                        Column {
                            Text(text = "$i")
                            IntIcon(resId = R.drawable.img)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IntIcon(resId: Int) {
    Log.d("zzx", "IntIcon")
    androidx.compose.material.Icon(painter = painterResource(id = resId), contentDescription = null)
}