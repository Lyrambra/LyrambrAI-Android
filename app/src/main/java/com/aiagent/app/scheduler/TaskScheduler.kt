package com.aiagent.app.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.aiagent.app.data.RepeatInterval
import com.aiagent.app.data.ScheduledTask

class TaskScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val prefs = context.getSharedPreferences("scheduled_tasks", Context.MODE_PRIVATE)

    suspend fun scheduleTask(task: ScheduledTask): Long {
        val id = saveTask(task)
        val taskWithId = task.copy(id = id)
        
        if (task.isEnabled) {
            setAlarm(taskWithId)
        }
        
        return id
    }

    suspend fun updateTask(task: ScheduledTask) {
        saveTask(task)
        cancelAlarm(task.id)
        if (task.isEnabled) {
            setAlarm(task)
        }
    }

    suspend fun cancelTask(taskId: Long) {
        deleteTask(taskId)
        cancelAlarm(taskId)
    }

    suspend fun getAllTasks(): List<ScheduledTask> {
        val tasks = mutableListOf<ScheduledTask>()
        val allEntries = prefs.all
        for (entry in allEntries) {
            if (entry.key.startsWith("task_")) {
                val json = entry.value as? String ?: continue
                parseTask(json)?.let { tasks.add(it) }
            }
        }
        return tasks.sortedBy { it.timeInMillis }
    }

    suspend fun getTaskById(taskId: Long): ScheduledTask? {
        val json = prefs.getString("task_$taskId", null) ?: return null
        return parseTask(json)
    }

    private fun saveTask(task: ScheduledTask): Long {
        val id = if (task.id == 0L) {
            val nextId = prefs.getLong("next_task_id", 1)
            prefs.edit().putLong("next_task_id", nextId + 1).apply()
            nextId
        } else {
            task.id
        }
        
        val taskToSave = task.copy(id = id)
        prefs.edit().putString("task_$id", serializeTask(taskToSave)).apply()
        return id
    }

    private fun deleteTask(taskId: Long) {
        prefs.edit().remove("task_$taskId").apply()
    }

    private fun setAlarm(task: ScheduledTask) {
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            putExtra("task_id", task.id)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        var triggerTime = task.timeInMillis
        if (triggerTime < System.currentTimeMillis()) {
            triggerTime = when (task.repeatInterval) {
                RepeatInterval.DAILY -> triggerTime + AlarmManager.INTERVAL_DAY
                RepeatInterval.WEEKLY -> triggerTime + AlarmManager.INTERVAL_DAY * 7
                RepeatInterval.MONTHLY -> triggerTime + AlarmManager.INTERVAL_DAY * 30
                RepeatInterval.NONE -> System.currentTimeMillis() + 60000
            }
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }

    private fun cancelAlarm(taskId: Long) {
        val intent = Intent(context, TaskAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun serializeTask(task: ScheduledTask): String {
        return "${task.id}|${task.name}|${task.prompt}|${task.timeInMillis}|${task.repeatInterval.name}|${task.isEnabled}|${task.createdAt}"
    }

    private fun parseTask(json: String): ScheduledTask? {
        return try {
            val parts = json.split("|")
            ScheduledTask(
                id = parts[0].toLong(),
                name = parts[1],
                prompt = parts[2],
                timeInMillis = parts[3].toLong(),
                repeatInterval = RepeatInterval.valueOf(parts[4]),
                isEnabled = parts[5].toBoolean(),
                createdAt = parts[6].toLong()
            )
        } catch (e: Exception) {
            null
        }
    }
}
