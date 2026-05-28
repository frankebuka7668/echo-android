package com.echo.app.ai

import com.echo.app.decision.AiResult
import com.echo.app.decision.DecisionEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class GroqAiEngine(
    private val scope: CoroutineScope,
    private val client: GroqApiClient,
    private val decisionEngine: DecisionEngine
) {
    fun respond(
        userInput: String,
        onResult: (AiResult) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        scope.launch {
            runCatching {
                client.chat(userInput)
            }.onSuccess { content ->
                runCatching { decisionEngine.parse(content) }
                    .onSuccess(onResult)
                    .onFailure(onError)
            }.onFailure(onError)
        }
    }
}
