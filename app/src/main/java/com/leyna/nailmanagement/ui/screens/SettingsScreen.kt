package com.leyna.nailmanagement.ui.screens

import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.os.LocaleListCompat
import com.leyna.nailmanagement.R
import com.leyna.nailmanagement.data.repository.BackupRepository
import com.leyna.nailmanagement.data.repository.ImportResult
import com.leyna.nailmanagement.ui.theme.ThemePreferences
import kotlinx.coroutines.launch
import java.io.File

private val PRESET_COLORS = listOf(
    Color(0xFFF44336), // Red
    Color(0xFFE91E63), // Pink
    Color(0xFF9C27B0), // Deep Purple
    Color(0xFF6650A4), // Purple
    Color(0xFF2196F3), // Blue
    Color(0xFF00BCD4), // Cyan
    Color(0xFF009688), // Teal
    Color(0xFF4CAF50), // Green
    Color(0xFFFFEB3B), // Yellow
    Color(0xFFFF9800), // Orange
    Color(0xFF795548), // Brown
    Color(0xFF607D8B), // Blue Grey
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreenContent(
    backupRepository: BackupRepository,
    themePreferences: ThemePreferences,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var exportedFile by remember { mutableStateOf<File?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportConfirmDialog by remember { mutableStateOf(false) }
    var importResult by remember { mutableStateOf<ImportResult?>(null) }
    var showImportResultDialog by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var hexInput by remember { mutableStateOf(TextFieldValue("")) }
    var hexError by remember { mutableStateOf(false) }

    // SAF: Save exported file to device
    val saveDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null && exportedFile != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    exportedFile!!.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                Toast.makeText(context, context.getString(R.string.toast_save_successful), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.toast_save_failed, e.message ?: ""), Toast.LENGTH_SHORT).show()
            }
        }
        showExportDialog = false
    }

    // SAF: Open .zip file for import
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingImportUri = uri
            showImportConfirmDialog = true
        }
    }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Language section header
            Text(
                text = stringResource(R.string.settings_language),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            // Language selector
            val currentLocale = AppCompatDelegate.getApplicationLocales()
            val currentLanguageTag = if (currentLocale.isEmpty) "" else currentLocale.toLanguageTags()
            var showLanguageDialog by remember { mutableStateOf(false) }

            val currentLanguageLabel = when {
                currentLanguageTag.isEmpty() -> stringResource(R.string.settings_language_system)
                currentLanguageTag.startsWith("zh") -> stringResource(R.string.settings_language_zh_tw)
                else -> stringResource(R.string.settings_language_en)
            }

            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_language)) },
                supportingContent = { Text(currentLanguageLabel) },
                modifier = Modifier.clickable { showLanguageDialog = true }
            )
            HorizontalDivider()

            if (showLanguageDialog) {
                val languageOptions = listOf(
                    "" to stringResource(R.string.settings_language_system),
                    "en" to stringResource(R.string.settings_language_en),
                    "zh-TW" to stringResource(R.string.settings_language_zh_tw),
                )
                AlertDialog(
                    onDismissRequest = { showLanguageDialog = false },
                    title = { Text(stringResource(R.string.settings_language)) },
                    text = {
                        Column {
                            languageOptions.forEach { (tag, label) ->
                                val isSelected = tag == currentLanguageTag ||
                                    (tag.isEmpty() && currentLanguageTag.isEmpty())
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val localeList = if (tag.isEmpty()) {
                                                LocaleListCompat.getEmptyLocaleList()
                                            } else {
                                                LocaleListCompat.forLanguageTags(tag)
                                            }
                                            AppCompatDelegate.setApplicationLocales(localeList)
                                            showLanguageDialog = false
                                        }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            val localeList = if (tag.isEmpty()) {
                                                LocaleListCompat.getEmptyLocaleList()
                                            } else {
                                                LocaleListCompat.forLanguageTags(tag)
                                            }
                                            AppCompatDelegate.setApplicationLocales(localeList)
                                            showLanguageDialog = false
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(label)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showLanguageDialog = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            // Theme section header
            Text(
                text = stringResource(R.string.settings_theme),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            // Dynamic color toggle (only on Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_dynamic_color)) },
                    supportingContent = { Text(stringResource(R.string.settings_dynamic_color_desc)) },
                    trailingContent = {
                        Switch(
                            checked = themePreferences.dynamicColor,
                            onCheckedChange = { themePreferences.updateDynamicColor(it) }
                        )
                    }
                )
                HorizontalDivider()
            }

            // Custom seed color section (only visible when dynamic color is off)
            if (!themePreferences.dynamicColor) {
                var isEditingColor by remember { mutableStateOf(false) }
                val currentHex = "#${Integer.toHexString(themePreferences.seedColorArgb).drop(2).uppercase()}"

                if (!isEditingColor) {
                    // Display mode: show current color with edit button
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_theme_color)) },
                        supportingContent = { Text(currentHex) },
                        leadingContent = {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(themePreferences.seedColor)
                            )
                        },
                        trailingContent = {
                            TextButton(onClick = { isEditingColor = true }) {
                                Text(stringResource(R.string.edit))
                            }
                        }
                    )
                } else {
                    // Edit mode: palette selection is local until Apply

                    // Compute preview color from hex input (pad with F)
                    val previewColor = remember(hexInput.text, themePreferences.seedColorArgb) {
                        val stripped = hexInput.text.trim().removePrefix("#")
                        if (stripped.isEmpty()) {
                            Color(themePreferences.seedColorArgb)
                        } else {
                            val padded = stripped.padEnd(6, 'F')
                            try {
                                Color((0xFF shl 24) or padded.take(6).toLong(16).toInt())
                            } catch (_: NumberFormatException) {
                                Color(themePreferences.seedColorArgb)
                            }
                        }
                    }

                    Text(
                        text = stringResource(R.string.settings_theme_color),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 8.dp)
                    )

                    // Preset color palette
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PRESET_COLORS.forEach { color ->
                            val colorHex = "#${Integer.toHexString(color.toArgb()).drop(2).uppercase()}"
                            val isSelected = hexInput.text.equals(colorHex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .clickable {
                                        hexInput = TextFieldValue(
                                            text = colorHex,
                                            selection = TextRange(colorHex.length)
                                        )
                                        hexError = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = stringResource(R.string.cd_selected),
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Hex color input with preview
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = hexInput,
                            onValueChange = { value ->
                                hexInput = value
                                hexError = false
                            },
                            label = { Text(stringResource(R.string.settings_hex_color)) },
                            placeholder = { Text(stringResource(R.string.settings_hex_placeholder)) },
                            isError = hexError,
                            supportingText = if (hexError) {
                                { Text(stringResource(R.string.error_invalid_hex_color)) }
                            } else null,
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(previewColor)
                        )
                    }

                    // Apply / Cancel buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                isEditingColor = false
                                hexInput = TextFieldValue("")
                                hexError = false
                            }
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                val parsed = parseHexColor(hexInput.text)
                                if (parsed != null) {
                                    themePreferences.updateSeedColor(parsed)
                                    hexInput = TextFieldValue("")
                                    hexError = false
                                    isEditingColor = false
                                } else {
                                    hexError = true
                                }
                            }
                        ) {
                            Text(stringResource(R.string.action_apply))
                        }
                    }
                }
                HorizontalDivider()
            }

            // Data section header
            Text(
                text = stringResource(R.string.settings_data),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            // Export item
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_export_data)) },
                supportingContent = { Text(stringResource(R.string.settings_export_data_desc)) },
                modifier = Modifier.clickable(enabled = !isLoading) {
                    scope.launch {
                        isLoading = true
                        try {
                            val file = backupRepository.exportToZip()
                            exportedFile = file
                            showExportDialog = true
                        } catch (e: Exception) {
                            Toast.makeText(context, context.getString(R.string.toast_export_failed, e.message ?: ""), Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }
                }
            )
            HorizontalDivider()

            // Import item
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_import_data)) },
                supportingContent = { Text(stringResource(R.string.settings_import_data_desc)) },
                modifier = Modifier.clickable(enabled = !isLoading) {
                    openDocumentLauncher.launch(arrayOf("application/zip"))
                }
            )
            HorizontalDivider()

            // About section header
            Text(
                text = stringResource(R.string.settings_about),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_version)) },
                supportingContent = {
                    val versionName = context.packageManager
                        .getPackageInfo(context.packageName, 0).versionName
                    Text(versionName ?: stringResource(R.string.version_unknown))
                }
            )
            HorizontalDivider()

            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_author)) },
                supportingContent = { Text(stringResource(R.string.author_name)) }
            )
        }

        // Loading overlay
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

    // Export dialog: share or save
    if (showExportDialog && exportedFile != null) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text(stringResource(R.string.dialog_title_export_complete)) },
            text = { Text(stringResource(R.string.dialog_export_choose)) },
            confirmButton = {
                TextButton(onClick = {
                    val fileUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        exportedFile!!
                    )
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/zip"
                        putExtra(Intent.EXTRA_STREAM, fileUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.action_share)))
                    showExportDialog = false
                }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.action_share))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    saveDocumentLauncher.launch(exportedFile!!.name)
                }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.action_save_to_device))
                    }
                }
            }
        )
    }

    // Import confirm dialog
    if (showImportConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                showImportConfirmDialog = false
                pendingImportUri = null
            },
            title = { Text(stringResource(R.string.dialog_title_confirm_import)) },
            text = { Text(stringResource(R.string.dialog_import_warning)) },
            confirmButton = {
                TextButton(onClick = {
                    showImportConfirmDialog = false
                    val uri = pendingImportUri ?: return@TextButton
                    scope.launch {
                        isLoading = true
                        try {
                            val inputStream = context.contentResolver.openInputStream(uri)
                            if (inputStream != null) {
                                val result = backupRepository.importFromZip(inputStream)
                                importResult = result
                                showImportResultDialog = true
                            } else {
                                Toast.makeText(context, context.getString(R.string.toast_unable_to_read_file), Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, context.getString(R.string.toast_import_failed, e.message ?: ""), Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                            pendingImportUri = null
                        }
                    }
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImportConfirmDialog = false
                    pendingImportUri = null
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Import result dialog
    if (showImportResultDialog && importResult != null) {
        AlertDialog(
            onDismissRequest = { showImportResultDialog = false },
            title = { Text(stringResource(R.string.dialog_title_import_complete)) },
            text = {
                if (importResult!!.inventoryCount > 0) {
                    Text(stringResource(R.string.dialog_import_result_v2, importResult!!.gelCount, importResult!!.nailStyleCount, importResult!!.inventoryCount))
                } else {
                    Text(stringResource(R.string.dialog_import_result, importResult!!.gelCount, importResult!!.nailStyleCount))
                }
            },
            confirmButton = {
                TextButton(onClick = { showImportResultDialog = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
}

private fun parseHexColor(input: String): Color? {
    val hex = input.trim().removePrefix("#")
    if (hex.length != 6) return null
    return try {
        val argb = (0xFF shl 24) or hex.toLong(16).toInt()
        Color(argb)
    } catch (_: NumberFormatException) {
        null
    }
}
