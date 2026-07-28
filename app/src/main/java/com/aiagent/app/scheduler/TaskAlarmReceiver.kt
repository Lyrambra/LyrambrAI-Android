package com.aiagent.app.scheduler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.aiagent.app.MainActivity
import com.aiagent.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra("task_id", -1)
        if (taskId == -1L) return

        CoroutineScope(Dispatchers.IO).launch {
            val taskScheduler = TaskScheduler(context)
            val task = taskScheduler.getTaskById(taskId) ?: return@launch

            showNotification(context, task.name, task.prompt)

            when (task.repeatInterval) {
                com.aiagent.app.data.RepeatInterval.DAILY -> {
                    val nextTime = task.timeInMillis + 24 * 60 * 60 * 1000
                    taskScheduler.updateTask(task.copy(timeInMillis = nextTime))
                }
                com.aiagent.app.data.RepeatInterval.WEEKLY -> {
                    val nextTime = task.timeInMillis + 7 * 24 * 60 * 60 * 1000
                    taskScheduler.updateTask(task.copy(timeInMillis = nextTime))
                }
                com.aiagent.app.data.RepeatInterval.MONTHLY -> {
                    val nextTime = task.timeInMillis + 30 * 24 * 60 * 60 * 1000
                    taskScheduler.updateTask(task.copy(timeInMillis = nextTime))
                }
                com.aiagent.app.data.RepeatInterval.NONE -> {
                    taskScheduler.updateTask(task.copy(isEnabled = false))
                }
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
