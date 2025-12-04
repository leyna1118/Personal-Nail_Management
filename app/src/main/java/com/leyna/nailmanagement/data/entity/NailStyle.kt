package com.leyna.nailmanagement.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nail_styles")
data class NailStyle(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val steps: String, // Format: "stepText;;imagePath|||stepText;;imagePath|||..."
    val imagePath: String? = null // Main finished result image
)

/**
 * Represents a single step with optional image
 */
data class StepWithImage(
    val text: String,
    val imagePath: String? = null
)