package com.leyna.nailmanagement.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leyna.nailmanagement.data.dao.GelInventoryDao
import com.leyna.nailmanagement.data.entity.Gel
import com.leyna.nailmanagement.data.entity.GelInventory
import com.leyna.nailmanagement.data.repository.GelRepository
import com.leyna.nailmanagement.data.repository.ImageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GelViewModel(
    private val repository: GelRepository,
    private val imageRepository: ImageRepository,
    private val gelInventoryDao: GelInventoryDao
) : ViewModel() {

    val allGels: StateFlow<List<Gel>> = repository.allGels
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Autocomplete suggestions
    val distinctBrands: StateFlow<List<String>> = repository.distinctBrands
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val distinctSeries: StateFlow<List<String>> = repository.distinctSeries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val distinctCategories: StateFlow<List<String>> = repository.distinctCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val distinctStores: StateFlow<List<String>> = repository.distinctStores
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getGelById(id: Long): Flow<Gel?> = repository.getGelById(id)

    fun insertGel(
        name: String, price: Double, colorCode: String, imageUri: Uri?,
        brand: String? = null, series: String? = null, category: String? = null,
        store: String? = null, storeNote: String? = null, notes: String? = null
    ) {
        viewModelScope.launch {
            val imagePath = imageUri?.let { imageRepository.copyGelImageToStorage(it) }
            val gel = Gel(
                name = name, price = price, colorCode = colorCode, imagePath = imagePath,
                brand = brand, series = series, category = category,
                store = store, storeNote = storeNote, notes = notes
            )
            repository.insertGel(gel)
        }
    }

    fun deleteGels(ids: List<Long>) {
        viewModelScope.launch {
            val imagePaths = repository.deleteGels(ids)
            imagePaths.forEach { imageRepository.deleteImage(it) }
        }
    }

    fun updateGel(
        id: Long, name: String, price: Double, colorCode: String,
        imageUri: Uri?, existingImagePath: String?,
        brand: String? = null, series: String? = null, category: String? = null,
        store: String? = null, storeNote: String? = null, notes: String? = null
    ) {
        viewModelScope.launch {
            val imagePath = if (imageUri != null) {
                imageRepository.copyGelImageToStorage(imageUri)
            } else {
                existingImagePath
            }
            val gel = Gel(
                id = id, name = name, price = price, colorCode = colorCode, imagePath = imagePath,
                brand = brand, series = series, category = category,
                store = store, storeNote = storeNote, notes = notes
            )
            repository.updateGel(gel)
        }
    }

    // Store note lookup
    suspend fun getStoreNoteByStore(store: String): String? =
        repository.getStoreNoteByStore(store)

    fun updateStoreNoteForStore(store: String, storeNote: String?) {
        viewModelScope.launch { repository.updateStoreNoteForStore(store, storeNote) }
    }

    // Inventory
    fun getInventoryByGelId(gelId: Long): Flow<List<GelInventory>> =
        gelInventoryDao.getByGelId(gelId)

    fun insertInventory(inventory: GelInventory) {
        viewModelScope.launch { gelInventoryDao.insert(inventory) }
    }

    fun updateInventory(inventory: GelInventory) {
        viewModelScope.launch { gelInventoryDao.update(inventory) }
    }

    fun deleteInventory(id: Long) {
        viewModelScope.launch { gelInventoryDao.deleteById(id) }
    }
}
