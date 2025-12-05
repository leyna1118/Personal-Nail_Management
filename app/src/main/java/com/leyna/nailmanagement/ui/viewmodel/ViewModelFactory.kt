package com.leyna.nailmanagement.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.leyna.nailmanagement.data.database.AppDatabase
import com.leyna.nailmanagement.data.repository.GelRepository
import com.leyna.nailmanagement.data.repository.ImageRepository
import com.leyna.nailmanagement.data.repository.NailStyleRepository

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val database = AppDatabase.getDatabase(context)
        val imageRepository = ImageRepository(context.applicationContext)

        return when {
            modelClass.isAssignableFrom(GelViewModel::class.java) -> {
                val gelRepository = GelRepository(database.gelDao())
                GelViewModel(gelRepository, imageRepository) as T
            }
            modelClass.isAssignableFrom(NailStyleViewModel::class.java) -> {
                val nailStyleRepository = NailStyleRepository(database.nailStyleDao())
                NailStyleViewModel(nailStyleRepository, imageRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}