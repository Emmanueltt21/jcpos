package com.example.jetpackpos.data.repository

import com.example.jetpackpos.data.db.OrderDao
import com.example.jetpackpos.data.model.Order
import com.example.jetpackpos.data.model.OrderItem
import com.example.jetpackpos.data.model.OrderWithItems
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(private val orderDao: OrderDao) {

    fun getAllOrdersWithItems(): Flow<List<OrderWithItems>> = orderDao.getAllOrdersWithItems()

    fun getOrderWithItemsById(orderId: Long): Flow<OrderWithItems?> = orderDao.getOrderWithItemsById(orderId)

    fun getOrdersByDateRange(startDate: Long, endDate: Long): Flow<List<OrderWithItems>> {
        return orderDao.getOrdersByDateRange(startDate, endDate)
    }

    suspend fun insertOrderWithItems(order: Order, items: List<OrderItem>): Long {
        // Basic validation can be done here or in ViewModel
        if (items.isEmpty()) {
            throw IllegalArgumentException("Cannot create an order with no items.")
        }
        // Ensure totalAmount matches sum of item prices * quantities (or handle in ViewModel)
        // val calculatedTotal = items.sumOf { it.priceAtPurchase * it.quantitySold }
        // if (order.totalAmount != calculatedTotal) {
        //     Log.w("OrderRepository", "Order totalAmount ${order.totalAmount} does not match calculated $calculatedTotal")
        //     // Decide on handling: throw error, log, or use calculated total
        // }
        return orderDao.insertOrderWithItems(order, items)
    }

    suspend fun clearAllOrderData() {
        orderDao.clearAllOrderData()
    }
}
