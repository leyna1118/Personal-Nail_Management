package com.leyna.nailmanagement.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.leyna.nailmanagement.data.database.AppDatabase
import com.leyna.nailmanagement.data.entity.GelWithNailStyles
import com.leyna.nailmanagement.data.entity.NailStyle
import com.leyna.nailmanagement.data.entity.NailStyleWithGels
import com.leyna.nailmanagement.data.repository.NailStyleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NailStyleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NailStyleRepository

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

    fun insertNailStyle(name: String, steps: List<String>, gelIds: List<Long>) {
        viewModelScope.launch {
            val stepsJson = steps.joinToString(separator = "|||")
            val nailStyle = NailStyle(name = name, steps = stepsJson)
            repository.insertNailStyleWithGels(nailStyle, gelIds)
        }
    }

    fun updateNailStyle(id: Long, name: String, steps: List<String>, gelIds: List<Long>) {
        viewModelScope.launch {
            val stepsJson = steps.joinToString(separator = "|||")
            val nailStyle = NailStyle(id = id, name = name, steps = stepsJson)
            repository.updateNailStyleWithGels(nailStyle, gelIds)
        }
    }

    companion object {
        fun parseSteps(stepsString: String): List<String> {
            return if (stepsString.isBlank()) {
                emptyList()
            } else {
                stepsString.split("|||")
            }
        }
    }
}