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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.leyna.nailmanagement.R
import com.leyna.nailmanagement.data.entity.Gel
import com.leyna.nailmanagement.data.entity.NailStyleWithGels
import com.leyna.nailmanagement.data.entity.StepWithImage
import com.leyna.nailmanagement.ui.components.MentionTextField
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

@Composable
fun EditNailContent(
    nailStyleWithGels: NailStyleWithGels?,
    allGels: List<Gel>,
    onSave: (
        name: String,
        steps: List<StepInput>,
        mainImageUri: Uri?,
        existingMainImagePath: String?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val existingSteps: List<StepWithImage> = nailStyleWithGels?.let {
        NailStyleViewModel.parseSteps(it.nailStyle.steps)
    } ?: emptyList()

    val existingMainImagePath = nailStyleWithGels?.nailStyle?.imagePath

    var name by rememberSaveable { mutableStateOf(nailStyleWithGels?.nailStyle?.name ?: "") }
    var steps by remember {
        mutableStateOf(existingSteps.map { EditableStep(it.text, it.imagePath) })
    }
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
            text = stringResource(R.string.label_finished_result_image),
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
                    contentDescription = stringResource(R.string.cd_main_nail_image),
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
                        text = stringResource(R.string.tap_to_add_finished_photo),
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
            label = { Text(stringResource(R.string.label_style_name)) },
            isError = nameError,
            supportingText = if (nameError) {
                { Text(stringResource(R.string.error_name_required)) }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Steps section
        Text(
            text = stringResource(R.string.label_steps),
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
                    contentDescription = stringResource(R.string.action_add_step),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.action_add_step),
                    color = MaterialTheme.colorScheme.primary
                )
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
                        selectedMainImageUri,
                        existingMainImagePath
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (nailStyleWithGels == null) stringResource(R.string.action_save_style) else stringResource(R.string.action_update_style))
        }
    }
}

@Composable
private fun StepInputFieldWithImage(
    stepNumber: Int,
    step: EditableStep,
    allGels: List<Gel>,
    onTextChange: (String) -> Unit,
    onAddImage: () -> Unit,
    onRemoveImage: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                    text = stringResource(R.string.label_step_number, stepNumber),
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
                        contentDescription = stringResource(R.string.cd_remove_step),
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
                            contentDescription = stringResource(R.string.cd_step_image, stepNumber),
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
                                contentDescription = stringResource(R.string.cd_remove_image),
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.cd_add_step_image),
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.tap_to_add_step_image),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Step text field with mention support
            MentionTextField(
                storageText = step.text,
                onStorageTextChange = onTextChange,
                allGels = allGels,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.label_description)) },
                placeholder = { Text(stringResource(R.string.hint_type_at_to_insert_gel)) },
                minLines = 2
            )
        }
    }
}
