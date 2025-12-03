package com.leyna.nailmanagement.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.leyna.nailmanagement.data.database.AppDatabase
import com.leyna.nailmanagement.data.entity.Gel
import com.leyna.nailmanagement.data.repository.GelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GelViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GelRepository

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

    fun insertGel(name: String, price: Double, colorCode: String) {
        viewModelScope.launch {
            val gel = Gel(name = name, price = price, colorCode = colorCode)
            repository.insertGel(gel)
        }
    }

    fun updateGel(id: Long, name: String, price: Double, colorCode: String) {
        viewModelScope.launch {
            val gel = Gel(id = id, name = name, price = price, colorCode = colorCode)
            repository.updateGel(gel)
        }
    }
}