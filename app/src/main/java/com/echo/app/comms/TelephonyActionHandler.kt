package com.echo.app.comms

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.telecom.TelecomManager
import com.echo.app.logging.EchoLog

class TelephonyActionHandler(private val context: Context) {
    private val telecom = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    private val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun answerCall(): Boolean {
        return runCatching {
            telecom.acceptRingingCall()
            true
        }.getOrElse {
            EchoLog.w("answerCall failed", it)
            false
        }
    }

    fun endCall(): Boolean {
        if (Build.VERSION.SDK_INT < 28) return false
        return runCatching {
            telecom.endCall()
            true
        }.getOrElse {
            EchoLog.w("endCall failed", it)
            false
        }
    }

    fun muteRinger(): Boolean {
        return runCatching {
            audio.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0)
            true
        }.getOrElse {
            EchoLog.w("muteRinger failed", it)
            false
        }
    }
}
