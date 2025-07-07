package com.example.jetpackpos.data.model

import java.util.UUID

data class CartItem(
    val id: String = UUID.randomUUID().toString(), // Unique ID for list stability
    val productId: Long,
    val productName: String,
    val productSku: String,
    val price: Double, // Price per unit at the time of adding to cart
    var quantityInCart: Int,
    val availableStock: Int // To help validate against, fetched when product is added
) {
    init {
        require(price >= 0) { "Cart item price cannot be negative." }
        require(quantityInCart > 0) { "Cart item quantity must be positive." }
    }

    fun getTotalPrice(): Double {
        return price * quantityInCart
    }
}
