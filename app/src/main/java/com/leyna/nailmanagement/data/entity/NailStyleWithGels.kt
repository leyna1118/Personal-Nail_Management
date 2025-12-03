package com.leyna.nailmanagement.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class NailStyleWithGels(
    @Embedded val nailStyle: NailStyle,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = NailStyleGelCrossRef::class,
            parentColumn = "nailStyleId",
            entityColumn = "gelId"
        )
    )
    val gels: List<Gel>
)