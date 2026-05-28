package com.echo.app.decision

import com.echo.app.logging.EchoLog
import com.echo.app.safety.SafetyController
import org.json.JSONArray
import org.json.JSONObject

class DecisionEngine(private val safety: SafetyController) {
    fun parse(modelContent: String): AiResult {
        val parsed = runCatching { JSONObject(modelContent) }.getOrNull()
        if (parsed == null) return AiResult(text = modelContent, actions = emptyList())

        val text = parsed.optString("text", modelContent)
        val actionsJson = parsed.optJSONArray("actions") ?: JSONArray()
        val actions = buildList {
            for (i in 0 until actionsJson.length()) {
                val obj = actionsJson.optJSONObject(i) ?: continue
                add(
                    EchoAction(
                        type = obj.optString("type", "UNKNOWN"),
                        payload = obj.optJSONObject("payload")
                    )
                )
            }
        }
        return AiResult(text = text, actions = actions)
    }

    fun dispatchActions(actions: List<EchoAction>) {
        actions.forEach { action ->
            if (safety.requiresConfirmation(action.type)) {
                EchoLog.w("Blocked action requiring confirmation: ${action.type}")
                return@forEach
            }
            EchoLog.i("Action queued: ${action.type} payload=${action.payload}")
        }
    }
}
