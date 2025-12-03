package com.leyna.nailmanagement.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.leyna.nailmanagement.data.entity.Gel
import kotlinx.coroutines.flow.Flow

@Dao
interface GelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGel(gel: Gel): Long

    @Update
    suspend fun updateGel(gel: Gel)

    @Query("SELECT * FROM gels ORDER BY name ASC")
    fun getAllGels(): Flow<List<Gel>>

    @Query("SELECT * FROM gels WHERE id = :id")
    fun getGelById(id: Long): Flow<Gel?>
}