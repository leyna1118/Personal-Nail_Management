package com.leyna.nailmanagement.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.leyna.nailmanagement.data.database.AppDatabase
import com.leyna.nailmanagement.data.entity.Gel
import com.leyna.nailmanagement.data.repository.GelRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class GelViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GelRepository
    private val context = application.applicationContext

    val allGels: StateFlow<List<Gel>>

    init {
        val gelDao = AppDatabase.getDatabase(application).gelDao()
        repository = GelRepository(gelDao)
        allGels = repository.allGels
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun getGelById(id: Long): Flow<Gel?> = repository.getGelById(id)

    fun insertGel(name: String, price: Double, colorCode: String, imageUri: Uri?) {
        viewModelScope.launch {
            val imagePath = imageUri?.let { copyImageToInternalStorage(it) }
            val gel = Gel(name = name, price = price, colorCode = colorCode, imagePath = imagePath)
            repository.insertGel(gel)
        }
    }

    fun updateGel(id: Long, name: String, price: Double, colorCode: String, imageUri: Uri?, existingImagePath: String?) {
        viewModelScope.launch {
            val imagePath = if (imageUri != null) {
                // New image selected, copy it
                copyImageToInternalStorage(imageUri)
            } else {
                // Keep existing image path
                existingImagePath
            }
            val gel = Gel(id = id, name = name, price = price, colorCode = colorCode, imagePath = imagePath)
            repository.updateGel(gel)
        }
    }

    private suspend fun copyImageToInternalStorage(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val imagesDir = File(context.filesDir, "gel_images")
                if (!imagesDir.exists()) {
                    imagesDir.mkdirs()
                }

                val fileName = "gel_${UUID.randomUUID()}.jpg"
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
}