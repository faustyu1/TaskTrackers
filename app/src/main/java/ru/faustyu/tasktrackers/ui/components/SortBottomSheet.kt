package ru.faustyu.tasktrackers.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
    val localizedContext = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
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
                Text(
                    text = stringResource(R.string.sort_by),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val sortOptions = listOf(
                    Triple(SortMode.DATE_DESC, stringResource(R.string.sort_date_desc), Icons.Default.ArrowDownward),
                    Triple(SortMode.DATE_ASC, stringResource(R.string.sort_date_asc), Icons.Default.ArrowUpward),
                    Triple(SortMode.ALPHA_ASC, stringResource(R.string.sort_alpha_asc), Icons.Default.SortByAlpha),
                    Triple(SortMode.ALPHA_DESC, stringResource(R.string.sort_alpha_desc), Icons.Default.SortByAlpha),
                    Triple(SortMode.IMPORTANCE, stringResource(R.string.sort_importance), Icons.Default.PriorityHigh),
                    Triple(SortMode.URGENCY, stringResource(R.string.sort_urgency), Icons.Default.Schedule),
                    Triple(SortMode.SPHERE, stringResource(R.string.sort_sphere), Icons.Default.Category),
                    Triple(SortMode.DUE_DATE, stringResource(R.string.sort_due_date), Icons.Default.CalendarToday),
                    // Triple(SortMode.MANUAL, stringResource(R.string.sort_manual), Icons.Default.DragHandle) // Manual sorting might need different UI or reordering
                )

                sortOptions.forEach { (mode, label, icon) ->
                    val isSelected = mode == currentSort
                    
                    Surface(
                        onClick = { 
                            onSelectSort(mode)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(50), // Fully rounded / pill shape
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
