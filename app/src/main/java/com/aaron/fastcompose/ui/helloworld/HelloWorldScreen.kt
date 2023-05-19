package com.aaron.fastcompose.ui.helloworld

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import com.aaron.compose.base.BaseRoute
import com.aaron.compose.base.navTo
import com.aaron.compose.base.popBackStack
import com.aaron.compose.ui.TopBar
import com.aaron.fastcompose.R
import com.aaron.fastcompose.ui.home.HomeScreen
import com.google.accompanist.navigation.animation.composable

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/12/29
 */

object HelloWorldScreen : BaseRoute {

    override val route: String = "hello-world"

    fun navigate(navController: NavController, navOptions: NavOptions? = null) {
        navController.navTo(
            route = HelloWorldScreen,
            navOptions = navOptions
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.helloWorldScreen(navController: NavController) {
    composable(route = HelloWorldScreen.route) {
        HelloWorldScreen(
            onBack = {
                navController.popBackStack()
            },
            onPopBackToHome = {
                navController.popBackStack(HomeScreen)
            }
        )
    }
}

@Composable
private fun HelloWorldScreen(
    onBack: () -> Unit,
    onPopBackToHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colors.background
            )
    ) {
        TopBar(
            title = "Hello World",
            startIcon = R.drawable.back,
            contentPadding = WindowInsets.statusBars.asPaddingValues(),
            backgroundColor = Color.Transparent,
            elevation = 0.dp,
            titleStyle = TextStyle(fontSize = 24.sp),
            contentColor = contentColorFor(backgroundColor = MaterialTheme.colors.surface),
            onStartIconClick = {
                onBack()
            },
            endLayout = {
                IconButton(
                    onClick = {
                        onPopBackToHome()
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = LocalContentColor.current
                    )
                }
            }
        )

        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
                .background(
                    color = when (isSystemInDarkTheme()) {
                        true -> Color(0xFF94E497)
                        else -> Color(0xFF4CAF50)
                    },
                    shape = RoundedCornerShape(16.dp)
                )
        )
    }
}