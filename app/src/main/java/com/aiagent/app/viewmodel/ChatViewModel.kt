package com.aiagent.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aiagent.app.data.AppPreferences
import com.aiagent.app.data.ChatMessage
import com.aiagent.app.network.ChatMessageData
import com.aiagent.app.network.OpenAiApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = AppPreferences(application)
    private val apiClient = OpenAiApiClient()

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
            val modelName = preferences.modelName.first()

            if (apiKey.isBlank()) {
                val errorMessage = ChatMessage(
                    role = "assistant",
                    content = "请先在设置中配置API密钥。"
                )
                _messages.value = _messages.value + errorMessage
                _isLoading.value = false
                return@launch
            }

            // 创建一个占位的助手消息，用于流式更新
            val assistantMessageId = System.currentTimeMillis().toString()
            val assistantMessage = ChatMessage(
                id = assistantMessageId,
                role = "assistant",
                content = "",
                thinkingContent = null
            )
            _messages.value = _messages.value + assistantMessage

            try {
                val messageHistory = _messages.value
                    .filter { it.role == "user" || it.role == "assistant" }
                    .dropLast(1) // 移除占位消息
                    .map { ChatMessageData(role = it.role, content = it.content) }

                val contentBuilder = StringBuilder()
                val thinkingBuilder = StringBuilder()

                apiClient.streamChatCompletion(
                    apiKey = apiKey,
                    apiUrl = apiUrl,
                    model = modelName,
                    messages = messageHistory,
                    systemPrompt = systemPrompt
                ).collect { chunk ->
                    if (chunk.content.isNotEmpty()) {
                        contentBuilder.append(chunk.content)
                    }
                    if (chunk.thinkingContent != null) {
                        thinkingBuilder.append(chunk.thinkingContent)
                    }

                    // 实时更新占位消息
                    _messages.value = _messages.value.map { msg ->
                        if (msg.id == assistantMessageId) {
                            msg.copy(
                                content = contentBuilder.toString(),
                                thinkingContent = if (thinkingBuilder.isNotEmpty()) thinkingBuilder.toString() else null
                            )
                        } else {
                            msg
                        }
                    }
                }
            } catch (e: Exception) {
                // 如果出错，更新占位消息为错误信息
                _messages.value = _messages.value.map { msg ->
                    if (msg.id == assistantMessageId) {
                        msg.copy(
                            content = if (msg.content.isBlank()) {
                                "请求失败: ${e.message}"
                            } else {
                                msg.content + "\n\n[请求中断: ${e.message}]"
                            }
                        )
                    } else {
                        msg
                    }
                }
            }

            _isLoading.value = false
        }
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
