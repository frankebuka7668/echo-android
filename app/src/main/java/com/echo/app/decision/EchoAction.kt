package com.echo.app.decision

import org.json.JSONObject

data class EchoAction(
    val type: String,
    val payload: JSONObject?
)
