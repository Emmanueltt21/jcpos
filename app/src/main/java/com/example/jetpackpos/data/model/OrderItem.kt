package com.example.jetpackpos.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = Order::class,
            parentColumns = ["id"],
            childColumns = ["order_id"],
            onDelete = ForeignKey.CASCADE // If an Order is deleted, its OrderItems are also deleted
        ),
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.SET_NULL // If a Product is deleted, keep the OrderItem but nullify product_id
                                         // Alternatively, could be RESTRICT to prevent product deletion if part of an order
        )
    ],
    indices = [
        androidx.room.Index(value = ["order_id"]),
        androidx.room.Index(value = ["product_id"])
    ]
)
data class OrderItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "order_id")
    val orderId: Long,

    @ColumnInfo(name = "product_id")
    val productId: Long?, // Nullable if product can be deleted

    @ColumnInfo(name = "quantity_sold") // Renamed to avoid conflict with Product.quantity
    val quantitySold: Int,

    @ColumnInfo(name = "price_at_purchase")
    val priceAtPurchase: Double,

    // Optionally, store product name/sku at time of purchase for historical data integrity
    // even if product details change or product is deleted.
    @ColumnInfo(name = "product_name_at_purchase")
    val productNameAtPurchase: String,

    @ColumnInfo(name = "product_sku_at_purchase")
    val productSkuAtPurchase: String
) {
    init {
        require(quantitySold > 0) { "Quantity sold must be positive." }
        require(priceAtPurchase >= 0) { "Price at purchase cannot be negative." }
    }
}
