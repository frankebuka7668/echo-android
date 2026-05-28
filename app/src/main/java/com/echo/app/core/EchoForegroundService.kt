package com.echo.app.core

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.echo.app.logging.EchoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class EchoForegroundService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var orchestrator: EchoOrchestrator? = null

    override fun onCreate() {
        super.onCreate()
        EchoLog.i("Foreground service created")
        startForeground(
            ForegroundNotification.notificationId(),
            ForegroundNotification.create(this)
        )
        orchestrator = EchoOrchestrator(this, scope).also { it.start() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        orchestrator?.stop()
        EchoLog.w("Foreground service destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
