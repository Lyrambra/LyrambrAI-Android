package com.aiagent.app.ui.navigation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aiagent.app.ui.screens.*
import com.aiagent.app.viewmodel.SettingsViewModel

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Chat : Screen("chat")
    object Tasks : Screen("tasks")
    object Settings : Screen("settings")
    object AddTask : Screen("add_task/{taskId}") {
        fun createRoute(taskId: Long = -1L) = "add_task/$taskId"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val settingsViewModel: SettingsViewModel = viewModel()
    val isFirstLaunch by settingsViewModel.isFirstLaunch.collectAsState(initial = true)
    val context = LocalContext.current

    LaunchedEffect(isFirstLaunch) {
        if (isFirstLaunch) {
            navController.navigate(Screen.Onboarding.route) {
                popUpTo(0)
            }
        } else {
            navController.navigate(Screen.Chat.route) {
                popUpTo(0)
            }
        }
    }

    NavHost(navController = navController, startDestination = Screen.Chat.route) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Chat.route) {
                        popUpTo(0)
                    }
                },
                onExit = {
                    (context as? Activity)?.finish()
                }
            )
        }
        composable(Screen.Chat.route) {
            ChatScreen(
                onNavigateToTasks = { navController.navigate(Screen.Tasks.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Tasks.route) {
            TasksScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddTask = { navController.navigate(Screen.AddTask.createRoute(-1L)) },
                onEditTask = { taskId -> navController.navigate(Screen.AddTask.createRoute(taskId)) }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AddTask.route) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")?.toLongOrNull() ?: -1L
            AddTaskScreen(
                taskId = taskId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
