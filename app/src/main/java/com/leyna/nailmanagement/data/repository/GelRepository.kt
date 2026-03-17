package com.leyna.nailmanagement.data.repository

import com.leyna.nailmanagement.data.dao.GelDao
import com.leyna.nailmanagement.data.dao.NailStyleDao
import com.leyna.nailmanagement.data.entity.Gel
import kotlinx.coroutines.flow.Flow

class GelRepository(
    private val gelDao: GelDao,
    private val nailStyleDao: NailStyleDao
) {
    val allGels: Flow<List<Gel>> = gelDao.getAllGels()

    val distinctBrands: Flow<List<String>> = gelDao.getDistinctBrands()
    val distinctSeries: Flow<List<String>> = gelDao.getDistinctSeries()
    val distinctCategories: Flow<List<String>> = gelDao.getDistinctCategories()
    val distinctStores: Flow<List<String>> = gelDao.getDistinctStores()

    fun getGelById(id: Long): Flow<Gel?> = gelDao.getGelById(id)

    suspend fun getStoreNoteByStore(store: String): String? = gelDao.getStoreNoteByStore(store)

    suspend fun updateStoreNoteForStore(store: String, storeNote: String?) =
        gelDao.updateStoreNoteForStore(store, storeNote)

    suspend fun insertGel(gel: Gel): Long = gelDao.insertGel(gel)

    suspend fun updateGel(gel: Gel) = gelDao.updateGel(gel)

    /**
     * Delete gels by IDs.
     * Before deletion, replaces all [[gel:ID]] mentions in NailStyle steps with the gel's name.
     * Returns the image paths of deleted gels so caller can clean up files.
     */
    suspend fun deleteGels(ids: List<Long>): List<String?> {
        // 1. Load gel names for mention replacement
        val gelNames = ids.mapNotNull { id ->
            gelDao.getGelByIdSync(id)?.let { id to it.name }
        }.toMap()

        // 2. Replace mentions in all nail style steps
        val allNailStyles = nailStyleDao.getAllNailStylesSync()
        for (nailStyle in allNailStyles) {
            var updatedSteps = nailStyle.steps
            for ((gelId, gelName) in gelNames) {
                updatedSteps = updatedSteps.replace("[[gel:$gelId]]", gelName)
            }
            if (updatedSteps != nailStyle.steps) {
                nailStyleDao.updateNailStyle(nailStyle.copy(steps = updatedSteps))
            }
        }

        // 3. Collect image paths before deletion
        val imagePaths = ids.mapNotNull { gelDao.getGelByIdSync(it)?.imagePath }

        // 4. Delete gels (cross-refs cascade automatically)
        gelDao.deleteGelsByIds(ids)

        return imagePaths
    }
}
