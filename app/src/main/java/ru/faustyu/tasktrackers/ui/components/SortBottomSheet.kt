package ru.faustyu.tasktrackers.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.faustyu.tasktrackers.R
import ru.faustyu.tasktrackers.viewmodel.SortMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortBottomSheet(
    currentSort: SortMode,
    onSelectSort: (SortMode) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

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
        ) {
            Text(
                text = stringResource(R.string.sort_by),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val sortOptions = listOf(
                SortMode.DATE_DESC to stringResource(R.string.sort_date_desc),
                SortMode.DATE_ASC to stringResource(R.string.sort_date_asc),
                SortMode.ALPHA_ASC to stringResource(R.string.sort_alpha_asc),
                SortMode.ALPHA_DESC to stringResource(R.string.sort_alpha_desc),
                SortMode.IMPORTANCE to stringResource(R.string.sort_importance),
                SortMode.URGENCY to stringResource(R.string.sort_urgency),
                SortMode.SPHERE to stringResource(R.string.sort_sphere)
            )

            val sortIcons = listOf(
                Icons.Default.ArrowDownward,
                Icons.Default.ArrowUpward,
                Icons.Default.SortByAlpha,
                Icons.Default.SortByAlpha,
                Icons.Default.PriorityHigh,
                Icons.Default.Schedule,
                Icons.Default.Category
            )

            sortOptions.forEachIndexed { index, (mode, label) ->
                val isSelected = mode == currentSort
                ListItem(
                    headlineContent = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = sortIcons[index],
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingContent = {
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ListItemDefaults.colors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.surface
                    )
                )
                if (index < sortOptions.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}
