package com.echo.app.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.echo.app.logging.EchoLog
import kotlinx.coroutines.CoroutineScope

class SpeechToTextController(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var listener: ((String) -> Unit)? = null

    fun setListener(listener: (String) -> Unit) {
        this.listener = listener
    }

    fun startOnce() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            EchoLog.w("Speech recognition not available")
            return
        }

        val sr = speechRecognizer ?: SpeechRecognizer.createSpeechRecognizer(context).also {
            it.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) = Unit
                override fun onBeginningOfSpeech() = Unit
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() = Unit

                override fun onError(error: Int) {
                    EchoLog.w("STT error: $error")
                }

                override fun onResults(results: Bundle?) {
                    val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
                    val text = list.firstOrNull().orEmpty()
                    if (text.isNotBlank()) listener?.invoke(text)
                }

                override fun onPartialResults(partialResults: Bundle?) = Unit
                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            })
        }

        speechRecognizer = sr
        sr.startListening(recognizerIntent())
    }

    fun stop() {
        runCatching { speechRecognizer?.stopListening() }
        runCatching { speechRecognizer?.cancel() }
        runCatching { speechRecognizer?.destroy() }
        speechRecognizer = null
    }

    private fun recognizerIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
    }
}
