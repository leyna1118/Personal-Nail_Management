package com.leyna.nailmanagement.ui.screens

import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
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
                Toast.makeText(context, "Save successful", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Save failed: ${e.message}", Toast.LENGTH_SHORT).show()
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
            // Theme section header
            Text(
                text = "Theme",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            // Dynamic color toggle (only on Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ListItem(
                    headlineContent = { Text("Dynamic Color") },
                    supportingContent = { Text("Use wallpaper-based colors") },
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
                        headlineContent = { Text("Theme Color") },
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
                                Text("Edit")
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
                        text = "Theme Color",
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
                                        contentDescription = "Selected",
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
                            label = { Text("Hex Color") },
                            placeholder = { Text("#FF1118") },
                            isError = hexError,
                            supportingText = if (hexError) {
                                { Text("Invalid hex color") }
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
                            Text("Cancel")
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
                            Text("Apply")
                        }
                    }
                }
            }

            HorizontalDivider()

            // Data section header
            Text(
                text = "Data",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            // Export item
            ListItem(
                headlineContent = { Text("Export Data") },
                supportingContent = { Text("Export all data to a .zip file to transfer data") },
                modifier = Modifier.clickable(enabled = !isLoading) {
                    scope.launch {
                        isLoading = true
                        try {
                            val file = backupRepository.exportToZip()
                            exportedFile = file
                            showExportDialog = true
                        } catch (e: Exception) {
                            Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }
                }
            )
            HorizontalDivider()

            // Import item
            ListItem(
                headlineContent = { Text("Import Data") },
                supportingContent = { Text("Import data from a .zip file (will overwrite existing data)") },
                modifier = Modifier.clickable(enabled = !isLoading) {
                    openDocumentLauncher.launch(arrayOf("application/zip"))
                }
            )
            HorizontalDivider()
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
            title = { Text("Export Complete") },
            text = { Text("Choose how to export:") },
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
                    context.startActivity(Intent.createChooser(shareIntent, "Share Backup File"))
                    showExportDialog = false
                }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Share")
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
                        Text("Save to Device")
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
            title = { Text("Confirm Import") },
            text = { Text("Import will clear all existing data. Are you sure you want to continue?") },
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
                                Toast.makeText(context, "Unable to read file", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                            pendingImportUri = null
                        }
                    }
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImportConfirmDialog = false
                    pendingImportUri = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Import result dialog
    if (showImportResultDialog && importResult != null) {
        AlertDialog(
            onDismissRequest = { showImportResultDialog = false },
            title = { Text("Import Complete") },
            text = {
                Text("Imported ${importResult!!.gelCount} Gels and ${importResult!!.nailStyleCount} Nail Styles")
            },
            confirmButton = {
                TextButton(onClick = { showImportResultDialog = false }) {
                    Text("OK")
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
