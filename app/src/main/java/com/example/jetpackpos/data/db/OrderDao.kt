package com.example.jetpackpos.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.jetpackpos.data.model.Order
import com.example.jetpackpos.data.model.OrderItem
import com.example.jetpackpos.data.model.OrderWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long // Returns the auto-generated ID of the order

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItem>) // Inserts a list of order items

    @Transaction
    suspend fun insertOrderWithItems(order: Order, items: List<OrderItem>): Long {
        val orderId = insertOrder(order)
        val itemsWithOrderId = items.map { it.copy(orderId = orderId) }
        insertOrderItems(itemsWithOrderId)
        return orderId
    }

    @Transaction // Ensures that the Order and its OrderItems are loaded together
    @Query("SELECT * FROM orders WHERE id = :orderId")
    fun getOrderWithItemsById(orderId: Long): Flow<OrderWithItems?>

    @Transaction
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrdersWithItems(): Flow<List<OrderWithItems>>

    @Transaction
    @Query("SELECT * FROM orders WHERE timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    fun getOrdersByDateRange(startDate: Long, endDate: Long): Flow<List<OrderWithItems>>

    // Potentially a method to delete all orders and items for database reset
    @Query("DELETE FROM orders")
    suspend fun clearOrders()

    @Query("DELETE FROM order_items")
    suspend fun clearOrderItems()

    @Transaction
    suspend fun clearAllOrderData() {
        clearOrderItems() // Clear items first due to foreign key constraints if any were RESTRICT
        clearOrders()
    }
}
