package ru.faustyu.tasktrackers.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.faustyu.tasktrackers.R
import ru.faustyu.tasktrackers.data.model.TagGroup
import ru.faustyu.tasktrackers.data.model.TaskWithTags
import ru.faustyu.tasktrackers.ui.components.FilterBottomSheet
import ru.faustyu.tasktrackers.ui.components.SortBottomSheet
import ru.faustyu.tasktrackers.ui.components.TaskCard
import ru.faustyu.tasktrackers.viewmodel.SortMode
import ru.faustyu.tasktrackers.viewmodel.TaskListUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    uiState: TaskListUiState,
    isRussian: Boolean,
    onToggleComplete: (TaskWithTags) -> Unit,
    onDeleteTask: (TaskWithTags) -> Unit,
    onTaskClick: (Long) -> Unit,
    onAddTask: (String, String, List<Long>) -> Unit,
    onAddCustomTag: (String, String, TagGroup, String) -> Unit,
    onToggleFilterTag: (Long) -> Unit,
    onClearFilters: () -> Unit,
    onSetSortMode: (SortMode) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var showCreateSheet by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }
    var searchActive by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    AnimatedContent(
                        targetState = searchActive,
                        label = "searchAnim"
                    ) { isSearching ->
                        if (isSearching) {
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = onSearchQueryChange,
                                placeholder = { Text(stringResource(R.string.search_tasks)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(28.dp),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = { focusManager.clearFocus() }
                                ),
                                trailingIcon = {
                                    IconButton(onClick = {
                                        searchActive = false
                                        onSearchQueryChange("")
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                                    }
                                }
                            )
                        } else {
                            Text(stringResource(R.string.app_name))
                        }
                    }
                },
                actions = {
                    if (!searchActive) {
                        IconButton(onClick = { searchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_tasks))
                        }
                    }
                    BadgedBox(
                        badge = {
                            if (uiState.selectedFilterTags.isNotEmpty()) {
                                Badge {
                                    Text("${uiState.selectedFilterTags.size}")
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = stringResource(R.string.filter_by_tags))
                        }
                    }
                    IconButton(onClick = { showSortSheet = true }) {
                        Icon(Icons.Default.Sort, contentDescription = stringResource(R.string.sort_by))
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.settings))
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateSheet = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.new_task)) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.tasks.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Outlined.TaskAlt,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (uiState.selectedFilterTags.isNotEmpty() || uiState.searchQuery.isNotBlank())
                            stringResource(R.string.no_matching_tasks)
                        else
                            stringResource(R.string.no_tasks_yet),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (uiState.selectedFilterTags.isNotEmpty() || uiState.searchQuery.isNotBlank())
                            stringResource(R.string.try_different_filters)
                        else
                            stringResource(R.string.tap_plus_to_create),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Task counter
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.task_count, uiState.tasks.size),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val completedCount = uiState.tasks.count { it.task.isCompleted }
                        if (completedCount > 0) {
                            Text(
                                text = stringResource(R.string.completed_count, completedCount),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                items(
                    items = uiState.tasks,
                    key = { it.task.taskId }
                ) { taskWithTags ->
                    TaskCard(
                        taskWithTags = taskWithTags,
                        isRussian = isRussian,
                        onToggleComplete = { onToggleComplete(taskWithTags) },
                        onDelete = { onDeleteTask(taskWithTags) },
                        onClick = { onTaskClick(taskWithTags.task.taskId) },
                        modifier = Modifier.animateItem(
                            fadeInSpec = tween(300),
                            fadeOutSpec = tween(300),
                            placementSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    )
                }

                // Bottom spacer for FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Bottom sheets
    if (showCreateSheet) {
        TaskEditSheet(
            allTags = uiState.allTags,
            isRussian = isRussian,
            onSave = { title, desc, tagIds ->
                onAddTask(title, desc, tagIds)
                showCreateSheet = false
            },
            onAddCustomTag = onAddCustomTag,
            onDismiss = { showCreateSheet = false }
        )
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            allTags = uiState.allTags,
            selectedTagIds = uiState.selectedFilterTags,
            isRussian = isRussian,
            onToggleTag = onToggleFilterTag,
            onClear = onClearFilters,
            onDismiss = { showFilterSheet = false }
        )
    }

    if (showSortSheet) {
        SortBottomSheet(
            currentSort = uiState.sortMode,
            onSelectSort = {
                onSetSortMode(it)
                showSortSheet = false
            },
            onDismiss = { showSortSheet = false }
        )
    }
}
