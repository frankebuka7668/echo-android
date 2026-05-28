package com.echo.app.comms

import android.content.Context
import android.os.Build
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import com.echo.app.logging.EchoLog

class TelephonyCallMonitor(private val context: Context) {
    private val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private var callback: TelephonyCallback? = null

    fun start() {
        if (Build.VERSION.SDK_INT < 31) {
            EchoLog.w("TelephonyCallback requires API 31+")
            return
        }
        val cb = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
            override fun onCallStateChanged(state: Int) {
                EchoLog.i("Call state: $state")
            }
        }
        callback = cb
        tm.registerTelephonyCallback(context.mainExecutor, cb)
    }

    fun stop() {
        val cb = callback ?: return
        if (Build.VERSION.SDK_INT >= 31) tm.unregisterTelephonyCallback(cb)
        callback = null
    }
}
