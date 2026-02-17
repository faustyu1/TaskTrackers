package ru.faustyu.tasktrackers.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.faustyu.tasktrackers.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentLocale: String,
    currentTheme: String,
    onSetLocale: (String) -> Unit,
    onSetTheme: (String) -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    showExportSuccess: Boolean,
    showImportSuccess: Boolean,
    onDismissExportSuccess: () -> Unit,
    onDismissImportSuccess: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
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
            // Language section
            Text(
                text = stringResource(R.string.language),
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
                        .padding(8.dp)
                ) {
                    val localeOptions = listOf(
                        "system" to stringResource(R.string.locale_system),
                        "en" to stringResource(R.string.locale_english),
                        "ru" to stringResource(R.string.locale_russian)
                    )
                    localeOptions.forEachIndexed { index, (value, label) ->
                        ListItem(
                            headlineContent = { Text(label) },
                            leadingContent = {
                                RadioButton(
                                    selected = currentLocale == value,
                                    onClick = { onSetLocale(value) }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSetLocale(value) }
                        )
                        if (index < localeOptions.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Theme section
            Text(
                text = stringResource(R.string.theme),
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
                        .padding(8.dp)
                ) {
                    val themeOptions = listOf(
                        "system" to stringResource(R.string.theme_system),
                        "light" to stringResource(R.string.theme_light),
                        "dark" to stringResource(R.string.theme_dark)
                    )
                    themeOptions.forEachIndexed { index, (value, label) ->
                        ListItem(
                            headlineContent = { Text(label) },
                            leadingContent = {
                                RadioButton(
                                    selected = currentTheme == value,
                                    onClick = { onSetTheme(value) }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSetTheme(value) }
                        )
                        if (index < themeOptions.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Export / Import
            Text(
                text = stringResource(R.string.data),
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
                        .padding(8.dp)
                ) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.export_data)) },
                        supportingContent = { Text(stringResource(R.string.export_description)) },
                        leadingContent = {
                            Icon(Icons.Default.Upload, contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onExportClick() }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.import_data)) },
                        supportingContent = { Text(stringResource(R.string.import_description)) },
                        leadingContent = {
                            Icon(Icons.Default.Download, contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onImportClick() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About section
            Text(
                text = stringResource(R.string.about),
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
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.app_name)) },
                        supportingContent = { Text(stringResource(R.string.app_version)) },
                        leadingContent = {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }
            }
        }
    }

    // Auto-dismiss snackbars
    if (showExportSuccess) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            onDismissExportSuccess()
        }
    }
    if (showImportSuccess) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            onDismissImportSuccess()
        }
    }
}
