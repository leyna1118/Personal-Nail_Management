package com.leyna.nailmanagement.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.leyna.nailmanagement.R
import com.leyna.nailmanagement.data.entity.GelInventory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.US)

private fun formatDate(millis: Long?): String? {
    return millis?.let { dateFormat.format(Date(it)) }
}

@Composable
fun GelInventoryContent(
    inventoryList: List<GelInventory>,
    onAdd: (GelInventory) -> Unit,
    onUpdate: (GelInventory) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var editingInventory by remember { mutableStateOf<GelInventory?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletingId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingInventory = null
                    showEditDialog = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.action_add_inventory)
                )
            }
        }
    ) { innerPadding ->
        if (inventoryList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.empty_no_inventory),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(inventoryList, key = { it.id }) { inventory ->
                    InventoryCard(
                        inventory = inventory,
                        onEdit = {
                            editingInventory = inventory
                            showEditDialog = true
                        },
                        onDelete = {
                            deletingId = inventory.id
                            showDeleteDialog = true
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    if (showEditDialog) {
        InventoryEditDialog(
            inventory = editingInventory,
            onSave = { inv ->
                if (editingInventory != null) {
                    onUpdate(inv)
                } else {
                    onAdd(inv)
                }
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }

    if (showDeleteDialog && deletingId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.dialog_title_delete_inventory)) },
            text = { Text(stringResource(R.string.dialog_delete_inventory_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(deletingId!!)
                    showDeleteDialog = false
                    deletingId = null
                }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    deletingId = null
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun InventoryCard(
    inventory: GelInventory,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date info on the left
            Column(modifier = Modifier.weight(1f)) {
                val dates = listOf(
                    stringResource(R.string.label_purchase_date) to formatDate(inventory.purchaseDate),
                    stringResource(R.string.label_expiry_date) to formatDate(inventory.expiryDate),
                    stringResource(R.string.label_used_up_date) to formatDate(inventory.usedUpDate),
                    stringResource(R.string.label_write_off_date) to formatDate(inventory.writeOffDate)
                )

                dates.forEach { (label, value) ->
                    Row {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(100.dp)
                        )
                        Text(
                            text = value ?: "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                }

                inventory.note?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Edit/delete buttons vertically on the right
            Column {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InventoryEditDialog(
    inventory: GelInventory?,
    onSave: (GelInventory) -> Unit,
    onDismiss: () -> Unit
) {
    var purchaseDate by remember { mutableStateOf(inventory?.purchaseDate) }
    var expiryDate by remember { mutableStateOf(inventory?.expiryDate) }
    var usedUpDate by remember { mutableStateOf(inventory?.usedUpDate) }
    var writeOffDate by remember { mutableStateOf(inventory?.writeOffDate) }
    var note by remember { mutableStateOf(inventory?.note ?: "") }

    var showDatePicker by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (inventory != null) stringResource(R.string.edit)
                else stringResource(R.string.action_add_inventory)
            )
        },
        text = {
            Column {
                DateFieldRow(
                    label = stringResource(R.string.label_purchase_date),
                    dateMillis = purchaseDate,
                    onPickClick = { showDatePicker = "purchase" },
                    onClear = { purchaseDate = null }
                )
                Spacer(modifier = Modifier.height(8.dp))
                DateFieldRow(
                    label = stringResource(R.string.label_expiry_date),
                    dateMillis = expiryDate,
                    onPickClick = { showDatePicker = "expiry" },
                    onClear = { expiryDate = null }
                )
                Spacer(modifier = Modifier.height(8.dp))
                DateFieldRow(
                    label = stringResource(R.string.label_used_up_date),
                    dateMillis = usedUpDate,
                    onPickClick = { showDatePicker = "usedUp" },
                    onClear = { usedUpDate = null }
                )
                Spacer(modifier = Modifier.height(8.dp))
                DateFieldRow(
                    label = stringResource(R.string.label_write_off_date),
                    dateMillis = writeOffDate,
                    onPickClick = { showDatePicker = "writeOff" },
                    onClear = { writeOffDate = null }
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.label_inventory_note)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val result = GelInventory(
                    id = inventory?.id ?: 0,
                    gelId = inventory?.gelId ?: 0,
                    purchaseDate = purchaseDate,
                    expiryDate = expiryDate,
                    usedUpDate = usedUpDate,
                    writeOffDate = writeOffDate,
                    note = note.trim().ifBlank { null }
                )
                onSave(result)
            }) {
                Text(stringResource(R.string.action_save_inventory))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )

    if (showDatePicker != null) {
        val currentValue = when (showDatePicker) {
            "purchase" -> purchaseDate
            "expiry" -> expiryDate
            "usedUp" -> usedUpDate
            "writeOff" -> writeOffDate
            else -> null
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = currentValue)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = null },
            confirmButton = {
                TextButton(onClick = {
                    val selected = datePickerState.selectedDateMillis
                    when (showDatePicker) {
                        "purchase" -> purchaseDate = selected
                        "expiry" -> expiryDate = selected
                        "usedUp" -> usedUpDate = selected
                        "writeOff" -> writeOffDate = selected
                    }
                    showDatePicker = null
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun DateFieldRow(
    label: String,
    dateMillis: Long?,
    onPickClick: () -> Unit,
    onClear: () -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onPickClick,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = formatDate(dateMillis) ?: stringResource(R.string.label_select_date)
                )
            }
            if (dateMillis != null) {
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onClear) {
                    Text(stringResource(R.string.action_clear_date))
                }
            }
        }
    }
}
