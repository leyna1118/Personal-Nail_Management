package com.leyna.nailmanagement.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.leyna.nailmanagement.data.entity.GelInventory
import kotlinx.coroutines.flow.Flow

@Dao
interface GelInventoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(inventory: GelInventory): Long

    @Update
    suspend fun update(inventory: GelInventory)

    @Query("SELECT * FROM gel_inventory WHERE gelId = :gelId ORDER BY CASE WHEN expiryDate IS NULL THEN 1 ELSE 0 END, expiryDate ASC")
    fun getByGelId(gelId: Long): Flow<List<GelInventory>>

    @Query("DELETE FROM gel_inventory WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM gel_inventory")
    suspend fun deleteAll()

    @Query("SELECT * FROM gel_inventory")
    suspend fun getAllSync(): List<GelInventory>
}
