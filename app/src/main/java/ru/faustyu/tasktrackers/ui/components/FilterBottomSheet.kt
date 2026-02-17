package ru.faustyu.tasktrackers.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.faustyu.tasktrackers.R
import ru.faustyu.tasktrackers.data.model.Tag
import ru.faustyu.tasktrackers.data.model.TagGroup
import ru.faustyu.tasktrackers.ui.theme.parseTagColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    allTags: List<Tag>,
    selectedTagIds: Set<Long>,
    isRussian: Boolean,
    onToggleTag: (Long) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.filter_by_tags),
                    style = MaterialTheme.typography.titleLarge
                )
                if (selectedTagIds.isNotEmpty()) {
                    TextButton(onClick = onClear) {
                        Text(stringResource(R.string.clear_all))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val groups = listOf(
                TagGroup.READINESS to stringResource(R.string.group_readiness),
                TagGroup.IMPORTANCE to stringResource(R.string.group_importance),
                TagGroup.URGENCY to stringResource(R.string.group_urgency),
                TagGroup.SPHERE to stringResource(R.string.group_sphere),
                TagGroup.CUSTOM to stringResource(R.string.group_custom)
            )

            groups.forEach { (group, groupName) ->
                val tagsInGroup = allTags.filter { it.group == group }
                if (tagsInGroup.isNotEmpty()) {
                    Text(
                        text = groupName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tagsInGroup.forEach { tag ->
                            val isSelected = tag.tagId in selectedTagIds
                            val tagColor = parseTagColor(tag.colorHex)
                            FilterChip(
                                selected = isSelected,
                                onClick = { onToggleTag(tag.tagId) },
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
        }
    }
}
