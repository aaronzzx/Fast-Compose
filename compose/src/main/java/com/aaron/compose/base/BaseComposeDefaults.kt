package com.aaron.compose.base

import androidx.compose.runtime.Composable

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/12/2
 */
object BaseComposeDefaults {

    var entrance: @Composable (ComposableFun) -> Unit = { content ->
        content()
    }
}

private typealias ComposableFun = @Composable () -> Unit