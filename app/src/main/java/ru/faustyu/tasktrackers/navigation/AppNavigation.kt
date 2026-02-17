package ru.faustyu.tasktrackers.navigation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import kotlinx.coroutines.launch
import ru.faustyu.tasktrackers.ui.screens.*
import ru.faustyu.tasktrackers.viewmodel.SettingsViewModel
import ru.faustyu.tasktrackers.viewmodel.TaskListViewModel

object Routes {
    const val TASK_LIST = "task_list"
    const val TASK_DETAIL = "task_detail/{taskId}"
    const val SETTINGS = "settings"
    const val ARCHIVE = "archive"
    const val STATISTICS = "statistics"
    const val ONBOARDING = "onboarding"

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
    val hasSeenOnboarding by settingsViewModel.hasSeenOnboarding.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val systemLocale = context.resources.configuration.locales[0].language
    val isRussian = when (currentLocale) {
        "ru" -> true
        "en" -> false
        else -> systemLocale == "ru"
    }

    // Register activity result launchers HERE (before NavHost) so LocalActivityResultRegistryOwner is available
    val scope = rememberCoroutineScope()
    var showExportSuccess by remember { mutableStateOf(false) }
    var showImportSuccess by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val json = taskListViewModel.exportToJson()
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(json.toByteArray())
                }
                showExportSuccess = true
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            taskListViewModel.importFromJson(context, uri)
            showImportSuccess = true
        }
    }

    if (hasSeenOnboarding == null) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        return
    }

    NavHost(
        navController = navController,
        startDestination = if (hasSeenOnboarding == true) Routes.TASK_LIST else Routes.ONBOARDING,
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
                onTaskClick = { taskId -> navController.navigate(Routes.taskDetail(taskId)) },
                onAddTask = { title, desc, tagIds, dueDate, colorHex, repeatMode ->
                    taskListViewModel.addTask(title, desc, tagIds, dueDate, colorHex, repeatMode)
                },
                onAddCustomTag = { name, group, color -> taskListViewModel.addCustomTag(name, group, color) },
                onToggleFilterTag = { taskListViewModel.toggleFilterTag(it) },
                onClearFilters = { taskListViewModel.clearFilters() },
                onSetSortMode = { taskListViewModel.setSortMode(it) },
                onSearchQueryChange = { taskListViewModel.setSearchQuery(it) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToArchive = { navController.navigate(Routes.ARCHIVE) },
                onNavigateToStatistics = { navController.navigate(Routes.STATISTICS) },
                onArchiveTask = { taskListViewModel.archiveTask(it.task.taskId) }
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
                onUpdateTask = { title, desc, tagIds, dueDate, colorHex, repeatMode ->
                    taskListViewModel.updateTask(taskId, title, desc, tagIds, dueDate, colorHex, repeatMode)
                },
                onAddCustomTag = { name, group, color ->
                    taskListViewModel.addCustomTag(name, group, color)
                },
                onDelete = {
                    taskListViewModel.deleteTask(
                        taskWithTags ?: return@TaskDetailScreen
                    )
                    navController.popBackStack()
                },
                onArchive = {
                    taskWithTags?.let {
                        taskListViewModel.archiveTask(it.task.taskId)
                        navController.popBackStack()
                    }
                },
                onAddSubtask = { tId, title -> taskListViewModel.addSubtask(tId, title) },
                onToggleSubtask = { taskListViewModel.toggleSubtask(it) },
                onDeleteSubtask = { taskListViewModel.deleteSubtask(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            val currentTheme by settingsViewModel.currentTheme.collectAsStateWithLifecycle()
            SettingsScreen(
                currentLocale = currentLocale,
                currentTheme = currentTheme,
                onSetLocale = { settingsViewModel.setLocale(it) },
                onSetTheme = { settingsViewModel.setTheme(it) },
                onExportClick = { exportLauncher.launch("tasktrackers_backup.json") },
                onImportClick = { importLauncher.launch(arrayOf("application/json")) },
                showExportSuccess = showExportSuccess,
                showImportSuccess = showImportSuccess,
                onDismissExportSuccess = { showExportSuccess = false },
                onDismissImportSuccess = { showImportSuccess = false },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ARCHIVE) {
            val archivedTasks by taskListViewModel.archivedTasks.collectAsStateWithLifecycle()
            ArchiveScreen(
                archivedTasks = archivedTasks,
                isRussian = isRussian,
                onUnarchive = { taskListViewModel.unarchiveTask(it) },
                onDelete = { taskListViewModel.deleteTask(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.STATISTICS) {
            val archivedTasks by taskListViewModel.archivedTasks.collectAsStateWithLifecycle()
            StatisticsScreen(
                allTasks = uiState.tasks,
                archivedTasks = archivedTasks,
                isRussian = isRussian,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinish = {
                    settingsViewModel.setOnboardingSeen()
                    navController.navigate(Routes.TASK_LIST) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
    }
}

