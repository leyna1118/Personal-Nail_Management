package com.leyna.nailmanagement.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = GelInventory.TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = Gel::class,
            parentColumns = [Gel.COL_ID],
            childColumns = ["gelId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("gelId")]
)
data class GelInventory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gelId: Long,
    val purchaseDate: Long? = null,   // epoch millis
    val expiryDate: Long? = null,
    val usedUpDate: Long? = null,
    val writeOffDate: Long? = null,
    val note: String? = null
) {
    companion object {
        const val TABLE_NAME = "gel_inventory"
    }
}
