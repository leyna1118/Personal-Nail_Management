package com.leyna.nailmanagement.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.leyna.nailmanagement.data.database.AppDatabase
import com.leyna.nailmanagement.data.entity.GelWithNailStyles
import com.leyna.nailmanagement.data.entity.NailStyle
import com.leyna.nailmanagement.data.entity.NailStyleWithGels
import com.leyna.nailmanagement.data.entity.StepWithImage
import com.leyna.nailmanagement.data.repository.NailStyleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Represents step data from the UI with potential new image URI
 */
data class StepInput(
    val text: String,
    val existingImagePath: String? = null,
    val newImageUri: Uri? = null
)

class NailStyleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NailStyleRepository
    private val context = application.applicationContext

    val allNailStylesWithGels: StateFlow<List<NailStyleWithGels>>
    val allGelsWithNailStyles: StateFlow<List<GelWithNailStyles>>

    init {
        val nailStyleDao = AppDatabase.getDatabase(application).nailStyleDao()
        repository = NailStyleRepository(nailStyleDao)

        allNailStylesWithGels = repository.getAllNailStylesWithGels()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        allGelsWithNailStyles = repository.getAllGelsWithNailStyles()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

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
            val mainImagePath = mainImageUri?.let { copyImageToInternalStorage(it, "nail_main") }
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
                copyImageToInternalStorage(mainImageUri, "nail_main")
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
                copyImageToInternalStorage(step.newImageUri, "nail_step")
            } else {
                step.existingImagePath
            }
            StepWithImage(text = step.text, imagePath = imagePath)
        }
    }

    private suspend fun copyImageToInternalStorage(uri: Uri, prefix: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val imagesDir = File(context.filesDir, "nail_images")
                if (!imagesDir.exists()) {
                    imagesDir.mkdirs()
                }

                val fileName = "${prefix}_${UUID.randomUUID()}.jpg"
                val destinationFile = File(imagesDir, fileName)

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    destinationFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                destinationFile.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
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

        // Legacy support for old format (plain text steps)
        fun parseStepsLegacy(stepsString: String): List<String> {
            return if (stepsString.isBlank()) {
                emptyList()
            } else {
                stepsString.split(STEP_SEPARATOR).map { it.split(FIELD_SEPARATOR).first() }
            }
        }
    }
}