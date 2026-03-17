package com.leyna.nailmanagement.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.leyna.nailmanagement.R
import com.leyna.nailmanagement.data.entity.Gel
import kotlinx.coroutines.launch

data class GelSuggestions(
    val brands: List<String> = emptyList(),
    val seriesList: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val stores: List<String> = emptyList()
)

@Composable
fun EditGelContent(
    gel: Gel?,
    onSave: (
        name: String, price: Double, colorCode: String,
        imageUri: Uri?, existingImagePath: String?,
        brand: String?, series: String?, category: String?,
        store: String?, storeNote: String?, notes: String?
    ) -> Unit,
    modifier: Modifier = Modifier,
    suggestions: GelSuggestions = GelSuggestions(),
    onLookupStoreNote: (suspend (String) -> String?)? = null,
    onUpdateStoreNoteForAll: ((String, String?) -> Unit)? = null
) {
    val scope = rememberCoroutineScope()

    var name by rememberSaveable { mutableStateOf(gel?.name ?: "") }
    var price by rememberSaveable { mutableStateOf(gel?.price?.toString() ?: "") }
    var colorCode by rememberSaveable { mutableStateOf(gel?.colorCode ?: "") }
    var brand by rememberSaveable { mutableStateOf(gel?.brand ?: "") }
    var series by rememberSaveable { mutableStateOf(gel?.series ?: "") }
    var category by rememberSaveable { mutableStateOf(gel?.category ?: "") }
    var store by rememberSaveable { mutableStateOf(gel?.store ?: "") }
    var storeNote by rememberSaveable { mutableStateOf(gel?.storeNote ?: "") }
    var notes by rememberSaveable { mutableStateOf(gel?.notes ?: "") }

    // Track original store note loaded from DB for this store name
    var originalStoreNote by remember { mutableStateOf<String?>(gel?.storeNote) }

    // Store note confirmation dialog
    var showStoreNoteConfirm by remember { mutableStateOf(false) }

    // Track the newly selected URI separately from existing image path
    var selectedImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val existingImagePath = gel?.imagePath

    // For display: show selected URI if available, otherwise show existing path
    val displayImage: Any? = selectedImageUri ?: existingImagePath

    var nameError by rememberSaveable { mutableStateOf(false) }
    var priceError by rememberSaveable { mutableStateOf(false) }
    var colorCodeError by rememberSaveable { mutableStateOf(false) }


    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Image picker section
        Text(
            text = stringResource(R.string.label_image),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { imagePickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (displayImage != null) {
                AsyncImage(
                    model = displayImage,
                    contentDescription = stringResource(R.string.cd_gel_image),
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.cd_add_image),
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.tap_to_add_image),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                nameError = false
            },
            label = { Text(stringResource(R.string.label_name)) },
            isError = nameError,
            supportingText = if (nameError) {
                { Text(stringResource(R.string.error_name_required)) }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = colorCode,
            onValueChange = {
                colorCode = it
                colorCodeError = false
            },
            label = { Text(stringResource(R.string.label_color_code)) },
            isError = colorCodeError,
            supportingText = if (colorCodeError) {
                { Text(stringResource(R.string.error_color_code_required)) }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Brand
        AutocompleteTextField(
            value = brand,
            onValueChange = { brand = it },
            label = stringResource(R.string.label_brand),
            suggestions = suggestions.brands,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Series
        AutocompleteTextField(
            value = series,
            onValueChange = { series = it },
            label = stringResource(R.string.label_series),
            suggestions = suggestions.seriesList,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Category
        AutocompleteTextField(
            value = category,
            onValueChange = { category = it },
            label = stringResource(R.string.label_category),
            suggestions = suggestions.categories,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Price
        OutlinedTextField(
            value = price,
            onValueChange = {
                price = it
                priceError = false
            },
            label = { Text(stringResource(R.string.label_price)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = priceError,
            supportingText = if (priceError) {
                { Text(stringResource(R.string.error_valid_price_required)) }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Store (with auto-fill of storeNote on selection)
        AutocompleteTextField(
            value = store,
            onValueChange = { store = it },
            onSuggestionSelected = { selectedStore ->
                store = selectedStore
                // Auto-fill storeNote from existing data
                if (onLookupStoreNote != null) {
                    scope.launch {
                        val note = onLookupStoreNote(selectedStore)
                        if (note != null) {
                            storeNote = note
                            originalStoreNote = note
                        }
                    }
                }
            },
            label = stringResource(R.string.label_store),
            suggestions = suggestions.stores,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Store Note (link/address)
        OutlinedTextField(
            value = storeNote,
            onValueChange = { storeNote = it },
            label = { Text(stringResource(R.string.label_store_note)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Notes
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(stringResource(R.string.label_notes)) },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val isNameValid = name.isNotBlank()
                val isPriceValid = price.toDoubleOrNull() != null && price.toDouble() >= 0
                val isColorCodeValid = colorCode.isNotBlank()

                nameError = !isNameValid
                priceError = !isPriceValid
                colorCodeError = !isColorCodeValid

                if (isNameValid && isPriceValid && isColorCodeValid) {
                    val trimmedStore = store.trim().ifBlank { null }
                    val trimmedStoreNote = storeNote.trim().ifBlank { null }

                    // Check if storeNote changed for an existing store
                    val storeNoteChanged = trimmedStore != null &&
                        originalStoreNote != null &&
                        trimmedStoreNote != originalStoreNote &&
                        onUpdateStoreNoteForAll != null

                    if (storeNoteChanged) {
                        showStoreNoteConfirm = true
                    } else {
                        onSave(
                            name.trim(), price.toDouble(), colorCode.trim(),
                            selectedImageUri, existingImagePath,
                            brand.trim().ifBlank { null },
                            series.trim().ifBlank { null },
                            category.trim().ifBlank { null },
                            trimmedStore, trimmedStoreNote,
                            notes.trim().ifBlank { null }
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (gel == null) stringResource(R.string.action_save_gel) else stringResource(R.string.action_update_gel))
        }
    }

    // Store note batch update confirmation dialog
    if (showStoreNoteConfirm) {
        val trimmedStore = store.trim()
        val trimmedStoreNote = storeNote.trim().ifBlank { null }
        AlertDialog(
            onDismissRequest = { showStoreNoteConfirm = false },
            title = { Text(stringResource(R.string.dialog_title_update_store_note)) },
            text = { Text(stringResource(R.string.dialog_update_store_note, trimmedStore)) },
            confirmButton = {
                TextButton(onClick = {
                    onUpdateStoreNoteForAll?.invoke(trimmedStore, trimmedStoreNote)
                    onSave(
                        name.trim(), price.toDouble(), colorCode.trim(),
                        selectedImageUri, existingImagePath,
                        brand.trim().ifBlank { null },
                        series.trim().ifBlank { null },
                        category.trim().ifBlank { null },
                        trimmedStore, trimmedStoreNote,
                        notes.trim().ifBlank { null }
                    )
                    showStoreNoteConfirm = false
                }) {
                    Text(stringResource(R.string.dialog_update_store_note_all))
                }
            },
            dismissButton = {
                TextButton(onClick = { showStoreNoteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AutocompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suggestions: List<String>,
    modifier: Modifier = Modifier,
    onSuggestionSelected: ((String) -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val filtered = remember(value, suggestions) {
        if (value.isBlank()) emptyList()
        else suggestions.filter { it.contains(value, ignoreCase = true) && !it.equals(value, ignoreCase = true) }
    }

    ExposedDropdownMenuBox(
        expanded = expanded && filtered.isNotEmpty(),
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = true
            },
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded && filtered.isNotEmpty(),
            onDismissRequest = { expanded = false }
        ) {
            filtered.forEach { suggestion ->
                DropdownMenuItem(
                    text = { Text(suggestion) },
                    onClick = {
                        if (onSuggestionSelected != null) {
                            onSuggestionSelected(suggestion)
                        } else {
                            onValueChange(suggestion)
                        }
                        expanded = false
                    }
                )
            }
        }
    }
}
