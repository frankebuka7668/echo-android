package com.echo.app.core

import android.content.Context
import com.echo.app.ai.GroqAiEngine
import com.echo.app.ai.GroqApiClient
import com.echo.app.decision.DecisionEngine
import com.echo.app.logging.EchoLog
import com.echo.app.overlay.OverlayController
import com.echo.app.safety.SafetyController
import com.echo.app.voice.SpeechToTextController
import com.echo.app.voice.TextToSpeechEngine
import kotlinx.coroutines.CoroutineScope

class EchoOrchestrator(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val safety = SafetyController(context)
    private val tts = TextToSpeechEngine(context)
    private val stt = SpeechToTextController(context, scope)
    private val groq = GroqApiClient(context)
    private val decision = DecisionEngine(safety)
    private val ai = GroqAiEngine(scope, groq, decision)
    private val overlay = OverlayController(context, scope)

    fun start() {
        tts.init()
        overlay.start(
            onPushToTalk = { stt.startOnce() },
            onSendText = { text -> handleUserText(text) },
            onStopEcho = { handleStopCommand() }
        )
        stt.setListener { text ->
            handleUserText(text)
        }
    }

    fun stop() {
        overlay.stop()
        stt.stop()
        tts.shutdown()
    }

    private fun handleUserText(text: String) {
        if (text.isBlank()) return
        if (text.trim().equals("STOP ECHO", ignoreCase = true)) {
            handleStopCommand()
            return
        }
        EchoLog.i("User input: $text")
        overlay.setAssistantThinking(true)
        ai.respond(text,
            onResult = { result ->
                overlay.setAssistantThinking(false)
                overlay.showAssistantText(result.text)
                if (!safety.isSilentModeEnabled()) tts.speak(result.text)
                decision.dispatchActions(result.actions)
            },
            onError = { err ->
                overlay.setAssistantThinking(false)
                overlay.showAssistantText("Error: ${err.message ?: "unknown"}")
            }
        )
    }

    private fun handleStopCommand() {
        EchoLog.w("Emergency stop invoked")
        overlay.stop()
        stt.stop()
        tts.shutdown()
    }
}
