package com.echo.app.ai

import android.content.Context
import com.echo.app.BuildConfig
import com.echo.app.logging.EchoLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class GroqApiClient(private val context: Context) {
    suspend fun chat(userInput: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GROQ_API_KEY
        require(apiKey.isNotBlank()) { "Missing GROQ_API_KEY (set in build.gradle or local.properties)" }

        val url = URL("https://api.groq.com/openai/v1/chat/completions")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Authorization", "Bearer $apiKey")
            setRequestProperty("Content-Type", "application/json")
            connectTimeout = 20_000
            readTimeout = 30_000
            doOutput = true
        }

        val body = JSONObject()
            .put("model", BuildConfig.GROQ_MODEL)
            .put(
                "messages",
                JSONArray()
                    .put(
                        JSONObject()
                            .put("role", "system")
                            .put(
                                "content",
                                "You are ECHO, an Android AI assistant. Return a single JSON object: {\"text\":\"...\",\"actions\":[{\"type\":\"...\",\"payload\":{...}}]}."
                            )
                    )
                    .put(
                        JSONObject()
                            .put("role", "user")
                            .put("content", userInput)
                    )
            )
            .put("temperature", 0.7)
            .toString()

        conn.outputStream.use { os ->
            os.write(body.toByteArray(Charsets.UTF_8))
        }

        val status = conn.responseCode
        val reader = if (status in 200..299) conn.inputStream else conn.errorStream
        val raw = BufferedReader(InputStreamReader(reader)).use { it.readText() }
        if (status !in 200..299) {
            EchoLog.e("Groq HTTP $status: $raw")
            error("Groq API error ($status)")
        }

        val json = JSONObject(raw)
        val choices = json.optJSONArray("choices") ?: JSONArray()
        val message = choices.optJSONObject(0)?.optJSONObject("message")
        message?.optString("content")?.trim().orEmpty()
    }
}
