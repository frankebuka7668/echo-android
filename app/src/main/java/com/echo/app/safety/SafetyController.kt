package com.echo.app.safety

import android.content.Context
import android.content.SharedPreferences

class SafetyController(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("echo_safety", Context.MODE_PRIVATE)

    fun isSilentModeEnabled(): Boolean {
        return prefs.getBoolean("silent_mode", false)
    }

    fun setSilentModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("silent_mode", enabled).apply()
    }

    fun requiresConfirmation(actionType: String): Boolean {
        if (actionType.equals("UNKNOWN", ignoreCase = true)) return true
        return when (actionType.uppercase()) {
            "ANSWER_CALL",
            "DECLINE_CALL",
            "WHATSAPP_ANSWER_CALL",
            "WHATSAPP_DECLINE_CALL",
            "WHATSAPP_SEND_MESSAGE",
            "CLICK",
            "TYPE",
            "OPEN_APP",
            "SCREEN_CAPTURE" -> true
            else -> false
        }
    }
}
