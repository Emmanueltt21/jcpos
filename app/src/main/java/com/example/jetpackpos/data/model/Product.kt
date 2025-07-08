package com.example.jetpackpos.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [Index(value = ["sku"], unique = true)]
)
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "price")
    val price: Double,

    @ColumnInfo(name = "sku")
    val sku: String,

    @ColumnInfo(name = "quantity")
    val quantity: Int,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "image_uri")
    val imageUri: String? = null // Store path or URL to the image
) {
    // It's good practice to ensure price and quantity are non-negative
    init {
        require(price >= 0) { "Product price cannot be negative." }
        require(quantity >= 0) { "Product quantity cannot be negative." }
    }
}
