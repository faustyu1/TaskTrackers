package ru.faustyu.tasktrackers.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.faustyu.tasktrackers.R
import ru.faustyu.tasktrackers.data.model.TagGroup
import ru.faustyu.tasktrackers.data.model.TaskWithTags

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EisenhowerScreen(
    tasks: List<TaskWithTags>,
    isRussian: Boolean,
    onTaskClick: (Long) -> Unit,
    onBack: () -> Unit
) {
    // Classify tasks into quadrants
    val importantTags = setOf("Critical", "High")
    val notImportantTags = setOf("Low", "Medium")
    val urgentTags = setOf("On Fire", "Urgent")
    val notUrgentTags = setOf("Not Urgent")

    fun isImportant(twt: TaskWithTags): Boolean {
        return twt.tags.any { it.group == TagGroup.IMPORTANCE && it.name in importantTags }
    }

    fun isUrgent(twt: TaskWithTags): Boolean {
        return twt.tags.any { it.group == TagGroup.URGENCY && it.name in urgentTags }
    }

    val q1 = tasks.filter { isImportant(it) && isUrgent(it) }     // Do first
    val q2 = tasks.filter { isImportant(it) && !isUrgent(it) }    // Schedule
    val q3 = tasks.filter { !isImportant(it) && isUrgent(it) }    // Delegate
    val q4 = tasks.filter { !isImportant(it) && !isUrgent(it) }   // Eliminate

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.eisenhower_matrix)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
                .padding(12.dp)
        ) {
            // Top row: Important
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Q1: Important + Urgent = DO FIRST
                QuadrantCard(
                    title = stringResource(R.string.q1_do_first),
                    tasks = q1,
                    color = Color(0xFFEF5350),
                    isRussian = isRussian,
                    onTaskClick = onTaskClick,
                    modifier = Modifier.weight(1f)
                )
                // Q2: Important + Not Urgent = SCHEDULE
                QuadrantCard(
                    title = stringResource(R.string.q2_schedule),
                    tasks = q2,
                    color = Color(0xFF42A5F5),
                    isRussian = isRussian,
                    onTaskClick = onTaskClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom row: Not Important
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Q3: Not Important + Urgent = DELEGATE
                QuadrantCard(
                    title = stringResource(R.string.q3_delegate),
                    tasks = q3,
                    color = Color(0xFFFFA726),
                    isRussian = isRussian,
                    onTaskClick = onTaskClick,
                    modifier = Modifier.weight(1f)
                )
                // Q4: Not Important + Not Urgent = ELIMINATE
                QuadrantCard(
                    title = stringResource(R.string.q4_eliminate),
                    tasks = q4,
                    color = Color(0xFF78909C),
                    isRussian = isRussian,
                    onTaskClick = onTaskClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "← ${stringResource(R.string.urgent)} | ${stringResource(R.string.not_urgent)} →",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QuadrantCard(
    title: String,
    tasks: List<TaskWithTags>,
    color: Color,
    isRussian: Boolean,
    onTaskClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        )
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${tasks.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = color.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = color.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(6.dp))

            // Task list
            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "—",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(tasks, key = { it.task.taskId }) { twt ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onTaskClick(twt.task.taskId) },
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            tonalElevation = 1.dp
                        ) {
                            Text(
                                text = twt.task.title,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
