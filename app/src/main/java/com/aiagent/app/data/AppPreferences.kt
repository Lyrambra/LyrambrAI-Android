package com.aiagent.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "app_preferences")

class AppPreferences(private val context: Context) {

    private object Keys {
        val API_KEY = stringPreferencesKey("api_key")
        val API_URL = stringPreferencesKey("api_url")
        val MODEL_NAME = stringPreferencesKey("model_name")
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val SHOW_THINKING = booleanPreferencesKey("show_thinking")
        val SYSTEM_PROMPT = stringPreferencesKey("system_prompt")
    }

    val apiKey: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[Keys.API_KEY] ?: "" }

    val apiUrl: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[Keys.API_URL] ?: "api.deepseek.com" }

    val modelName: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[Keys.MODEL_NAME] ?: "deepseek-chat" }

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[Keys.IS_FIRST_LAUNCH] ?: true }

    val showThinking: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[Keys.SHOW_THINKING] ?: false }

    val systemPrompt: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[Keys.SYSTEM_PROMPT] ?: "你是一个有帮助的AI助手。"
        }

    suspend fun setApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.API_KEY] = apiKey
        }
    }

    suspend fun setApiUrl(apiUrl: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.API_URL] = apiUrl
        }
    }

    suspend fun setModelName(modelName: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.MODEL_NAME] = modelName
        }
    }

    suspend fun setFirstLaunchComplete() {
        context.dataStore.edit { preferences ->
            preferences[Keys.IS_FIRST_LAUNCH] = false
        }
    }

    suspend fun setShowThinking(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SHOW_THINKING] = show
        }
    }

    suspend fun setSystemPrompt(prompt: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SYSTEM_PROMPT] = prompt
        }
    }
}
