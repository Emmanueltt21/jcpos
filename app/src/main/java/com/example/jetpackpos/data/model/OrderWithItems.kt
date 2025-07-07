package com.example.jetpackpos.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class OrderWithItems(
    @Embedded
    val order: Order,

    @Relation(
        parentColumn = "id", // From Order entity (parent)
        entityColumn = "order_id" // From OrderItem entity (child)
    )
    val items: List<OrderItem>
)
