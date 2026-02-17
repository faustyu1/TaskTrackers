package ru.faustyu.tasktrackers.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.faustyu.tasktrackers.R
import ru.faustyu.tasktrackers.data.model.TagGroup
import ru.faustyu.tasktrackers.data.model.TaskWithTags
import ru.faustyu.tasktrackers.ui.theme.parseTagColor
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    allTasks: List<TaskWithTags>,
    archivedTasks: List<TaskWithTags>,
    isRussian: Boolean,
    onBack: () -> Unit
) {
    val total = allTasks.size + archivedTasks.size
    val active = allTasks.count { !it.task.isCompleted }
    val completed = allTasks.count { it.task.isCompleted } + archivedTasks.size
    val overdue = allTasks.count { it.task.dueDate != null && it.task.dueDate < System.currentTimeMillis() && !it.task.isCompleted }

    // Tasks by sphere
    val allCombined = allTasks + archivedTasks
    val sphereMap = mutableMapOf<String, Int>()
    allCombined.forEach { twt ->
        twt.tags.filter { it.group == TagGroup.SPHERE }.forEach { tag ->
            val name = if (isRussian) tag.nameRu else tag.name
            sphereMap[name] = (sphereMap[name] ?: 0) + 1
        }
    }

    // Tasks by day of week (last 30 days)
    val cal = Calendar.getInstance()
    val thirtyDaysAgo = cal.timeInMillis - 30L * 86_400_000L
    val recentTasks = allCombined.filter { it.task.createdAt >= thirtyDaysAgo }
    val dayOfWeekMap = mutableMapOf<Int, Int>()
    recentTasks.forEach { twt ->
        cal.timeInMillis = twt.task.createdAt
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        dayOfWeekMap[dow] = (dayOfWeekMap[dow] ?: 0) + 1
    }

    // Streak calculation
    val completionDates = allCombined
        .filter { it.task.isCompleted }
        .map {
            val c = Calendar.getInstance()
            c.timeInMillis = it.task.createdAt
            c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
            c.timeInMillis
        }
        .distinct()
        .sorted()
        .reversed()

    var streak = 0
    if (completionDates.isNotEmpty()) {
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0); today.set(Calendar.MINUTE, 0); today.set(Calendar.SECOND, 0); today.set(Calendar.MILLISECOND, 0)
        var checkDate = today.timeInMillis
        for (date in completionDates) {
            if (date == checkDate || date == checkDate - 86_400_000L) {
                streak++
                checkDate = date
            } else break
        }
    }

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animationProgress.animateTo(1f, animationSpec = tween(1000, easing = EaseOutCubic))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.statistics)) },
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
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Overview cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = stringResource(R.string.stat_total),
                    value = total.toString(),
                    icon = Icons.Default.List,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = stringResource(R.string.stat_active),
                    value = active.toString(),
                    icon = Icons.Default.PlayArrow,
                    color = Color(0xFF42A5F5),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = stringResource(R.string.stat_completed),
                    value = completed.toString(),
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF66BB6A),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = stringResource(R.string.stat_overdue),
                    value = overdue.toString(),
                    icon = Icons.Default.Warning,
                    color = Color(0xFFEF5350),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Streak
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.stat_streak),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(R.string.stat_streak_days, streak),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Completion donut chart
            if (total > 0) {
                Text(
                    text = stringResource(R.string.stat_completion_rate),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val completionRate = completed.toFloat() / total
                        val primaryColor = MaterialTheme.colorScheme.primary
                        val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest

                        Canvas(modifier = Modifier.size(150.dp)) {
                            val strokeWidth = 24.dp.toPx()
                            drawArc(
                                color = trackColor,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(strokeWidth, cap = StrokeCap.Round),
                                size = Size(size.width, size.height)
                            )
                            drawArc(
                                color = primaryColor,
                                startAngle = -90f,
                                sweepAngle = 360f * completionRate * animationProgress.value,
                                useCenter = false,
                                style = Stroke(strokeWidth, cap = StrokeCap.Round),
                                size = Size(size.width, size.height)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${(completionRate * 100).toInt()}%",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.stat_done),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tasks by sphere
            if (sphereMap.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.stat_by_sphere),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        val maxCount = sphereMap.values.maxOrNull() ?: 1
                        val sphereColors = listOf(
                            Color(0xFF5C6BC0), Color(0xFFAB47BC), Color(0xFF8D6E63),
                            Color(0xFF26A69A), Color(0xFFEC407A), Color(0xFFFFA000), Color(0xFF29B6F6)
                        )
                        sphereMap.entries.forEachIndexed { index, (name, count) ->
                            if (index > 0) Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.width(100.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(20.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(fraction = (count.toFloat() / maxCount) * animationProgress.value)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(sphereColors[index % sphereColors.size])
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = count.toString(),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Activity by day of week
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.stat_activity_week),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                val dayNames = listOf("", stringResource(R.string.day_sun), stringResource(R.string.day_mon),
                    stringResource(R.string.day_tue), stringResource(R.string.day_wed),
                    stringResource(R.string.day_thu), stringResource(R.string.day_fri),
                    stringResource(R.string.day_sat))
                val maxDayCount = dayOfWeekMap.values.maxOrNull() ?: 1
                val barColor = MaterialTheme.colorScheme.primary

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        (Calendar.SUNDAY..Calendar.SATURDAY).forEach { dow ->
                            val count = dayOfWeekMap[dow] ?: 0
                            val heightFrac = if (maxDayCount > 0) count.toFloat() / maxDayCount else 0f
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = count.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(24.dp)
                                        .fillMaxHeight(fraction = (heightFrac * animationProgress.value).coerceAtLeast(0.02f))
                                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                        .background(barColor.copy(alpha = 0.3f + 0.7f * heightFrac))
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        (Calendar.SUNDAY..Calendar.SATURDAY).forEach { dow ->
                            Text(
                                text = dayNames.getOrElse(dow) { "" },
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
