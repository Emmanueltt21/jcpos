package com.example.jetpackpos.data.repository

import com.example.jetpackpos.data.db.ProductDao
import com.example.jetpackpos.data.model.Product
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // Repositories are often singletons
class ProductRepository @Inject constructor(private val productDao: ProductDao) {

    fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()

    fun getProductById(productId: Long): Flow<Product?> = productDao.getProductById(productId)

    fun getProductBySku(sku: String): Flow<Product?> = productDao.getProductBySku(sku)

    fun searchProductsByNameOrSku(query: String): Flow<List<Product>> = productDao.searchProductsByNameOrSku(query)

    suspend fun insertProduct(product: Product): Long {
        return productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product)
    }

    suspend fun updateStock(productId: Long, newQuantity: Int) {
        productDao.updateStock(productId, newQuantity)
    }

    suspend fun getProductQuantity(productId: Long): Int? {
        return productDao.getProductQuantity(productId)
    }

    // This could be useful for validating if a product SKU already exists,
    // potentially returning a Boolean or the product itself.
    // Combining with getProductBySku which returns Flow, this might be more direct for some use cases.
    suspend fun isSkuExisting(sku: String, currentProductId: Long? = null): Boolean {
        // When checking for existing SKU, if currentProductId is provided (i.e., updating a product),
        // we should ignore the product itself from the check.
        // However, the current DAO getProductBySku doesn't support excluding an ID.
        // For simplicity, we can fetch the product by SKU and then check its ID.
        // A more optimized DAO method could be created if this becomes a performance bottleneck.
        val product = kotlinx.coroutines.flow.firstOrNull(productDao.getProductBySku(sku))
        return product != null && (currentProductId == null || product.id != currentProductId)
    }

    // Method to clear all products for database reset
    @Suppress("RedundantSuspendModifier") // Keeping suspend for consistency if DAO method becomes suspend
    suspend fun clearAllProducts() {
        // Room doesn't directly support deleteAll via DAO method without a Query.
        // We'd need to add @Query("DELETE FROM products") to ProductDao.
        // For now, let's add it to ProductDao and call it here.
        productDao.clearAllProducts() // Assuming this method will be added to ProductDao
    }
}
