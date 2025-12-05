package com.leyna.nailmanagement.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leyna.nailmanagement.data.entity.GelWithNailStyles
import com.leyna.nailmanagement.data.entity.NailStyle
import com.leyna.nailmanagement.data.entity.NailStyleWithGels
import com.leyna.nailmanagement.data.entity.StepWithImage
import com.leyna.nailmanagement.data.repository.ImageRepository
import com.leyna.nailmanagement.data.repository.NailStyleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Represents step data from the UI with potential new image URI
 */
data class StepInput(
    val text: String,
    val existingImagePath: String? = null,
    val newImageUri: Uri? = null
)

class NailStyleViewModel(
    private val repository: NailStyleRepository,
    private val imageRepository: ImageRepository
) : ViewModel() {

    val allNailStylesWithGels: StateFlow<List<NailStyleWithGels>> = repository.getAllNailStylesWithGels()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allGelsWithNailStyles: StateFlow<List<GelWithNailStyles>> = repository.getAllGelsWithNailStyles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getNailStyleWithGelsById(id: Long): Flow<NailStyleWithGels?> =
        repository.getNailStyleWithGelsById(id)

    fun getGelWithNailStylesById(id: Long): Flow<GelWithNailStyles?> =
        repository.getGelWithNailStylesById(id)

    fun insertNailStyle(
        name: String,
        steps: List<StepInput>,
        gelIds: List<Long>,
        mainImageUri: Uri?
    ) {
        viewModelScope.launch {
            val mainImagePath = mainImageUri?.let { imageRepository.copyNailMainImageToStorage(it) }
            val processedSteps = processSteps(steps)
            val stepsString = encodeSteps(processedSteps)
            val nailStyle = NailStyle(name = name, steps = stepsString, imagePath = mainImagePath)
            repository.insertNailStyleWithGels(nailStyle, gelIds)
        }
    }

    fun updateNailStyle(
        id: Long,
        name: String,
        steps: List<StepInput>,
        gelIds: List<Long>,
        mainImageUri: Uri?,
        existingMainImagePath: String?
    ) {
        viewModelScope.launch {
            val mainImagePath = if (mainImageUri != null) {
                imageRepository.copyNailMainImageToStorage(mainImageUri)
            } else {
                existingMainImagePath
            }
            val processedSteps = processSteps(steps)
            val stepsString = encodeSteps(processedSteps)
            val nailStyle = NailStyle(id = id, name = name, steps = stepsString, imagePath = mainImagePath)
            repository.updateNailStyleWithGels(nailStyle, gelIds)
        }
    }

    private suspend fun processSteps(steps: List<StepInput>): List<StepWithImage> {
        return steps.map { step ->
            val imagePath = if (step.newImageUri != null) {
                imageRepository.copyNailStepImageToStorage(step.newImageUri)
            } else {
                step.existingImagePath
            }
            StepWithImage(text = step.text, imagePath = imagePath)
        }
    }

    companion object {
        private const val STEP_SEPARATOR = "|||"
        private const val FIELD_SEPARATOR = ";;"

        fun parseSteps(stepsString: String): List<StepWithImage> {
            if (stepsString.isBlank()) return emptyList()

            return stepsString.split(STEP_SEPARATOR).map { stepData ->
                val parts = stepData.split(FIELD_SEPARATOR, limit = 2)
                val text = parts.getOrElse(0) { "" }
                val imagePath = parts.getOrNull(1)?.takeIf { it.isNotBlank() }
                StepWithImage(text = text, imagePath = imagePath)
            }
        }

        fun encodeSteps(steps: List<StepWithImage>): String {
            return steps.joinToString(STEP_SEPARATOR) { step ->
                "${step.text}$FIELD_SEPARATOR${step.imagePath ?: ""}"
            }
        }
    }
}