package com.aaron.fastcompose

import android.app.Application
import android.content.Context

/**
 * @author aaronzzxup@gmail.com
 * @since 2022/7/30
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = this
    }
}

lateinit var appContext: Context
    private set