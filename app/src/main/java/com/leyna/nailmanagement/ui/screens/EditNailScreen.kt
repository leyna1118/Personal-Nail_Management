package com.leyna.nailmanagement.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.leyna.nailmanagement.data.entity.Gel
import com.leyna.nailmanagement.data.entity.NailStyleWithGels
import com.leyna.nailmanagement.ui.viewmodel.NailStyleViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditNailContent(
    nailStyleWithGels: NailStyleWithGels?,
    allGels: List<Gel>,
    onSave: (name: String, steps: List<String>, gelIds: List<Long>) -> Unit,
    modifier: Modifier = Modifier
) {
    val existingSteps: List<String> = nailStyleWithGels?.let {
        NailStyleViewModel.parseSteps(it.nailStyle.steps)
    } ?: emptyList()

    val existingGelIds: List<Long> = nailStyleWithGels?.gels?.map { it.id } ?: emptyList()

    var name by rememberSaveable { mutableStateOf(nailStyleWithGels?.nailStyle?.name ?: "") }
    var steps by remember { mutableStateOf(existingSteps) }
    var selectedGelIds by remember { mutableStateOf(existingGelIds) }

    var nameError by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
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
            StepInputField(
                stepNumber = index + 1,
                value = step,
                allGels = allGels,
                onValueChange = { newValue ->
                    steps = steps.toMutableList().apply { set(index, newValue) }
                },
                onGelSelected = { gelId ->
                    if (gelId !in selectedGelIds) {
                        selectedGelIds = selectedGelIds + gelId
                    }
                },
                onRemove = {
                    steps = steps.toMutableList().apply { removeAt(index) }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Add step button
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    steps = steps + ""
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
                    val filteredSteps = steps.filter { it.isNotBlank() }
                    onSave(name.trim(), filteredSteps, selectedGelIds)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (nailStyleWithGels == null) "Save Style" else "Update Style")
        }
    }
}

@Composable
private fun StepInputField(
    stepNumber: Int,
    value: String,
    allGels: List<Gel>,
    onValueChange: (String) -> Unit,
    onGelSelected: (Long) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDropdown by remember { mutableStateOf(false) }
    var dropdownGels by remember { mutableStateOf<List<Gel>>(emptyList()) }
    var atPosition by remember { mutableStateOf(-1) }

    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = { newValue ->
                    onValueChange(newValue)

                    // Check for @ character to trigger autocomplete
                    val lastAtIndex = newValue.lastIndexOf('@')
                    if (lastAtIndex >= 0) {
                        val afterAt = newValue.substring(lastAtIndex + 1)
                        // Check if we're still typing after @
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
                label = { Text("Step $stepNumber") },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type @ to insert a gel") }
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove step"
                )
            }
        }

        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false },
            properties = PopupProperties(focusable = false),
            modifier = Modifier
                .heightIn(max = 200.dp)
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
                        // Replace @query with gel name
                        if (atPosition >= 0) {
                            val beforeAt = value.substring(0, atPosition)
                            val newValue = "$beforeAt${gel.name}"
                            onValueChange(newValue)
                            onGelSelected(gel.id)
                        }
                        showDropdown = false
                    }
                )
            }
        }
    }
}