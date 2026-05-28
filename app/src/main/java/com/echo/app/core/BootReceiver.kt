package com.echo.app.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.echo.app.logging.EchoLog

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        EchoLog.i("Boot event: ${intent.action}")
        if (!EchoServiceController.shouldAutoStart(context)) return
        val serviceIntent = Intent(context, EchoForegroundService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}
