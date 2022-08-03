package com.aaron.compose.ktx

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * 通过 Context 找到 Activity
 */
inline fun <reified T : Activity> Context.requireActivity(): T {
    val activity = findActivity<T>()
    require(activity != null) {
        "Activity must not be null."
    }
    return activity
}

/**
 * 通过 Context 找到 Activity
 */
inline fun <reified T : Activity> Context.findActivity(): T? {
    var context = this
    while (context is ContextWrapper) {
        if (context is T) return context
        context = context.baseContext
    }
    return null
}