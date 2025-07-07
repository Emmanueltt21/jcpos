package com.example.jetpackpos.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.jetpackpos.data.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT * FROM products WHERE id = :productId")
    fun getProductById(productId: Long): Flow<Product?>

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE sku = :sku")
    fun getProductBySku(sku: String): Flow<Product?> // For checking SKU uniqueness or direct SKU lookup

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchProductsByNameOrSku(query: String): Flow<List<Product>>

    @Query("UPDATE products SET quantity = :newQuantity WHERE id = :productId")
    suspend fun updateStock(productId: Long, newQuantity: Int)

    // You might also want a method to get multiple products by ID, e.g., for cart restoration
    @Query("SELECT * FROM products WHERE id IN (:productIds)")
    fun getProductsByIds(productIds: List<Long>): Flow<List<Product>>

    // Get product quantity
    @Query("SELECT quantity FROM products WHERE id = :productId")
    suspend fun getProductQuantity(productId: Long): Int?

    @Query("DELETE FROM products")
    suspend fun clearAllProducts() // Method to delete all products
}
