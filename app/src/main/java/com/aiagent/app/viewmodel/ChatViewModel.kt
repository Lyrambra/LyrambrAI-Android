package com.aiagent.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aiagent.app.data.AppPreferences
import com.aiagent.app.data.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = AppPreferences(application)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _showThinking = MutableStateFlow(false)
    val showThinking: StateFlow<Boolean> = _showThinking.asStateFlow()

    init {
        viewModelScope.launch {
            _showThinking.value = preferences.showThinking.first()
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            val userMessage = ChatMessage(
                role = "user",
                content = content
            )
            _messages.value = _messages.value + userMessage
            _isLoading.value = true

            val systemPrompt = preferences.systemPrompt.first()
            val apiKey = preferences.apiKey.first()
            val apiUrl = preferences.apiUrl.first()

            if (apiKey.isBlank()) {
                val errorMessage = ChatMessage(
                    role = "assistant",
                    content = "请先在设置中配置API密钥。"
                )
                _messages.value = _messages.value + errorMessage
                _isLoading.value = false
                return@launch
            }

            try {
                val response = callApi(apiKey, apiUrl, systemPrompt, _messages.value)
                val assistantMessage = ChatMessage(
                    role = "assistant",
                    content = response.content,
                    thinkingContent = response.thinkingContent
                )
                _messages.value = _messages.value + assistantMessage
            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    role = "assistant",
                    content = "请求失败: ${e.message}"
                )
                _messages.value = _messages.value + errorMessage
            }

            _isLoading.value = false
        }
    }

    private data class ApiResponse(
        val content: String,
        val thinkingContent: String?
    )

    private suspend fun callApi(
        apiKey: String,
        apiUrl: String,
        systemPrompt: String,
        messages: List<ChatMessage>
    ): ApiResponse {
        return ApiResponse(
            content = "这是一个演示回复。请配置有效的API密钥后使用。\n\n```kotlin\nfun hello() {\n    println(\"Hello, World!\")\n}\n```\n\n| 列1 | 列2 | 列3 |\n|-----|-----|-----|\n| A | B | C |\n| D | E | F |",
            thinkingContent = "这是思考过程的示例内容。模型正在分析用户的问题并生成回复..."
        )
    }

    fun toggleShowThinking() {
        viewModelScope.launch {
            val newValue = !_showThinking.value
            _showThinking.value = newValue
            preferences.setShowThinking(newValue)
        }
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }
}
