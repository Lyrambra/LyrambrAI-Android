package com.aiagent.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aiagent.app.data.AppPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = AppPreferences(application)

    val apiKey: Flow<String> = preferences.apiKey
    val apiUrl: Flow<String> = preferences.apiUrl
    val modelName: Flow<String> = preferences.modelName
    val isFirstLaunch: Flow<Boolean> = preferences.isFirstLaunch
    val showThinking: Flow<Boolean> = preferences.showThinking
    val systemPrompt: Flow<String> = preferences.systemPrompt

    fun saveApiKey(key: String) {
        viewModelScope.launch {
            preferences.setApiKey(key)
        }
    }

    fun saveApiUrl(url: String) {
        viewModelScope.launch {
            preferences.setApiUrl(url)
        }
    }

    fun saveModelName(model: String) {
        viewModelScope.launch {
            preferences.setModelName(model)
        }
    }

    fun completeFirstLaunch() {
        viewModelScope.launch {
            preferences.setFirstLaunchComplete()
        }
    }

    fun setShowThinking(show: Boolean) {
        viewModelScope.launch {
            preferences.setShowThinking(show)
        }
    }

    fun saveSystemPrompt(prompt: String) {
        viewModelScope.launch {
            preferences.setSystemPrompt(prompt)
        }
    }
}
