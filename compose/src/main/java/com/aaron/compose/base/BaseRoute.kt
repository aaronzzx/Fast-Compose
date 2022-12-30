package com.aaron.compose.base

import android.os.Bundle
import androidx.core.net.toUri
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.Navigator
import androidx.navigation.PopUpToBuilder

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/12/30
 */
interface BaseRoute {

    /**
     * 1. 只有路径：home , home/image
     *
     * 2. 路径+参数：home/image/{userId}/{userName}
     *
     * 3. 路径+可选参数：home/image/?userId={userId}&userName={userName}
     *
     * 4. 路径+参数+可选参数：home/image/{index}?userId={userId}&userName={userName}
     */
    val route: String

    fun match(route: String?): Boolean {
        return route == this.route
    }
}

fun NavBackStackEntry?.match(vararg route: BaseRoute): Boolean {
    val entryRoute = this?.destination?.route
    return route.any { it.match(entryRoute) }
}

fun NavController.navTo(
    route: BaseRoute,
    args: Bundle? = null,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null
) {
    if (args == null || args.isEmpty) {
        navigate(route.route, navOptions, navigatorExtras)
    } else {
        val routeLink = NavDeepLinkRequest
            .Builder
            .fromUri(NavDestination.createRoute(route.route).toUri())
            .build()
        val deepLinkMatch = graph.matchDeepLink(routeLink)
        if (deepLinkMatch != null) {
            val destination = deepLinkMatch.destination
            val id = destination.id
            navigate(id, args, navOptions, navigatorExtras)
        } else {
            navigate(route.route, navOptions, navigatorExtras)
        }
    }
}

fun NavController.popBackStack(
    route: BaseRoute,
    inclusive: Boolean = false,
    saveState: Boolean = false
) {
    popBackStack(
        route = route.route,
        inclusive = inclusive,
        saveState = saveState
    )
}

inline fun NavOptionsBuilder.popUpTo(
    route: BaseRoute,
    crossinline popUpToBuilder: PopUpToBuilder.() -> Unit = {}
) {
    popUpTo(route = route.route) {
        popUpToBuilder()
    }
}