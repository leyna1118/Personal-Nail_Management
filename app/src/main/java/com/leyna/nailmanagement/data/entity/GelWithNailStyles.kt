package com.leyna.nailmanagement.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class GelWithNailStyles(
    @Embedded val gel: Gel,
    @Relation(
        parentColumn = Gel.COL_ID,
        entityColumn = NailStyle.COL_ID,
        associateBy = Junction(
            value = NailStyleGelCrossRef::class,
            parentColumn = NailStyleGelCrossRef.COL_GEL_ID,
            entityColumn = NailStyleGelCrossRef.COL_NAIL_STYLE_ID
        )
    )
    val nailStyles: List<NailStyle>
)