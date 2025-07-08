package com.example.jetpackpos.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shop_info")
data class ShopInfo(
    @PrimaryKey
    val id: Int = DEFAULT_SHOP_INFO_ID, // Always 1 for the single row

    @ColumnInfo(name = "shop_name")
    val shopName: String = "My POS Store",

    @ColumnInfo(name = "contact_number")
    val contactNumber: String? = null,

    @ColumnInfo(name = "email")
    val email: String? = null,

    @ColumnInfo(name = "address")
    val address: String? = null,

    @ColumnInfo(name = "currency_symbol")
    val currencySymbol: String = "$", // Default currency symbol

    @ColumnInfo(name = "tax_percentage")
    val taxPercentage: Double = 0.0 // Default tax rate as a decimal (e.g., 0.07 for 7%)
) {
    companion object {
        const val DEFAULT_SHOP_INFO_ID = 1
    }

    init {
        require(taxPercentage >= 0.0 && taxPercentage <= 1.0) { "Tax percentage must be between 0.0 and 1.0" }
    }
}
