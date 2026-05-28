package com.echo.app.logging

import android.util.Log

object EchoLog {
    private const val TAG = "ECHO"

    fun init() = Unit

    fun d(message: String) {
        Log.d(TAG, message)
    }

    fun i(message: String) {
        Log.i(TAG, message)
    }

    fun w(message: String, tr: Throwable? = null) {
        Log.w(TAG, message, tr)
    }

    fun e(message: String, tr: Throwable? = null) {
        Log.e(TAG, message, tr)
    }
}
