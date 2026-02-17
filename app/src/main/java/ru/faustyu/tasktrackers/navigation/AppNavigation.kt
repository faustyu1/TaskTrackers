package ru.faustyu.tasktrackers.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.faustyu.tasktrackers.ui.screens.SettingsScreen
import ru.faustyu.tasktrackers.ui.screens.TaskDetailScreen
import ru.faustyu.tasktrackers.ui.screens.TaskListScreen
import ru.faustyu.tasktrackers.viewmodel.SettingsViewModel
import ru.faustyu.tasktrackers.viewmodel.TaskListViewModel

object Routes {
    const val TASK_LIST = "task_list"
    const val TASK_DETAIL = "task_detail/{taskId}"
    const val SETTINGS = "settings"

    fun taskDetail(taskId: Long) = "task_detail/$taskId"
}

@Composable
fun AppNavigation(
    taskListViewModel: TaskListViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val navController = rememberNavController()
    val uiState by taskListViewModel.uiState.collectAsStateWithLifecycle()
    val currentLocale by settingsViewModel.currentLocale.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val systemLocale = context.resources.configuration.locales[0].language
    val isRussian = when (currentLocale) {
        "ru" -> true
        "en" -> false
        else -> systemLocale == "ru"
    }

    NavHost(
        navController = navController,
        startDestination = Routes.TASK_LIST,
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(350)
            ) + fadeIn(animationSpec = tween(350))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 3 },
                animationSpec = tween(350)
            ) + fadeOut(animationSpec = tween(200))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 3 },
                animationSpec = tween(350)
            ) + fadeIn(animationSpec = tween(350))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(350)
            ) + fadeOut(animationSpec = tween(200))
        }
    ) {
        composable(Routes.TASK_LIST) {
            TaskListScreen(
                uiState = uiState,
                isRussian = isRussian,
                onToggleComplete = { taskListViewModel.toggleTaskCompletion(it) },
                onDeleteTask = { taskListViewModel.deleteTask(it) },
                onTaskClick = { taskId -> navController.navigate(Routes.taskDetail(taskId)) },
                onAddTask = { title, desc, tagIds -> taskListViewModel.addTask(title, desc, tagIds) },
                onAddCustomTag = { name, nameRu, group, color -> taskListViewModel.addCustomTag(name, nameRu, group, color) },
                onToggleFilterTag = { taskListViewModel.toggleFilterTag(it) },
                onClearFilters = { taskListViewModel.clearFilters() },
                onSetSortMode = { taskListViewModel.setSortMode(it) },
                onSearchQueryChange = { taskListViewModel.setSearchQuery(it) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(
            route = Routes.TASK_DETAIL,
            arguments = listOf(navArgument("taskId") { type = NavType.LongType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getLong("taskId") ?: return@composable

            val taskWithTags by taskListViewModel.repository.getTaskWithTagsById(taskId)
                .collectAsStateWithLifecycle(initialValue = null)

            TaskDetailScreen(
                taskWithTags = taskWithTags,
                allTags = uiState.allTags,
                isRussian = isRussian,
                onToggleComplete = {
                    taskWithTags?.let { taskListViewModel.toggleTaskCompletion(it) }
                },
                onUpdateTask = { title, desc, tagIds ->
                    taskListViewModel.updateTask(taskId, title, desc, tagIds)
                },
                onAddCustomTag = { name, nameRu, group, color ->
                    taskListViewModel.addCustomTag(name, nameRu, group, color)
                },
                onDelete = {
                    taskListViewModel.deleteTask(
                        taskWithTags ?: return@TaskDetailScreen
                    )
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                currentLocale = currentLocale,
                onSetLocale = { settingsViewModel.setLocale(it) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
