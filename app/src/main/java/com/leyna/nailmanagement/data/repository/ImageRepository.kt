package com.leyna.nailmanagement.data.repository

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class ImageRepository(private val context: Context) {

    private object Storage {
        const val GEL_DIRECTORY = "gel_images"
        const val NAIL_DIRECTORY = "nail_images"
        const val GEL_PREFIX = "gel_"
        const val NAIL_MAIN_PREFIX = "nail_main"
        const val NAIL_STEP_PREFIX = "nail_step"
        const val FILE_EXTENSION = ".jpg"
    }

    suspend fun copyGelImageToStorage(uri: Uri): String? {
        return copyImageToInternalStorage(
            uri = uri,
            directory = Storage.GEL_DIRECTORY,
            prefix = Storage.GEL_PREFIX
        )
    }

    suspend fun copyNailMainImageToStorage(uri: Uri): String? {
        return copyImageToInternalStorage(
            uri = uri,
            directory = Storage.NAIL_DIRECTORY,
            prefix = Storage.NAIL_MAIN_PREFIX
        )
    }

    suspend fun copyNailStepImageToStorage(uri: Uri): String? {
        return copyImageToInternalStorage(
            uri = uri,
            directory = Storage.NAIL_DIRECTORY,
            prefix = Storage.NAIL_STEP_PREFIX
        )
    }

    private suspend fun copyImageToInternalStorage(
        uri: Uri,
        directory: String,
        prefix: String
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                val imagesDir = File(context.filesDir, directory)
                if (!imagesDir.exists()) {
                    imagesDir.mkdirs()
                }

                val fileName = "${prefix}_${UUID.randomUUID()}${Storage.FILE_EXTENSION}"
                val destinationFile = File(imagesDir, fileName)

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    destinationFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                destinationFile.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}