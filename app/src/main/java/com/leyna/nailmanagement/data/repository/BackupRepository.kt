package com.leyna.nailmanagement.data.repository

import android.content.Context
import com.leyna.nailmanagement.data.dao.GelDao
import com.leyna.nailmanagement.data.dao.GelInventoryDao
import com.leyna.nailmanagement.data.dao.NailStyleDao
import com.leyna.nailmanagement.data.entity.Gel
import com.leyna.nailmanagement.data.entity.GelInventory
import com.leyna.nailmanagement.data.entity.NailStyle
import com.leyna.nailmanagement.data.entity.StepWithImage
import com.leyna.nailmanagement.ui.viewmodel.NailStyleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class ImportResult(
    val gelCount: Int,
    val nailStyleCount: Int,
    val inventoryCount: Int = 0
)

class BackupRepository(
    private val context: Context,
    private val gelDao: GelDao,
    private val nailStyleDao: NailStyleDao,
    private val gelInventoryDao: GelInventoryDao
) {
    companion object {
        private const val GEL_IMAGES_DIR = "gel_images"
        private const val NAIL_IMAGES_DIR = "nail_images"
        private const val DATA_JSON = "data.json"
        private const val BACKUP_VERSION = 2
    }

    /**
     * Exports all gels, nail styles, cross-refs, inventory, and images to a ZIP file in cache dir.
     * Returns the File pointing to the created ZIP.
     */
    suspend fun exportToZip(): File = withContext(Dispatchers.IO) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dateStr = dateFormat.format(Date())
        val zipFile = File(context.cacheDir, "NailManagement_backup_$dateStr.zip")

        val gels = gelDao.getAllGels().first()
        val nailStylesWithGels = nailStyleDao.getAllNailStylesWithGels().first()

        ZipOutputStream(zipFile.outputStream()).use { zos ->
            // Build gels JSON array and copy gel images
            val gelsJsonArray = JSONArray()
            for (gel in gels) {
                val gelJson = JSONObject().apply {
                    put("id", gel.id)
                    put("name", gel.name)
                    put("price", gel.price)
                    put("colorCode", gel.colorCode)
                    put("brand", gel.brand ?: JSONObject.NULL)
                    put("series", gel.series ?: JSONObject.NULL)
                    put("category", gel.category ?: JSONObject.NULL)
                    put("store", gel.store ?: JSONObject.NULL)
                    put("storeNote", gel.storeNote ?: JSONObject.NULL)
                    put("notes", gel.notes ?: JSONObject.NULL)
                }

                if (gel.imagePath != null) {
                    val imageFile = File(gel.imagePath)
                    if (imageFile.exists()) {
                        val zipPath = "$GEL_IMAGES_DIR/${imageFile.name}"
                        gelJson.put("imageFile", zipPath)
                        zos.putNextEntry(ZipEntry(zipPath))
                        imageFile.inputStream().use { it.copyTo(zos) }
                        zos.closeEntry()
                    }
                }

                gelsJsonArray.put(gelJson)
            }

            // Build nail styles JSON array and copy nail images
            val nailStylesJsonArray = JSONArray()
            for (nailStyleWithGels in nailStylesWithGels) {
                val nailStyle = nailStyleWithGels.nailStyle
                val nailStyleJson = JSONObject().apply {
                    put("id", nailStyle.id)
                    put("name", nailStyle.name)
                }

                // Main image
                if (nailStyle.imagePath != null) {
                    val imageFile = File(nailStyle.imagePath)
                    if (imageFile.exists()) {
                        val zipPath = "$NAIL_IMAGES_DIR/${imageFile.name}"
                        nailStyleJson.put("imageFile", zipPath)
                        zos.putNextEntry(ZipEntry(zipPath))
                        imageFile.inputStream().use { it.copyTo(zos) }
                        zos.closeEntry()
                    }
                }

                // Steps with images
                val steps = NailStyleViewModel.parseSteps(nailStyle.steps)
                val stepsJsonArray = JSONArray()
                for (step in steps) {
                    val stepJson = JSONObject().apply {
                        put("text", step.text)
                    }
                    if (step.imagePath != null) {
                        val imageFile = File(step.imagePath)
                        if (imageFile.exists()) {
                            val zipPath = "$NAIL_IMAGES_DIR/${imageFile.name}"
                            stepJson.put("imageFile", zipPath)
                            zos.putNextEntry(ZipEntry(zipPath))
                            imageFile.inputStream().use { it.copyTo(zos) }
                            zos.closeEntry()
                        }
                    }
                    stepsJsonArray.put(stepJson)
                }
                nailStyleJson.put("steps", stepsJsonArray)

                // Gel IDs associated with this nail style
                val gelIds = nailStyleWithGels.gels.map { it.id }
                nailStyleJson.put("gelIds", JSONArray(gelIds))

                nailStylesJsonArray.put(nailStyleJson)
            }

            // Build inventory JSON array
            val inventoryJsonArray = JSONArray()
            val allInventory = gelInventoryDao.getAllSync()
            for (inv in allInventory) {
                val invJson = JSONObject().apply {
                    put("id", inv.id)
                    put("gelId", inv.gelId)
                    put("purchaseDate", inv.purchaseDate ?: JSONObject.NULL)
                    put("expiryDate", inv.expiryDate ?: JSONObject.NULL)
                    put("usedUpDate", inv.usedUpDate ?: JSONObject.NULL)
                    put("writeOffDate", inv.writeOffDate ?: JSONObject.NULL)
                    put("note", inv.note ?: JSONObject.NULL)
                }
                inventoryJsonArray.put(invJson)
            }

            // Build root JSON
            val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val rootJson = JSONObject().apply {
                put("version", BACKUP_VERSION)
                put("exportDate", dateTimeFormat.format(Date()))
                put("gels", gelsJsonArray)
                put("nailStyles", nailStylesJsonArray)
                put("inventory", inventoryJsonArray)
            }

            // Write data.json
            zos.putNextEntry(ZipEntry(DATA_JSON))
            zos.write(rootJson.toString(2).toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }

        zipFile
    }

    /**
     * Imports from a ZIP, clearing all existing data first.
     * Remaps gel IDs and gel references in step text.
     */
    suspend fun importFromZip(inputStream: InputStream): ImportResult = withContext(Dispatchers.IO) {
        // Extract ZIP to a temp directory first
        val tempDir = File(context.cacheDir, "backup_import_${UUID.randomUUID()}")
        tempDir.mkdirs()

        try {
            // Extract all entries
            ZipInputStream(inputStream).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        val outFile = File(tempDir, entry.name)
                        outFile.parentFile?.mkdirs()
                        outFile.outputStream().use { fos ->
                            zis.copyTo(fos)
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }

            // Parse data.json
            val dataFile = File(tempDir, DATA_JSON)
            val rootJson = JSONObject(dataFile.readText(Charsets.UTF_8))

            val gelsArray = rootJson.getJSONArray("gels")
            val nailStylesArray = rootJson.getJSONArray("nailStyles")

            // Clear all existing data
            nailStyleDao.deleteAllCrossRefs()
            nailStyleDao.deleteAllNailStyles()
            gelInventoryDao.deleteAll()
            gelDao.deleteAll()

            // Delete existing image directories
            val gelImagesDir = File(context.filesDir, GEL_IMAGES_DIR)
            val nailImagesDir = File(context.filesDir, NAIL_IMAGES_DIR)
            gelImagesDir.deleteRecursively()
            nailImagesDir.deleteRecursively()
            gelImagesDir.mkdirs()
            nailImagesDir.mkdirs()

            // Import gels, building oldId -> newId map
            val gelIdMap = mutableMapOf<Long, Long>()

            for (i in 0 until gelsArray.length()) {
                val gelJson = gelsArray.getJSONObject(i)
                val oldId = gelJson.getLong("id")

                // Copy image if present
                var imagePath: String? = null
                if (gelJson.has("imageFile") && !gelJson.isNull("imageFile")) {
                    val zipImagePath = gelJson.getString("imageFile")
                    val sourceFile = File(tempDir, zipImagePath)
                    if (sourceFile.exists()) {
                        val destFile = File(gelImagesDir, "gel_${UUID.randomUUID()}.jpg")
                        sourceFile.copyTo(destFile)
                        imagePath = destFile.absolutePath
                    }
                }

                val gel = Gel(
                    id = 0, // auto-generate
                    name = gelJson.getString("name"),
                    price = gelJson.getDouble("price"),
                    colorCode = gelJson.getString("colorCode"),
                    imagePath = imagePath,
                    brand = gelJson.optNullableString("brand"),
                    series = gelJson.optNullableString("series"),
                    category = gelJson.optNullableString("category"),
                    store = gelJson.optNullableString("store"),
                    storeNote = gelJson.optNullableString("storeNote"),
                    notes = gelJson.optNullableString("notes")
                )
                val newId = gelDao.insertGel(gel)
                gelIdMap[oldId] = newId
            }

            // Import nail styles
            var nailStyleCount = 0
            for (i in 0 until nailStylesArray.length()) {
                val nailStyleJson = nailStylesArray.getJSONObject(i)

                // Copy main image if present
                var mainImagePath: String? = null
                if (nailStyleJson.has("imageFile") && !nailStyleJson.isNull("imageFile")) {
                    val zipImagePath = nailStyleJson.getString("imageFile")
                    val sourceFile = File(tempDir, zipImagePath)
                    if (sourceFile.exists()) {
                        val destFile = File(nailImagesDir, "nail_main_${UUID.randomUUID()}.jpg")
                        sourceFile.copyTo(destFile)
                        mainImagePath = destFile.absolutePath
                    }
                }

                // Process steps
                val stepsArray = nailStyleJson.getJSONArray("steps")
                val steps = mutableListOf<StepWithImage>()
                for (j in 0 until stepsArray.length()) {
                    val stepJson = stepsArray.getJSONObject(j)
                    var stepText = stepJson.getString("text")

                    // Remap gel IDs in step text: [[gel:OLD_ID]] -> [[gel:NEW_ID]]
                    stepText = remapGelIds(stepText, gelIdMap)

                    // Copy step image if present
                    var stepImagePath: String? = null
                    if (stepJson.has("imageFile") && !stepJson.isNull("imageFile")) {
                        val zipImagePath = stepJson.getString("imageFile")
                        val sourceFile = File(tempDir, zipImagePath)
                        if (sourceFile.exists()) {
                            val destFile = File(nailImagesDir, "nail_step_${UUID.randomUUID()}.jpg")
                            sourceFile.copyTo(destFile)
                            stepImagePath = destFile.absolutePath
                        }
                    }

                    steps.add(StepWithImage(text = stepText, imagePath = stepImagePath))
                }

                val stepsString = NailStyleViewModel.encodeSteps(steps)

                // Remap gel IDs for cross-ref
                val gelIdsArray = nailStyleJson.getJSONArray("gelIds")
                val remappedGelIds = mutableListOf<Long>()
                for (j in 0 until gelIdsArray.length()) {
                    val oldGelId = gelIdsArray.getLong(j)
                    val newGelId = gelIdMap[oldGelId]
                    if (newGelId != null) {
                        remappedGelIds.add(newGelId)
                    }
                }

                val nailStyle = NailStyle(
                    id = 0, // auto-generate
                    name = nailStyleJson.getString("name"),
                    steps = stepsString,
                    imagePath = mainImagePath
                )

                nailStyleDao.insertNailStyleWithGels(nailStyle, remappedGelIds)
                nailStyleCount++
            }

            // Import inventory (v2+)
            var inventoryCount = 0
            if (rootJson.has("inventory")) {
                val inventoryArray = rootJson.getJSONArray("inventory")
                for (i in 0 until inventoryArray.length()) {
                    val invJson = inventoryArray.getJSONObject(i)
                    val oldGelId = invJson.getLong("gelId")
                    val newGelId = gelIdMap[oldGelId] ?: continue
                    val inventory = GelInventory(
                        id = 0,
                        gelId = newGelId,
                        purchaseDate = if (invJson.isNull("purchaseDate")) null else invJson.getLong("purchaseDate"),
                        expiryDate = if (invJson.isNull("expiryDate")) null else invJson.getLong("expiryDate"),
                        usedUpDate = if (invJson.isNull("usedUpDate")) null else invJson.getLong("usedUpDate"),
                        writeOffDate = if (invJson.isNull("writeOffDate")) null else invJson.getLong("writeOffDate"),
                        note = invJson.optNullableString("note")
                    )
                    gelInventoryDao.insert(inventory)
                    inventoryCount++
                }
            }

            ImportResult(
                gelCount = gelsArray.length(),
                nailStyleCount = nailStyleCount,
                inventoryCount = inventoryCount
            )
        } finally {
            // Clean up temp directory
            tempDir.deleteRecursively()
        }
    }

    /**
     * Remaps gel ID references in step text from old IDs to new IDs.
     * Pattern: [[gel:OLD_ID]] -> [[gel:NEW_ID]]
     */
    private fun remapGelIds(text: String, gelIdMap: Map<Long, Long>): String {
        return text.replace(Regex("""\[\[gel:(\d+)]]""")) { matchResult ->
            val oldId = matchResult.groupValues[1].toLongOrNull()
            val newId = oldId?.let { gelIdMap[it] }
            if (newId != null) {
                "[[gel:$newId]]"
            } else {
                matchResult.value // keep original if no mapping found
            }
        }
    }
}

/**
 * Helper to safely get a nullable String from JSONObject,
 * treating JSONObject.NULL and "null" as Kotlin null.
 */
private fun JSONObject.optNullableString(key: String): String? {
    if (!has(key) || isNull(key)) return null
    val value = getString(key)
    return if (value == "null") null else value
}
