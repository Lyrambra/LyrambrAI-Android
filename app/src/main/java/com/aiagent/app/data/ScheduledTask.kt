package com.aiagent.app.data

data class ScheduledTask(
    val id: Long = 0,
    val name: String,
    val prompt: String,
    val timeInMillis: Long,
    val repeatInterval: RepeatInterval = RepeatInterval.NONE,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

enum class RepeatInterval {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY
}
