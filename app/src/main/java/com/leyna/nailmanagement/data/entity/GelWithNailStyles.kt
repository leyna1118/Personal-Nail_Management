package com.leyna.nailmanagement.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class GelWithNailStyles(
    @Embedded val gel: Gel,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = NailStyleGelCrossRef::class,
            parentColumn = "gelId",
            entityColumn = "nailStyleId"
        )
    )
    val nailStyles: List<NailStyle>
)