package com.leyna.nailmanagement.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leyna.nailmanagement.data.entity.Gel
import com.leyna.nailmanagement.data.repository.GelRepository
import com.leyna.nailmanagement.data.repository.ImageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GelViewModel(
    private val repository: GelRepository,
    private val imageRepository: ImageRepository
) : ViewModel() {

    val allGels: StateFlow<List<Gel>> = repository.allGels
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getGelById(id: Long): Flow<Gel?> = repository.getGelById(id)

    fun insertGel(name: String, price: Double, colorCode: String, imageUri: Uri?) {
        viewModelScope.launch {
            val imagePath = imageUri?.let { imageRepository.copyGelImageToStorage(it) }
            val gel = Gel(name = name, price = price, colorCode = colorCode, imagePath = imagePath)
            repository.insertGel(gel)
        }
    }

    fun updateGel(id: Long, name: String, price: Double, colorCode: String, imageUri: Uri?, existingImagePath: String?) {
        viewModelScope.launch {
            val imagePath = if (imageUri != null) {
                imageRepository.copyGelImageToStorage(imageUri)
            } else {
                existingImagePath
            }
            val gel = Gel(id = id, name = name, price = price, colorCode = colorCode, imagePath = imagePath)
            repository.updateGel(gel)
        }
    }
}