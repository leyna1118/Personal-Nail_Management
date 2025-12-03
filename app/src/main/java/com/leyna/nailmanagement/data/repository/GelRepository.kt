package com.leyna.nailmanagement.data.repository

import com.leyna.nailmanagement.data.dao.GelDao
import com.leyna.nailmanagement.data.entity.Gel
import kotlinx.coroutines.flow.Flow

class GelRepository(private val gelDao: GelDao) {
    val allGels: Flow<List<Gel>> = gelDao.getAllGels()

    fun getGelById(id: Long): Flow<Gel?> = gelDao.getGelById(id)

    suspend fun insertGel(gel: Gel): Long = gelDao.insertGel(gel)

    suspend fun updateGel(gel: Gel) = gelDao.updateGel(gel)
}