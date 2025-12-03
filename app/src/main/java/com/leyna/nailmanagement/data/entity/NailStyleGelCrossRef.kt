package com.leyna.nailmanagement.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "nail_style_gel_cross_ref",
    primaryKeys = ["nailStyleId", "gelId"],
    foreignKeys = [
        ForeignKey(
            entity = NailStyle::class,
            parentColumns = ["id"],
            childColumns = ["nailStyleId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Gel::class,
            parentColumns = ["id"],
            childColumns = ["gelId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("nailStyleId"), Index("gelId")]
)
data class NailStyleGelCrossRef(
    val nailStyleId: Long,
    val gelId: Long
)