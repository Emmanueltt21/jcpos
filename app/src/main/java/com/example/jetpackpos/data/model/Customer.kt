package com.example.jetpackpos.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "customers",
    indices = [
        Index(value = ["phone_number"], unique = true), // Assuming phone number should be unique
        Index(value = ["email"], unique = true) // Assuming email should be unique if provided
    ]
)
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "phone_number")
    val phoneNumber: String?, // Nullable if not mandatory, but good for uniqueness if present

    @ColumnInfo(name = "email")
    val email: String?, // Nullable, but good for uniqueness if present

    @ColumnInfo(name = "address")
    val address: String?
) {
    init {
        require(name.isNotBlank()) { "Customer name cannot be blank." }
    }
}
