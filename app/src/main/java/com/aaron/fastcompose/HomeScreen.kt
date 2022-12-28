package com.aaron.fastcompose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aaron.compose.ui.TopBar
import com.aaron.compose.utils.OverScrollHandler
import com.aaron.fastcompose.route.RouteUrls
import com.google.accompanist.systemuicontroller.rememberSystemUiController

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/12/28
 */

@Composable
fun HomeScreen() {
    val systemUiController = rememberSystemUiController()
    val isDarkTheme = isSystemInDarkTheme()
    SideEffect {
        systemUiController.systemBarsDarkContentEnabled = !isDarkTheme
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(
            title = "Jetpack Compose",
            contentPadding = WindowInsets.statusBars.asPaddingValues(),
            backgroundColor = Color.Transparent,
            contentColor = Color(0xFFDFB891),
            elevation = 0.dp,
            titleSize = 32.sp
        )
        OverScrollHandler(enabled = false) {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = routeItems,
                    key = { it.text },
                    contentType = { "RouteItem" }
                ) { item ->
                    val navController = LocalNavController.current
                    RouteItem(
                        onClick = {
                            item.route(navController)
                        },
                        text = item.text,
                        color = item.color
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteItem(
    onClick: () -> Unit,
    text: String,
    color: Color,
    fontColor: Color = Color(0xFFFFE6CD)
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        onClick = { onClick() },
        shape = CircleShape,
        elevation = 8.dp,
        backgroundColor = color
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 18.sp,
                color = fontColor
            )
        }
    }
}

private val routeItems = listOf(
    RouteItem(
        text = "Pagination",
        color = Color(0xFF64B5F6),
        route = {
            it.navigate(RouteUrls.Paging)
        }
    ),
    RouteItem(
        text = "Text Shader",
        color = Color(0xFFE57373),
        route = {
            it.navigate(RouteUrls.TextShader)
        }
    )
)

private data class RouteItem(
    val text: String,
    val color: Color,
    val route: (NavController) -> Unit
)