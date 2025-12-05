package com.leyna.nailmanagement.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = Gel.TABLE_NAME)
data class Gel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val price: Double,
    val colorCode: String,
    val imagePath: String? = null
) {
    companion object {
        const val TABLE_NAME = "gels"
        const val COL_ID = "id"
    }
}