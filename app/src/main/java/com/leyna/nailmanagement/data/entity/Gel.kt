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
    val imagePath: String? = null,
    val brand: String? = null,
    val series: String? = null,
    val category: String? = null,
    val store: String? = null,
    val storeNote: String? = null,
    val notes: String? = null
) {
    companion object {
        const val TABLE_NAME = "gels"
        const val COL_ID = "id"
    }
}