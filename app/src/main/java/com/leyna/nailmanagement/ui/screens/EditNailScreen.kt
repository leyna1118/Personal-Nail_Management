package com.leyna.nailmanagement.ui.screens

import android.net.Uri
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import com.leyna.nailmanagement.data.entity.Gel
import com.leyna.nailmanagement.data.entity.NailStyleWithGels
import com.leyna.nailmanagement.data.entity.StepWithImage
import com.leyna.nailmanagement.ui.viewmodel.NailStyleViewModel
import com.leyna.nailmanagement.ui.viewmodel.StepInput

/**
 * UI state for a step being edited
 */
data class EditableStep(
    val text: String,
    val existingImagePath: String? = null,
    val newImageUri: Uri? = null
) {
    val displayImage: Any?
        get() = newImageUri ?: existingImagePath

    fun toStepInput(): StepInput = StepInput(
        text = text,
        existingImagePath = existingImagePath,
        newImageUri = newImageUri
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditNailContent(
    nailStyleWithGels: NailStyleWithGels?,
    allGels: List<Gel>,
    onSave: (
        name: String,
        steps: List<StepInput>,
        gelIds: List<Long>,
        mainImageUri: Uri?,
        existingMainImagePath: String?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val existingSteps: List<StepWithImage> = nailStyleWithGels?.let {
        NailStyleViewModel.parseSteps(it.nailStyle.steps)
    } ?: emptyList()

    val existingGelIds: List<Long> = nailStyleWithGels?.gels?.map { it.id } ?: emptyList()
    val existingMainImagePath = nailStyleWithGels?.nailStyle?.imagePath

    var name by rememberSaveable { mutableStateOf(nailStyleWithGels?.nailStyle?.name ?: "") }
    var steps by remember {
        mutableStateOf(existingSteps.map { EditableStep(it.text, it.imagePath) })
    }
    var selectedGelIds by remember { mutableStateOf(existingGelIds) }
    var selectedMainImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    var nameError by rememberSaveable { mutableStateOf(false) }

    // Track which step is waiting for an image
    var pendingStepImageIndex by remember { mutableIntStateOf(-1) }

    val mainImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedMainImageUri = uri
        }
    }

    val stepImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && pendingStepImageIndex >= 0 && pendingStepImageIndex < steps.size) {
            steps = steps.toMutableList().apply {
                val step = this[pendingStepImageIndex]
                this[pendingStepImageIndex] = step.copy(newImageUri = uri)
            }
        }
        pendingStepImageIndex = -1
    }

    val displayMainImage: Any? = selectedMainImageUri ?: existingMainImagePath

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Main finished image section
        Text(
            text = "Finished Result Image",
            style = MaterialTheme.typography.titleSmall,
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
                .clickable { mainImagePickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (displayMainImage != null) {
                AsyncImage(
                    model = displayMainImage,
                    contentDescription = "Main nail style image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add image",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap to add finished result photo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Name field
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                nameError = false
            },
            label = { Text("Style Name") },
            isError = nameError,
            supportingText = if (nameError) {
                { Text("Name is required") }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Steps section
        Text(
            text = "Steps",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        steps.forEachIndexed { index, step ->
            StepInputFieldWithImage(
                stepNumber = index + 1,
                step = step,
                allGels = allGels,
                onTextChange = { newText ->
                    steps = steps.toMutableList().apply {
                        this[index] = this[index].copy(text = newText)
                    }
                },
                onGelSelected = { gelId ->
                    if (gelId !in selectedGelIds) {
                        selectedGelIds = selectedGelIds + gelId
                    }
                },
                onAddImage = {
                    pendingStepImageIndex = index
                    stepImagePickerLauncher.launch("image/*")
                },
                onRemoveImage = {
                    steps = steps.toMutableList().apply {
                        this[index] = this[index].copy(existingImagePath = null, newImageUri = null)
                    }
                },
                onRemove = {
                    steps = steps.toMutableList().apply { removeAt(index) }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Add step button
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    steps = steps + EditableStep("")
                },
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add step",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Step",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Selected gels display
        if (selectedGelIds.isNotEmpty()) {
            Text(
                text = "Selected Gels",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                selectedGelIds.forEach { gelId ->
                    val gel = allGels.find { it.id == gelId }
                    gel?.let {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = it.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                IconButton(
                                    onClick = {
                                        selectedGelIds = selectedGelIds - gelId
                                    },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove gel",
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save button
        Button(
            onClick = {
                val isNameValid = name.isNotBlank()
                nameError = !isNameValid

                if (isNameValid) {
                    val filteredSteps = steps
                        .filter { it.text.isNotBlank() }
                        .map { it.toStepInput() }
                    onSave(
                        name.trim(),
                        filteredSteps,
                        selectedGelIds,
                        selectedMainImageUri,
                        existingMainImagePath
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (nailStyleWithGels == null) "Save Style" else "Update Style")
        }
    }
}

@Composable
private fun StepInputFieldWithImage(
    stepNumber: Int,
    step: EditableStep,
    allGels: List<Gel>,
    onTextChange: (String) -> Unit,
    onGelSelected: (Long) -> Unit,
    onAddImage: () -> Unit,
    onRemoveImage: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDropdown by remember { mutableStateOf(false) }
    var dropdownGels by remember { mutableStateOf<List<Gel>>(emptyList()) }
    var atPosition by remember { mutableIntStateOf(-1) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Step $stepNumber",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove step",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Step image (optional)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onAddImage() },
                contentAlignment = Alignment.Center
            ) {
                if (step.displayImage != null) {
                    Box {
                        AsyncImage(
                            model = step.displayImage,
                            contentDescription = "Step $stepNumber image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        // Remove image button
                        IconButton(
                            onClick = onRemoveImage,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(28.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove image",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add step image",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add step image (optional)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Step text field
            Box {
                OutlinedTextField(
                    value = step.text,
                    onValueChange = { newValue ->
                        onTextChange(newValue)

                        // Check for @ character to trigger autocomplete
                        val lastAtIndex = newValue.lastIndexOf('@')
                        if (lastAtIndex >= 0) {
                            val afterAt = newValue.substring(lastAtIndex + 1)
                            if (!afterAt.contains(' ') && afterAt.length < 20) {
                                atPosition = lastAtIndex
                                dropdownGels = if (afterAt.isEmpty()) {
                                    allGels
                                } else {
                                    allGels.filter {
                                        it.name.contains(afterAt, ignoreCase = true)
                                    }
                                }
                                showDropdown = dropdownGels.isNotEmpty()
                            } else {
                                showDropdown = false
                            }
                        } else {
                            showDropdown = false
                        }
                    },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Type @ to insert a gel") },
                    minLines = 2
                )

                DropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = { showDropdown = false },
                    properties = PopupProperties(focusable = false),
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    dropdownGels.forEach { gel ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(parseColor(gel.colorCode))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(gel.name)
                                }
                            },
                            onClick = {
                                if (atPosition >= 0) {
                                    val beforeAt = step.text.substring(0, atPosition)
                                    val newValue = "$beforeAt${gel.name}"
                                    onTextChange(newValue)
                                    onGelSelected(gel.id)
                                }
                                showDropdown = false
                            }
                        )
                    }
                }
            }
        }
    }
}
