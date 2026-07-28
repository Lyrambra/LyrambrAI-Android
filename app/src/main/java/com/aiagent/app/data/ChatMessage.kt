package com.aiagent.app.data

data class ChatMessage(
    val id: String = System.currentTimeMillis().toString(),
    val role: String,
    val content: String,
    val thinkingContent: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
