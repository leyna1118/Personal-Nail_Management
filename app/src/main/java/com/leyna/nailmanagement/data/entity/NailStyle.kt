package com.leyna.nailmanagement.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nail_styles")
data class NailStyle(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val steps: String // JSON array stored as string: ["step1", "step2"]
)