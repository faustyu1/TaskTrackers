package ru.faustyu.tasktrackers.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.faustyu.tasktrackers.R
import ru.faustyu.tasktrackers.data.model.*
import ru.faustyu.tasktrackers.ui.components.TagChip
import ru.faustyu.tasktrackers.ui.theme.parseTagColor
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskDetailScreen(
    taskWithTags: TaskWithTags?,
    allTags: List<Tag>,
    isRussian: Boolean,
    onToggleComplete: () -> Unit,
    onUpdateTask: (String, String, List<Long>, Long?, String?, String) -> Unit,
    onAddCustomTag: (String, TagGroup, String) -> Unit,
    onDelete: () -> Unit,
    onArchive: () -> Unit,
    onAddSubtask: (Long, String) -> Unit,
    onToggleSubtask: (Subtask) -> Unit,
    onDeleteSubtask: (Long) -> Unit,
    onBack: () -> Unit
) {
    var showEditSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var newSubtaskTitle by remember { mutableStateOf("") }
    var showSubtaskInput by remember { mutableStateOf(false) }

    if (taskWithTags == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val task = taskWithTags.task
    val tags = taskWithTags.tags
    val subtasks = taskWithTags.subtasks
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }
    val dueDateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.task_details)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { showEditSheet = true }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_task))
                    }
                    IconButton(onClick = onArchive) {
                        Icon(Icons.Default.Archive, contentDescription = stringResource(R.string.archive))
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete_task),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Task color indicator + Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (task.colorHex != null) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(parseTagColor(task.colorHex))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.headlineSmall,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (task.isCompleted)
                                stringResource(R.string.status_completed)
                            else stringResource(R.string.status_active),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        FilledTonalButton(onClick = onToggleComplete) {
                            Text(
                                if (task.isCompleted)
                                    stringResource(R.string.mark_active)
                                else stringResource(R.string.mark_completed)
                            )
                        }
                    }

                    // Due date
                    if (task.dueDate != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val isOverdue = task.dueDate < System.currentTimeMillis() && !task.isCompleted
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.deadline) + ": " + dueDateFormat.format(Date(task.dueDate)),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isOverdue) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (isOverdue) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.overdue),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    // Repeat mode
                    if (task.repeatMode != "none") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Repeat,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val repeatLabel = when (task.repeatMode) {
                                "daily" -> stringResource(R.string.repeat_daily)
                                "weekly" -> stringResource(R.string.repeat_weekly)
                                "monthly" -> stringResource(R.string.repeat_monthly)
                                else -> ""
                            }
                            Text(
                                text = repeatLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }

            // Description
            if (task.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.task_description),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Tags
            if (tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.select_tags),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tags.sortedBy { it.group.ordinal * 100 + it.sortOrder }
                        .forEach { tag ->
                            TagChip(
                                label = if (isRussian) tag.nameRu else tag.name,
                                color = parseTagColor(tag.colorHex)
                            )
                        }
                }
            }

            // Subtasks
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.subtasks),
                    style = MaterialTheme.typography.titleMedium
                )
                if (subtasks.isNotEmpty()) {
                    val completed = subtasks.count { it.isCompleted }
                    Text(
                        text = "$completed/${subtasks.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Subtask progress bar
            if (subtasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val progress = subtasks.count { it.isCompleted }.toFloat() / subtasks.size
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subtask list
            subtasks.sortedBy { it.sortOrder }.forEach { subtask ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = subtask.isCompleted,
                        onCheckedChange = { onToggleSubtask(subtask) }
                    )
                    Text(
                        text = subtask.title,
                        style = MaterialTheme.typography.bodyMedium,
                        textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (subtask.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(onClick = { onDeleteSubtask(subtask.subtaskId) }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.delete),
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // Add subtask
            AnimatedVisibility(visible = showSubtaskInput) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newSubtaskTitle,
                        onValueChange = { newSubtaskTitle = it },
                        label = { Text(stringResource(R.string.subtask_title)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newSubtaskTitle.isNotBlank()) {
                                onAddSubtask(task.taskId, newSubtaskTitle.trim())
                                newSubtaskTitle = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.add),
                            tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            OutlinedButton(
                onClick = { showSubtaskInput = !showSubtaskInput },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    if (showSubtaskInput) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (showSubtaskInput) stringResource(R.string.cancel)
                    else stringResource(R.string.add_subtask)
                )
            }

            // Created date
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "${stringResource(R.string.created)}: ${dateFormat.format(Date(task.createdAt))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_task)) },
            text = { Text(stringResource(R.string.delete_task_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Edit sheet
    if (showEditSheet) {
        TaskEditSheet(
            allTags = allTags,
            isRussian = isRussian,
            initialTitle = task.title,
            initialDescription = task.description,
            initialSelectedTagIds = tags.map { it.tagId }.toSet(),
            initialDueDate = task.dueDate,
            initialColorHex = task.colorHex,
            initialRepeatMode = task.repeatMode,
            isEditing = true,
            onSave = { newTitle, newDesc, newTagIds, dueDate, colorHex, repeatMode ->
                onUpdateTask(newTitle, newDesc, newTagIds, dueDate, colorHex, repeatMode)
                showEditSheet = false
            },
            onAddCustomTag = onAddCustomTag,
            onDismiss = { showEditSheet = false }
        )
    }
}
