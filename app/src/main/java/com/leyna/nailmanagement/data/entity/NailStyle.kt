package com.leyna.nailmanagement.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = NailStyle.TABLE_NAME)
data class NailStyle(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val steps: String, // Format: "stepText;;imagePath|||stepText;;imagePath|||..."
    val imagePath: String? = null // Main finished result image
) {
    companion object {
        const val TABLE_NAME = "nail_styles"
        const val COL_ID = "id"
    }
}

/**
 * Represents a single step with optional image
 */
data class StepWithImage(
    val text: String,
    val imagePath: String? = null
)