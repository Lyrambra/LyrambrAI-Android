package com.aiagent.app.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

/**
 * OpenAI 兼容 API 客户端
 * 参考 Cherry Studio 实现，支持流式 SSE 响应和 reasoning_content 思考过程
 */
class OpenAiApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * 流式聊天补全请求
     * 返回 Flow，逐块推送 [StreamChunk]
     */
    fun streamChatCompletion(
        apiKey: String,
        apiUrl: String,
        model: String,
        messages: List<ChatMessageData>,
        temperature: Float = 0.7f,
        maxTokens: Int? = null,
        systemPrompt: String = ""
    ): Flow<StreamChunk> = flow {
        require(apiKey.isNotBlank()) { "API密钥不能为空" }
        require(apiUrl.isNotBlank()) { "API地址不能为空" }
        require(model.isNotBlank()) { "模型名称不能为空" }

        val fullUrl = buildApiUrl(apiUrl)

        val messagesArray = JSONArray()
        if (systemPrompt.isNotBlank()) {
            messagesArray.put(JSONObject().apply {
                put("role", "system")
                put("content", systemPrompt)
            })
        }
        for (msg in messages) {
            messagesArray.put(JSONObject().apply {
                put("role", msg.role)
                put("content", msg.content)
            })
        }

        val requestBody = JSONObject().apply {
            put("model", model)
            put("messages", messagesArray)
            put("stream", true)
            put("temperature", temperature)
            if (maxTokens != null) {
                put("max_tokens", maxTokens)
            }
        }

        val request = Request.Builder()
            .url(fullUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "text/event-stream")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                throw IOException("API请求失败: ${response.code} ${response.message}\n$errorBody")
            }

            val body = response.body ?: throw IOException("响应体为空")
            val reader = BufferedReader(InputStreamReader(body.byteStream(), Charsets.UTF_8))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (line.isNullOrBlank()) continue
                // 兼容 "data:" 和 "data: " 两种格式
                if (!line.startsWith("data:")) continue

                val data = line.removePrefix("data:").trim()
                if (data == "[DONE]") {
                    emit(StreamChunk(content = "", thinkingContent = null, isDone = true))
                    break
                }

                try {
                    val json = JSONObject(data)
                    val choices = json.optJSONArray("choices")
                    if (choices != null && choices.length() > 0) {
                        val delta = choices.getJSONObject(0).optJSONObject("delta")
                        if (delta != null) {
                            val content = delta.optString("content", "")
                            val reasoningContent = delta.optString("reasoning_content", "")
                            if (content.isNotEmpty() || reasoningContent.isNotEmpty()) {
                                emit(StreamChunk(
                                    content = content,
                                    thinkingContent = if (reasoningContent.isNotEmpty()) reasoningContent else null,
                                    isDone = false
                                ))
                            }
                        }
                    }
                } catch (e: Exception) {
                    // 跳过无法解析的行
                }
            }
            reader.close()
        } catch (e: IOException) {
            throw IOException("网络请求失败: ${e.message}", e)
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 非流式聊天补全请求（用于定时任务等不需要流式的场景）
     */
    suspend fun chatCompletion(
        apiKey: String,
        apiUrl: String,
        model: String,
        messages: List<ChatMessageData>,
        temperature: Float = 0.7f,
        maxTokens: Int? = null,
        systemPrompt: String = ""
    ): ChatResult = withContext(Dispatchers.IO) {
        require(apiKey.isNotBlank()) { "API密钥不能为空" }
        require(apiUrl.isNotBlank()) { "API地址不能为空" }
        require(model.isNotBlank()) { "模型名称不能为空" }

        val fullUrl = buildApiUrl(apiUrl)

        val messagesArray = JSONArray()
        if (systemPrompt.isNotBlank()) {
            messagesArray.put(JSONObject().apply {
                put("role", "system")
                put("content", systemPrompt)
            })
        }
        for (msg in messages) {
            messagesArray.put(JSONObject().apply {
                put("role", msg.role)
                put("content", msg.content)
            })
        }

        val requestBody = JSONObject().apply {
            put("model", model)
            put("messages", messagesArray)
            put("stream", false)
            put("temperature", temperature)
            if (maxTokens != null) {
                put("max_tokens", maxTokens)
            }
        }

        val request = Request.Builder()
            .url(fullUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body!!.string()
            if (!response.isSuccessful) {
                throw IOException("API请求失败: ${response.code} ${response.message}\n$responseBody")
            }

            val json = JSONObject(responseBody)
            val choices = json.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val message = choices.getJSONObject(0).getJSONObject("message")
                val content = message.optString("content", "")
                val reasoningContent = message.optString("reasoning_content", "")
                ChatResult(
                    content = content,
                    thinkingContent = if (reasoningContent.isNotBlank()) reasoningContent else null
                )
            } else {
                ChatResult(content = "未收到有效回复", thinkingContent = null)
            }
        } catch (e: IOException) {
            throw IOException("网络请求失败: ${e.message}", e)
        }
    }

    /**
     * 构建 API URL
     * 参考 Cherry Studio 的 formatApiHost / hasApiVersion 逻辑：
     * - 补全 https:// 协议前缀
     * - # 后缀表示禁止自动追加版本号（Cherry Studio 风格）
     * - 如果 URL 已包含版本路径（/v1, /v2beta 等），不重复追加
     * - 自动追加 /v1/chat/completions 路径
     */
    private fun buildApiUrl(apiUrl: String): String {
        var host = apiUrl.trim()
        if (host.isEmpty()) return host

        // Cherry Studio 风格：# 后缀表示禁止自动追加版本号
        val suppressVersion = host.endsWith("#")
        if (suppressVersion) {
            host = host.removeSuffix("#")
        }

        // 补全协议前缀
        if (!host.startsWith("http://") && !host.startsWith("https://")) {
            host = "https://$host"
        }

        // 移除末尾斜杠
        host = host.removeSuffix("/")

        // 判断是否已包含版本号路径（参考 Cherry Studio 的 hasApiVersion）
        val hasVersion = hasApiVersion(host)

        // 决定是否追加 /v1（参考 Cherry Studio 的 formatApiHost）
        val shouldAppendVersion = !suppressVersion && !hasVersion
        if (shouldAppendVersion) {
            host = "$host/v1"
        }

        // 追加 chat/completions 路径（如果尚未包含）
        return if (host.contains("/chat/completions")) {
            host
        } else {
            "$host/chat/completions"
        }
    }

    /**
     * 检查 URL 是否已包含 API 版本路径（如 /v1, /v2, /v1beta）
     * 参考 Cherry Studio 的 VERSION_REGEX: /v\d+(?:alpha|beta)?(?:\/|$)
     */
    private fun hasApiVersion(host: String): Boolean {
        val versionRegex = Regex("/v\\d+(?:alpha|beta)?(?:/|$)", RegexOption.IGNORE_CASE)
        return versionRegex.containsMatchIn(host)
    }
}

data class ChatMessageData(
    val role: String,
    val content: String
)

data class StreamChunk(
    val content: String,
    val thinkingContent: String?,
    val isDone: Boolean
)

data class ChatResult(
    val content: String,
    val thinkingContent: String?
)
