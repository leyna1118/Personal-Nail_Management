package com.leyna.nailmanagement.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class NailStyleWithGels(
    @Embedded val nailStyle: NailStyle,
    @Relation(
        parentColumn = NailStyle.COL_ID,
        entityColumn = Gel.COL_ID,
        associateBy = Junction(
            value = NailStyleGelCrossRef::class,
            parentColumn = NailStyleGelCrossRef.COL_NAIL_STYLE_ID,
            entityColumn = NailStyleGelCrossRef.COL_GEL_ID
        )
    )
    val gels: List<Gel>
)