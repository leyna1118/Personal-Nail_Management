package com.leyna.nailmanagement.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.leyna.nailmanagement.data.entity.GelWithNailStyles
import com.leyna.nailmanagement.data.entity.NailStyle
import com.leyna.nailmanagement.data.entity.NailStyleGelCrossRef
import com.leyna.nailmanagement.data.entity.NailStyleWithGels
import kotlinx.coroutines.flow.Flow

@Dao
interface NailStyleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNailStyle(nailStyle: NailStyle): Long

    @Update
    suspend fun updateNailStyle(nailStyle: NailStyle)

    @Query("SELECT * FROM nail_styles ORDER BY name ASC")
    fun getAllNailStyles(): Flow<List<NailStyle>>

    @Query("SELECT * FROM nail_styles WHERE id = :id")
    fun getNailStyleById(id: Long): Flow<NailStyle?>

    @Transaction
    @Query("SELECT * FROM nail_styles ORDER BY name ASC")
    fun getAllNailStylesWithGels(): Flow<List<NailStyleWithGels>>

    @Transaction
    @Query("SELECT * FROM nail_styles WHERE id = :id")
    fun getNailStyleWithGelsById(id: Long): Flow<NailStyleWithGels?>

    @Transaction
    @Query("SELECT * FROM gels ORDER BY name ASC")
    fun getAllGelsWithNailStyles(): Flow<List<GelWithNailStyles>>

    @Transaction
    @Query("SELECT * FROM gels WHERE id = :id")
    fun getGelWithNailStylesById(id: Long): Flow<GelWithNailStyles?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNailStyleGelCrossRef(crossRef: NailStyleGelCrossRef)

    @Query("DELETE FROM nail_style_gel_cross_ref WHERE nailStyleId = :nailStyleId")
    suspend fun deleteGelRefsForNailStyle(nailStyleId: Long)

    @Transaction
    suspend fun updateNailStyleWithGels(nailStyle: NailStyle, gelIds: List<Long>) {
        updateNailStyle(nailStyle)
        deleteGelRefsForNailStyle(nailStyle.id)
        gelIds.forEach { gelId ->
            insertNailStyleGelCrossRef(NailStyleGelCrossRef(nailStyle.id, gelId))
        }
    }

    @Transaction
    suspend fun insertNailStyleWithGels(nailStyle: NailStyle, gelIds: List<Long>): Long {
        val nailStyleId = insertNailStyle(nailStyle)
        gelIds.forEach { gelId ->
            insertNailStyleGelCrossRef(NailStyleGelCrossRef(nailStyleId, gelId))
        }
        return nailStyleId
    }
}