package com.aiagent.app.scheduler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.aiagent.app.MainActivity
import com.aiagent.app.data.AppPreferences
import com.aiagent.app.data.RepeatInterval
import com.aiagent.app.network.ChatMessageData
import com.aiagent.app.network.OpenAiApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TaskAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra("task_id", -1)
        if (taskId == -1L) return

        // 使用 goAsync 让 BroadcastReceiver 在协程执行期间保持存活
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val taskScheduler = TaskScheduler(context)
                val task = taskScheduler.getTaskById(taskId) ?: return@launch

                // 读取 API 配置并调用 API
                val preferences = AppPreferences(context)
                val apiKey = preferences.apiKey.first()
                val apiUrl = preferences.apiUrl.first()
                val modelName = preferences.modelName.first()
                val systemPrompt = preferences.systemPrompt.first()

                var resultText = task.prompt
                if (apiKey.isNotBlank() && apiUrl.isNotBlank()) {
                    try {
                        val apiClient = OpenAiApiClient()
                        val result = apiClient.chatCompletion(
                            apiKey = apiKey,
                            apiUrl = apiUrl,
                            model = modelName,
                            messages = listOf(ChatMessageData(role = "user", content = task.prompt)),
                            systemPrompt = systemPrompt
                        )
                        resultText = result.content
                    } catch (e: Exception) {
                        resultText = "API调用失败: ${e.message}"
                    }
                }

                showNotification(context, task.name, resultText)

                // 调度下一次触发
                when (task.repeatInterval) {
                    RepeatInterval.DAILY -> {
                        val nextTime = task.timeInMillis + 24 * 60 * 60 * 1000
                        taskScheduler.updateTask(task.copy(timeInMillis = nextTime))
                    }
                    RepeatInterval.WEEKLY -> {
                        val nextTime = task.timeInMillis + 7 * 24 * 60 * 60 * 1000
                        taskScheduler.updateTask(task.copy(timeInMillis = nextTime))
                    }
                    RepeatInterval.MONTHLY -> {
                        val nextTime = task.timeInMillis + 30 * 24 * 60 * 60 * 1000
                        taskScheduler.updateTask(task.copy(timeInMillis = nextTime))
                    }
                    RepeatInterval.NONE -> {
                        taskScheduler.updateTask(task.copy(isEnabled = false))
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, title: String, content: String) {
        val channelId = "task_notifications"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "定时任务提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "定时任务触发时的通知"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val resultIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content.take(100))
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
