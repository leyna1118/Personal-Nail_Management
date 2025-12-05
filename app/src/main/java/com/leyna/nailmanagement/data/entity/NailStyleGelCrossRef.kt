package com.leyna.nailmanagement.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = NailStyleGelCrossRef.TABLE_NAME,
    primaryKeys = [NailStyleGelCrossRef.COL_NAIL_STYLE_ID, NailStyleGelCrossRef.COL_GEL_ID],
    foreignKeys = [
        ForeignKey(
            entity = NailStyle::class,
            parentColumns = [NailStyle.COL_ID],
            childColumns = [NailStyleGelCrossRef.COL_NAIL_STYLE_ID],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Gel::class,
            parentColumns = [Gel.COL_ID],
            childColumns = [NailStyleGelCrossRef.COL_GEL_ID],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(NailStyleGelCrossRef.COL_NAIL_STYLE_ID), Index(NailStyleGelCrossRef.COL_GEL_ID)]
)
data class NailStyleGelCrossRef(
    val nailStyleId: Long,
    val gelId: Long
) {
    companion object {
        const val TABLE_NAME = "nail_style_gel_cross_ref"
        const val COL_NAIL_STYLE_ID = "nailStyleId"
        const val COL_GEL_ID = "gelId"
    }
}