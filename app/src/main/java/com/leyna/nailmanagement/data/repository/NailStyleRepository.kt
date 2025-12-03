package com.leyna.nailmanagement.data.repository

import com.leyna.nailmanagement.data.dao.NailStyleDao
import com.leyna.nailmanagement.data.entity.GelWithNailStyles
import com.leyna.nailmanagement.data.entity.NailStyle
import com.leyna.nailmanagement.data.entity.NailStyleWithGels
import kotlinx.coroutines.flow.Flow

class NailStyleRepository(private val nailStyleDao: NailStyleDao) {

    fun getAllNailStylesWithGels(): Flow<List<NailStyleWithGels>> =
        nailStyleDao.getAllNailStylesWithGels()

    fun getNailStyleWithGelsById(id: Long): Flow<NailStyleWithGels?> =
        nailStyleDao.getNailStyleWithGelsById(id)

    fun getAllGelsWithNailStyles(): Flow<List<GelWithNailStyles>> =
        nailStyleDao.getAllGelsWithNailStyles()

    fun getGelWithNailStylesById(id: Long): Flow<GelWithNailStyles?> =
        nailStyleDao.getGelWithNailStylesById(id)

    suspend fun insertNailStyleWithGels(nailStyle: NailStyle, gelIds: List<Long>): Long =
        nailStyleDao.insertNailStyleWithGels(nailStyle, gelIds)

    suspend fun updateNailStyleWithGels(nailStyle: NailStyle, gelIds: List<Long>) =
        nailStyleDao.updateNailStyleWithGels(nailStyle, gelIds)
}