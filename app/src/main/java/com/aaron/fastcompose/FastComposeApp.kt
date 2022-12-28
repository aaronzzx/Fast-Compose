package com.aaron.fastcompose

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.aaron.compose.utils.noLocalProvidedFor
import com.aaron.fastcompose.paging.PagingScreen
import com.aaron.fastcompose.route.RouteUrls
import com.aaron.fastcompose.ui.theme.FastComposeTheme
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
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
        val navController = rememberAnimatedNavController()
        CompositionLocalProvider(
            LocalNavController providesDefault navController
        ) {
            FastComposeNavHost(navController = navController)
            HandleSystemBar(navController = navController)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun FastComposeNavHost(navController: NavHostController) {
    AnimatedNavHost(
        navController = navController,
        startDestination = RouteUrls.Home,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentScope.SlideDirection.Up,
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        },
        exitTransition = {
            fadeOut()
        },
        popEnterTransition = {
            fadeIn()
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentScope.SlideDirection.Down,
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    ) {
        composable(route = RouteUrls.Home) {
            HomeScreen()
        }
        composable(route = RouteUrls.Paging) {
            PagingScreen(vm = hiltViewModel())
        }
        composable(route = RouteUrls.TextShader) {
            TextShaderScreen()
        }
    }
}

@Composable
private fun HandleSystemBar(navController: NavController) {
    val systemUiController = rememberSystemUiController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    when (currentBackStackEntry?.destination?.route) {
        RouteUrls.Home -> {
            SideEffect {
                systemUiController.statusBarDarkContentEnabled = true
            }
        }
        RouteUrls.Paging -> {
            SideEffect {
                systemUiController.statusBarDarkContentEnabled = true
            }
        }
        RouteUrls.TextShader -> {
            SideEffect {
                systemUiController.statusBarDarkContentEnabled = false
            }
        }
    }
}

val LocalNavController =
    staticCompositionLocalOf<NavController> { noLocalProvidedFor("NavController") }