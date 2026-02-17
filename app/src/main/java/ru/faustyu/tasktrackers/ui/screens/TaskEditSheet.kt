package ru.faustyu.tasktrackers.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.faustyu.tasktrackers.R
import ru.faustyu.tasktrackers.data.model.Tag
import ru.faustyu.tasktrackers.data.model.TagGroup
import ru.faustyu.tasktrackers.ui.theme.parseTagColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskEditSheet(
    allTags: List<Tag>,
    isRussian: Boolean,
    initialTitle: String = "",
    initialDescription: String = "",
    initialSelectedTagIds: Set<Long> = emptySet(),
    isEditing: Boolean = false,
    onSave: (title: String, description: String, tagIds: List<Long>) -> Unit,
    onAddCustomTag: (name: String, nameRu: String, group: TagGroup, colorHex: String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var selectedTagIds by remember { mutableStateOf(initialSelectedTagIds) }
    var showAddCustomTag by remember { mutableStateOf(false) }
    var customTagName by remember { mutableStateOf("") }
    var customTagNameRu by remember { mutableStateOf("") }
    var selectedCustomGroup by remember { mutableStateOf(TagGroup.CUSTOM) }
    var titleError by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var keyboardJustHidden by remember { mutableStateOf(false) }

    // Handle back press: first hide keyboard, only then allow sheet dismiss
    BackHandler(enabled = true) {
        if (keyboardJustHidden) {
            // Keyboard was already hidden on previous back press, now dismiss
            keyboardJustHidden = false
            onDismiss()
        } else {
            // First back press: hide keyboard and clear focus
            keyboardController?.hide()
            focusManager.clearFocus()
            keyboardJustHidden = true
        }
    }
    // Capture localized context BEFORE ModalBottomSheet (it opens a new window that loses context overrides)
    val localizedContext = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        windowInsets = WindowInsets.statusBars,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        CompositionLocalProvider(LocalContext provides localizedContext) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
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
                    keyboardJustHidden = false
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
                    keyboardJustHidden = false
                },
                label = { Text(stringResource(R.string.task_description)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tag selector
            Text(
                text = stringResource(R.string.select_tags),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val groups = listOf(
                TagGroup.READINESS to stringResource(R.string.group_readiness),
                TagGroup.IMPORTANCE to stringResource(R.string.group_importance),
                TagGroup.URGENCY to stringResource(R.string.group_urgency),
                TagGroup.SPHERE to stringResource(R.string.group_sphere),
                TagGroup.CUSTOM to stringResource(R.string.group_custom)
            )

            groups.forEach { (group, groupName) ->
                val tagsInGroup = allTags.filter { it.group == group }
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
                                keyboardJustHidden = false
                            },
                            label = { Text(stringResource(R.string.tag_name_en)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customTagNameRu,
                            onValueChange = {
                                customTagNameRu = it
                                keyboardJustHidden = false
                            },
                            label = { Text(stringResource(R.string.tag_name_ru)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Group selector for custom tag
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
                                customTagNameRu = ""
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
                                            customTagNameRu.ifBlank { customTagName }.trim(),
                                            selectedCustomGroup,
                                            color
                                        )
                                        customTagName = ""
                                        customTagNameRu = ""
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
                        onSave(title.trim(), description.trim(), selectedTagIds.toList())
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
        }
        }
    }
}
