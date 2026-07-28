package com.aiagent.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aiagent.app.data.ScheduledTask
import com.aiagent.app.scheduler.TaskScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TasksViewModel(application: Application) : AndroidViewModel(application) {
    private val taskScheduler = TaskScheduler(application)

    private val _tasks = MutableStateFlow<List<ScheduledTask>>(emptyList())
    val tasks: StateFlow<List<ScheduledTask>> = _tasks.asStateFlow()

    private val _selectedTask = MutableStateFlow<ScheduledTask?>(null)
    val selectedTask: StateFlow<ScheduledTask?> = _selectedTask.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            _tasks.value = taskScheduler.getAllTasks()
        }
    }

    fun loadTaskById(taskId: Long) {
        viewModelScope.launch {
            _selectedTask.value = taskScheduler.getTaskById(taskId)
        }
    }

    fun saveTask(task: ScheduledTask) {
        viewModelScope.launch {
            if (task.id == 0L) {
                taskScheduler.scheduleTask(task)
            } else {
                taskScheduler.updateTask(task)
            }
            loadTasks()
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            taskScheduler.cancelTask(taskId)
            loadTasks()
        }
    }

    fun toggleTaskEnabled(taskId: Long, enabled: Boolean) {
        viewModelScope.launch {
            val task = _tasks.value.find { it.id == taskId }
            if (task != null) {
                taskScheduler.updateTask(task.copy(isEnabled = enabled))
                loadTasks()
            }
        }
    }
}
