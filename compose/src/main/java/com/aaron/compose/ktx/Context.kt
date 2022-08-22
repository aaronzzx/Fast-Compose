package com.aaron.compose.ktx

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * 通过 Context 找到 Activity
 */
fun Context.requireActivity(): Activity {
    val activity = findActivity()
    require(activity != null) {
        "Activity must not be null."
    }
    return activity
}

/**
 * 通过 Context 找到 Activity
 */
fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

/**
 * 通过 Context 找到 Activity
 */
inline fun <reified T : Activity> Context.requireGenericActivity(): T {
    val activity = findGenericActivity<T>()
    require(activity != null) {
        "Activity must not be null."
    }
    return activity
}

/**
 * 通过 Context 找到 Activity
 */
inline fun <reified T : Activity> Context.findGenericActivity(): T? {
    var context = this
    while (context is ContextWrapper) {
        if (context is T) return context
        context = context.baseContext
    }
    return null
}