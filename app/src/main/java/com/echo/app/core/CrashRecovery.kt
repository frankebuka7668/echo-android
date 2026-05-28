package com.echo.app.core

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.echo.app.logging.EchoLog
import kotlin.system.exitProcess

object CrashRecovery {
    private const val RESTART_DELAY_MS = 1500L

    fun install(app: Application) {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            EchoLog.e("Uncaught exception on ${t.name}", e)
            scheduleRestart(app)
            previous?.uncaughtException(t, e) ?: run {
                exitProcess(10)
            }
        }
    }

    private fun scheduleRestart(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, CrashRestartReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExact(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + RESTART_DELAY_MS,
            pi
        )
    }
}
