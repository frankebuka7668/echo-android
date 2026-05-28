package com.echo.app.decision

data class AiResult(
    val text: String,
    val actions: List<EchoAction>
)
