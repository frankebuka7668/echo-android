package com.echo.app.core

import android.content.Context
import android.content.SharedPreferences

object EchoServiceController {
    private const val PREFS = "echo_prefs"
    private const val KEY_AUTOSTART = "autostart"

    fun ensureRunning(context: Context) {
        prefs(context).edit().putBoolean(KEY_AUTOSTART, true).apply()
    }

    fun shouldAutoStart(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_AUTOSTART, true)
    }

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }
}
