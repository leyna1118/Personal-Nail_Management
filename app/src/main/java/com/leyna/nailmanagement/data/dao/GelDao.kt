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

    @Query("SELECT * FROM gels WHERE id = :id")
    suspend fun getGelByIdSync(id: Long): Gel?

    @Query("DELETE FROM gels WHERE id IN (:ids)")
    suspend fun deleteGelsByIds(ids: List<Long>)

    @Query("DELETE FROM gels")
    suspend fun deleteAll()

    @Query("SELECT DISTINCT brand FROM gels WHERE brand IS NOT NULL AND brand != '' ORDER BY brand ASC")
    fun getDistinctBrands(): Flow<List<String>>

    @Query("SELECT DISTINCT series FROM gels WHERE series IS NOT NULL AND series != '' ORDER BY series ASC")
    fun getDistinctSeries(): Flow<List<String>>

    @Query("SELECT DISTINCT category FROM gels WHERE category IS NOT NULL AND category != '' ORDER BY category ASC")
    fun getDistinctCategories(): Flow<List<String>>

    @Query("SELECT DISTINCT store FROM gels WHERE store IS NOT NULL AND store != '' ORDER BY store ASC")
    fun getDistinctStores(): Flow<List<String>>

    @Query("SELECT storeNote FROM gels WHERE store = :store AND storeNote IS NOT NULL AND storeNote != '' LIMIT 1")
    suspend fun getStoreNoteByStore(store: String): String?

    @Query("UPDATE gels SET storeNote = :storeNote WHERE store = :store")
    suspend fun updateStoreNoteForStore(store: String, storeNote: String?)
}