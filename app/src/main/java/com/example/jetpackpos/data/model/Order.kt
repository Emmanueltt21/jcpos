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
    val totalAmount: Double,

    @ColumnInfo(name = "customer_id", index = true) // Index for faster lookups if needed
    val customerId: Long? = null, // Nullable if order can be placed without a customer

    @ColumnInfo(name = "payment_method")
    val paymentMethod: String = "Cash" // Default payment method
) {
    init {
        require(totalAmount >= 0) { "Order total amount cannot be negative." }
        require(paymentMethod.isNotBlank()) { "Payment method cannot be blank."}
    }

    // Convenience getter for Date object from timestamp
    fun getOrderDate(): Date {
        return Date(timestamp)
    }
}
