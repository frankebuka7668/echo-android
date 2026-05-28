package com.echo.app.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import com.echo.app.logging.EchoLog
import java.util.Locale

class TextToSpeechEngine(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var ready: Boolean = false

    fun init() {
        if (tts != null) return
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (!ready) {
            EchoLog.w("TTS init failed: $status")
            return
        }
        tts?.language = Locale.US
        tts?.setSpeechRate(1.0f)
        tts?.setPitch(1.0f)
    }

    fun speak(text: String) {
        if (!ready) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "echo")
    }

    fun setVoice(rate: Float, pitch: Float) {
        tts?.setSpeechRate(rate)
        tts?.setPitch(pitch)
    }

    fun shutdown() {
        runCatching { tts?.stop() }
        runCatching { tts?.shutdown() }
        tts = null
        ready = false
    }
}
