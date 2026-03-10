package com.leyna.nailmanagement.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.leyna.nailmanagement.data.repository.BackupRepository
import com.leyna.nailmanagement.data.repository.ImportResult
import kotlinx.coroutines.launch
import java.io.File
//用來測試的註解
@Composable
fun SettingsScreenContent(
    backupRepository: BackupRepository,
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
        Column(modifier = Modifier.fillMaxSize()) {
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
                    // Share via Share Sheet
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
