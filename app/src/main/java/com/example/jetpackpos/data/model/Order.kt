package com.example.jetpackpos.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(), // Default to current time

    @ColumnInfo(name = "total_amount")
    val totalAmount: Double
) {
    init {
        require(totalAmount >= 0) { "Order total amount cannot be negative." }
    }

    // Convenience getter for Date object from timestamp
    fun getOrderDate(): Date {
        return Date(timestamp)
    }
}
