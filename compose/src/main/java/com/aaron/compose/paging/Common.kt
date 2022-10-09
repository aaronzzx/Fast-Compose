package com.aaron.compose.paging

import kotlinx.coroutines.delay

internal suspend fun makeSureTime(startTime: Long, minDelayTime: Long) {
    val costTime = System.currentTimeMillis() - startTime
    delay(costTime.coerceAtLeast(minDelayTime))
}

class AppPagingException(
    val code: Int,
    val msg: String
) : Exception("AppPagingException: $code, $msg")