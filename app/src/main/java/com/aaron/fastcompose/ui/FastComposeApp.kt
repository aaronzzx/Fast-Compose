package com.aaron.fastcompose.ui

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.aaron.fastcompose.ui.helloworld.helloWorldScreen
import com.aaron.fastcompose.ui.home.HomeScreen
import com.aaron.fastcompose.ui.home.homeScreen
import com.aaron.fastcompose.ui.paging.pagingScreen
import com.aaron.fastcompose.ui.test.testScreen
import com.aaron.fastcompose.ui.textshader.textShaderScreen
import com.aaron.fastcompose.ui.theme.FastComposeTheme
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/12/28
 */

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FastComposeApp() {
    FastComposeTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val navController = rememberAnimatedNavController()
            FastComposeNavHost(navController = navController)
            HandleSystemUi(navController = navController)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun FastComposeNavHost(navController: NavHostController) {
    val durationMillis = COMPOSABLE_ANIMATION_DURATION_MILLIS
    AnimatedNavHost(
        navController = navController,
        startDestination = HomeScreen.route,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentScope.SlideDirection.Start,
                animationSpec = tween(
                    durationMillis = durationMillis
                )
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentScope.SlideDirection.Start,
                animationSpec = tween(
                    durationMillis = durationMillis
                ),
                targetOffset = { it / 2 }
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentScope.SlideDirection.End,
                animationSpec = tween(
                    durationMillis = durationMillis
                ),
                initialOffset = { it / 2 }
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentScope.SlideDirection.End,
                animationSpec = tween(
                    durationMillis = durationMillis
                )
            )
        }
    ) {
        homeScreen(navController = navController)
        pagingScreen(navController = navController)
        textShaderScreen(navController = navController)
        helloWorldScreen(navController = navController)
        testScreen(navController = navController)
    }
}

@Composable
private fun HandleSystemUi(navController: NavController) {
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = true
        )
        systemUiController.setNavigationBarColor(
            color = Color.Transparent,
            darkIcons = true,
            navigationBarContrastEnforced = false
        )
    }
}

private const val COMPOSABLE_ANIMATION_DURATION_MILLIS = 400