package ru.faustyu.tasktrackers.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.faustyu.tasktrackers.R
import ru.faustyu.tasktrackers.data.model.Tag
import ru.faustyu.tasktrackers.data.model.TagGroup
import ru.faustyu.tasktrackers.ui.theme.parseTagColor
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskEditSheet(
    allTags: List<Tag>,
    isRussian: Boolean,
    initialTitle: String = "",
    initialDescription: String = "",
    initialSelectedTagIds: Set<Long> = emptySet(),
    initialDueDate: Long? = null,
    initialColorHex: String? = null,
    initialRepeatMode: String = "none",
    isEditing: Boolean = false,
    onSave: (title: String, description: String, tagIds: List<Long>,
             dueDate: Long?, colorHex: String?, repeatMode: String) -> Unit,
    onAddCustomTag: (name: String, group: TagGroup, colorHex: String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var selectedTagIds by remember { mutableStateOf(initialSelectedTagIds) }
    var showAddCustomTag by remember { mutableStateOf(false) }
    var customTagName by remember { mutableStateOf("") }
    var selectedCustomGroup by remember { mutableStateOf(TagGroup.CUSTOM) }
    var titleError by remember { mutableStateOf(false) }

    var selectedDueDate by remember { mutableStateOf(initialDueDate) }
    var selectedColorHex by remember { mutableStateOf(initialColorHex) }
    var selectedRepeatMode by remember { mutableStateOf(initialRepeatMode) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current


    val localizedContext = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDueDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDueDate = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.save_changes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        // Check if keyboard is currently visible
        val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0

        // Explicitly handle Back press ONLY when keyboard is visible
        BackHandler(enabled = imeVisible) {
            keyboardController?.hide()
            focusManager.clearFocus()
        }

        CompositionLocalProvider(LocalContext provides localizedContext) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding() // Move content up when keyboard opens
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 60.dp) // Add padding for gradient and ease of scrolling
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEditing) stringResource(R.string.edit_task) else stringResource(R.string.create_task),
                            style = MaterialTheme.typography.titleLarge
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title field
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            titleError = false
                        },
                        label = { Text(stringResource(R.string.task_title)) },
                        isError = titleError,
                        supportingText = {
                            if (titleError) {
                                Text(stringResource(R.string.title_required))
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Description field
                    OutlinedTextField(
                        value = description,
                        onValueChange = {
                            description = it
                        },
                        label = { Text(stringResource(R.string.task_description)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 5,
                        shape = MaterialTheme.shapes.medium
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Settings Row (Date, Repeat)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Date picker
                        OutlinedCard(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = if (selectedDueDate != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (selectedDueDate != null) dateFormat.format(Date(selectedDueDate!!)) else stringResource(R.string.no_deadline),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selectedDueDate != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Repeat mode
                        var expandedRepeat by remember { mutableStateOf(false) }
                        OutlinedCard(
                            onClick = { expandedRepeat = true },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Repeat,
                                    contentDescription = null,
                                    tint = if (selectedRepeatMode != "none") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = when (selectedRepeatMode) {
                                        "daily" -> stringResource(R.string.repeat_daily)
                                        "weekly" -> stringResource(R.string.repeat_weekly)
                                        "monthly" -> stringResource(R.string.repeat_monthly)
                                        else -> stringResource(R.string.repeat_none)
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selectedRepeatMode != "none") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                DropdownMenu(
                                    expanded = expandedRepeat,
                                    onDismissRequest = { expandedRepeat = false }
                                ) {
                                    listOf(
                                        "none" to stringResource(R.string.repeat_none),
                                        "daily" to stringResource(R.string.repeat_daily),
                                        "weekly" to stringResource(R.string.repeat_weekly),
                                        "monthly" to stringResource(R.string.repeat_monthly)
                                    ).forEach { (mode, label) ->
                                        DropdownMenuItem(
                                            text = { Text(label) },
                                            onClick = {
                                                selectedRepeatMode = mode
                                                expandedRepeat = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Color picker
                    Text(
                        text = stringResource(R.string.task_color),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    val taskColors = listOf(
                        null, "#F44336", "#E91E63", "#9C27B0", "#673AB7",
                        "#3F51B5", "#2196F3", "#03A9F4", "#009688",
                        "#4CAF50", "#8BC34A", "#CDDC39", "#FFEB3B",
                        "#FFC107", "#FF9800", "#FF5722", "#795548"
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        taskColors.forEach { colorHex ->
                            val bgColor = colorHex?.let { parseTagColor(it) } ?: MaterialTheme.colorScheme.surfaceContainerHigh
                            val isSelected = selectedColorHex == colorHex
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(bgColor)
                                    .then(
                                        if (isSelected) Modifier.border(
                                            3.dp,
                                            MaterialTheme.colorScheme.primary,
                                            CircleShape
                                        ) else Modifier
                                    )
                                    .clickable { selectedColorHex = colorHex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (colorHex != null) Color.White else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                if (colorHex == null) {
                                    Icon(
                                        Icons.Default.FormatColorReset,
                                        contentDescription = stringResource(R.string.no_color),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Tag selector
                    Text(
                        text = stringResource(R.string.select_tags),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    val exclusiveGroups = setOf(TagGroup.READINESS, TagGroup.IMPORTANCE, TagGroup.URGENCY)

                    val groups = listOf(
                        TagGroup.READINESS to stringResource(R.string.group_readiness),
                        TagGroup.IMPORTANCE to stringResource(R.string.group_importance),
                        TagGroup.URGENCY to stringResource(R.string.group_urgency),
                        TagGroup.SPHERE to stringResource(R.string.group_sphere),
                        TagGroup.CUSTOM to stringResource(R.string.group_custom)
                    )

                    groups.forEach { (group, groupName) ->
                        val tagsInGroup = allTags.filter { it.group == group }
                        val isExclusive = group in exclusiveGroups
                        if (tagsInGroup.isNotEmpty() || group == TagGroup.CUSTOM) {
                            Text(
                                text = groupName,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 6.dp)
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                tagsInGroup.forEach { tag ->
                                    val isSelected = tag.tagId in selectedTagIds
                                    val tagColor = parseTagColor(tag.colorHex)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedTagIds = if (isSelected) {
                                                selectedTagIds - tag.tagId
                                            } else if (isExclusive) {
                                                val otherIdsInGroup = tagsInGroup.map { it.tagId }.toSet()
                                                (selectedTagIds - otherIdsInGroup) + tag.tagId
                                            } else {
                                                selectedTagIds + tag.tagId
                                            }
                                        },
                                        label = {
                                            Text(
                                                if (isRussian) tag.nameRu else tag.name,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = tagColor.copy(alpha = 0.2f),
                                            selectedLabelColor = tagColor
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Add custom tag
                    AnimatedVisibility(visible = !showAddCustomTag) {
                        OutlinedButton(
                            onClick = { showAddCustomTag = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.add_custom_tag))
                        }
                    }

                    AnimatedVisibility(visible = showAddCustomTag) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = customTagName,
                                    onValueChange = {
                                        customTagName = it
                                    },
                                    label = { Text(stringResource(R.string.tag_name)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = MaterialTheme.shapes.medium
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = stringResource(R.string.tag_group),
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val groupOptions = listOf(
                                        TagGroup.CUSTOM to stringResource(R.string.group_custom),
                                        TagGroup.READINESS to stringResource(R.string.group_readiness),
                                        TagGroup.IMPORTANCE to stringResource(R.string.group_importance),
                                        TagGroup.URGENCY to stringResource(R.string.group_urgency),
                                        TagGroup.SPHERE to stringResource(R.string.group_sphere)
                                    )
                                    groupOptions.forEach { (group, label) ->
                                        FilterChip(
                                            selected = selectedCustomGroup == group,
                                            onClick = { selectedCustomGroup = group },
                                            label = { Text(label) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = {
                                        showAddCustomTag = false
                                        customTagName = ""
                                    }) {
                                        Text(stringResource(R.string.cancel))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    FilledTonalButton(
                                        onClick = {
                                            if (customTagName.isNotBlank()) {
                                                val colors = listOf("#78909C", "#7E57C2", "#26A69A", "#FF8A65", "#66BB6A", "#42A5F5", "#EC407A")
                                                val color = colors.random()
                                                onAddCustomTag(
                                                    customTagName.trim(),
                                                    selectedCustomGroup,
                                                    color
                                                )
                                                customTagName = ""
                                                showAddCustomTag = false
                                            }
                                        },
                                        enabled = customTagName.isNotBlank()
                                    ) {
                                        Text(stringResource(R.string.add))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Save button
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                titleError = true
                            } else {
                                onSave(
                                    title.trim(),
                                    description.trim(),
                                    selectedTagIds.toList(),
                                    selectedDueDate,
                                    selectedColorHex,
                                    selectedRepeatMode
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text(
                            text = if (isEditing) stringResource(R.string.save_changes) else stringResource(R.string.create_task),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                // Bottom fade gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp) // Make it taller for better liquid feel
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.surface, // Use surface color for seamless blend
                                    MaterialTheme.colorScheme.surface  // Solid at bottom
                                )
                            )
                        )
                )
            }
        }
    }
}
