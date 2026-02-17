package ru.faustyu.tasktrackers.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.faustyu.tasktrackers.R
import ru.faustyu.tasktrackers.data.model.TagGroup
import ru.faustyu.tasktrackers.data.model.TaskWithTags
import ru.faustyu.tasktrackers.ui.theme.CompletedTaskOverlay
import ru.faustyu.tasktrackers.ui.theme.CompletedTaskOverlayDark
import ru.faustyu.tasktrackers.ui.theme.SwipeDeleteBackground
import ru.faustyu.tasktrackers.ui.theme.parseTagColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    taskWithTags: TaskWithTags,
    isRussian: Boolean,
    onToggleComplete: () -> Unit,
    onArchive: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val task = taskWithTags.task
    val tags = taskWithTags.tags

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onArchive()
                true
            } else false
        }
    )

    val completedAlpha by animateFloatAsState(
        targetValue = if (task.isCompleted) 0.6f else 1f,
        animationSpec = tween(300),
        label = "completedAlpha"
    )

    val completedOverlay by animateColorAsState(
        targetValue = if (task.isCompleted) CompletedTaskOverlay else Color.Transparent,
        animationSpec = tween(400),
        label = "completedOverlay"
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFF9800)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Archive,
                    contentDescription = stringResource(R.string.archive),
                    tint = Color.White,
                    modifier = Modifier.padding(end = 24.dp)
                )
            }
        },
        enableDismissFromStartToEnd = false,
        modifier = modifier
    ) {
        ElevatedCard(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(completedAlpha),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 2.dp,
                pressedElevation = 4.dp
            ),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .background(completedOverlay)
            ) {
                // Color strip on left
                if (task.colorHex != null) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(parseTagColor(task.colorHex))
                            .align(Alignment.CenterStart)
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { onToggleComplete() },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                uncheckedColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            color = if (task.isCompleted)
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            else
                                MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Due date & repeat indicators
                    val hasDueDate = task.dueDate != null
                    val hasRepeat = task.repeatMode != "none"
                    val hasSubtasks = taskWithTags.subtasks.isNotEmpty()

                    if (hasDueDate || hasRepeat) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.padding(start = 48.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (hasDueDate) {
                                val isOverdue = task.dueDate!! < System.currentTimeMillis() && !task.isCompleted
                                val dueDateFormat = java.text.SimpleDateFormat("dd.MM", java.util.Locale.getDefault())
                                val dateColor = if (isOverdue) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = dateColor
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = dueDateFormat.format(java.util.Date(task.dueDate)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = dateColor
                                    )
                                }
                            }
                            if (hasRepeat) {
                                Icon(
                                    Icons.Default.Repeat,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }

                    if (task.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 48.dp)
                        )
                    }

                    // Subtask progress
                    if (hasSubtasks) {
                        val completedSubs = taskWithTags.subtasks.count { it.isCompleted }
                        val total = taskWithTags.subtasks.size
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.padding(start = 48.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LinearProgressIndicator(
                                progress = { completedSubs.toFloat() / total },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$completedSubs/$total",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.padding(start = 48.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            tags
                                .sortedBy { it.group.ordinal * 100 + it.sortOrder }
                                .forEach { tag ->
                                    TagChip(
                                        label = if (isRussian) tag.nameRu else tag.name,
                                        color = parseTagColor(tag.colorHex),
                                        isSmall = true
                                    )
                                }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TagChip(
    label: String,
    color: Color,
    isSmall: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(if (isSmall) 8.dp else 12.dp),
        color = color.copy(alpha = 0.15f),
        contentColor = color,
        modifier = modifier
    ) {
        Text(
            text = label,
            style = if (isSmall) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
            color = color,
            modifier = Modifier.padding(
                horizontal = if (isSmall) 8.dp else 12.dp,
                vertical = if (isSmall) 3.dp else 6.dp
            )
        )
    }
}
