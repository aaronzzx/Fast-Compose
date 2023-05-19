package com.aaron.fastcompose.ui.test

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import com.aaron.compose.base.BaseRoute
import com.aaron.compose.base.navTo
import com.aaron.compose.ktx.onClick
import com.aaron.compose.ui.BottomSheet
import com.aaron.compose.ui.Dialog
import com.aaron.compose.ui.FloatingScaffold
import com.aaron.compose.ui.TopBar
import com.aaron.fastcompose.R
import com.google.accompanist.navigation.animation.composable

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/12/29
 */

object TestScreen : BaseRoute {

    override val route: String = "test"

    fun navigate(navController: NavController, navOptions: NavOptions? = null) {
        navController.navTo(
            route = TestScreen,
            navOptions = navOptions
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.testScreen(navController: NavController) {
    composable(route = TestScreen.route) {
        TestScreen(
            onBack = {
                navController.popBackStack()
            }
        )
    }
}

@Composable
private fun TestScreen(
    onBack: () -> Unit
) {
    var showDialog by remember {
        mutableStateOf(false)
    }
    var showBottomSheet by remember {
        mutableStateOf(false)
    }
    FloatingScaffold(
        modifier = Modifier.fillMaxSize(),
        floating = {
            Dialog(
                visible = showDialog,
                onDismiss = { showDialog = false },
                backgroundColor = Color(0xFFFF8A65)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .onClick(enableRipple = false) {
                            showBottomSheet = true
                        }
                )
            }

            BottomSheet(
                visible = showBottomSheet,
                onDismiss = { showBottomSheet = false },
                backgroundColor = Color(0xFF4DB6AC)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .onClick(enableRipple = false) {
                            showDialog = false
                            showBottomSheet = false
                        }
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colors.background)
                .navigationBarsPadding()
        ) {
            TopBar(
                title = "Hello World",
                startIcon = R.drawable.back,
                contentPadding = WindowInsets.statusBars.asPaddingValues(),
                backgroundColor = Color.Transparent,
                elevation = 0.dp,
                titleSize = 24.sp,
                contentColor = contentColorFor(backgroundColor = MaterialTheme.colors.surface),
                onStartIconClick = {
                    onBack()
                }
            )

            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick = { showDialog = true }) {
                        Text(text = "Dialog")
                    }
                    Button(onClick = { showBottomSheet = true }) {
                        Text(text = "BottomSheet")
                    }
                }
            }
        }
    }
}