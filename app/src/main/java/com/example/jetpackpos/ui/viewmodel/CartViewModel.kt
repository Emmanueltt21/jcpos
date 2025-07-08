package com.example.jetpackpos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackpos.data.model.CartItem
import com.example.jetpackpos.data.model.Product
import com.example.jetpackpos.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull // Added import
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val taxRate: Double = 0.0, // Example: 0.07 for 7% tax. Start with 0.
    val taxAmount: Double = 0.0,
    val total: Double = 0.0
)

import com.example.jetpackpos.data.repository.OrderRepository

sealed class CartEvent {
    data class ItemAdded(val productName: String) : CartEvent()
    data class QuantityUpdated(val productName: String) : CartEvent()
    data class ItemRemoved(val productName: String) : CartEvent()
    data class StockUnavailable(val productName: String, val requested: Int, val available: Int) : CartEvent()
    data class OrderPlaced(val orderId: Long, val totalAmount: Double) : CartEvent()
    data class Error(val message: String) : CartEvent()
    object CartCleared : CartEvent()
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository // Added OrderRepository
) : ViewModel() {

    private val _cartUiState = MutableStateFlow(CartUiState())
    val cartUiState: StateFlow<CartUiState> = _cartUiState.asStateFlow()

    private val _cartEvents = MutableSharedFlow<CartEvent>()
    val cartEvents = _cartEvents.asSharedFlow()

    fun addProductToCart(product: Product, quantityToAdd: Int = 1) {
        viewModelScope.launch {
            if (quantityToAdd <= 0) return@launch

            val availableStock = product.quantity // Current stock from the product object
            val existingCartItem = _cartUiState.value.items.find { it.productId == product.id }

            val currentQuantityInCart = existingCartItem?.quantityInCart ?: 0
            val newPotentialQuantity = currentQuantityInCart + quantityToAdd

            if (newPotentialQuantity > availableStock) {
                _cartEvents.emit(CartEvent.StockUnavailable(product.name, newPotentialQuantity, availableStock))
                return@launch
            }

            if (existingCartItem != null) {
                val updatedItems = _cartUiState.value.items.map {
                    if (it.productId == product.id) {
                        it.copy(quantityInCart = newPotentialQuantity)
                    } else it
                }
                _cartUiState.update { it.copy(items = updatedItems) }
                _cartEvents.emit(CartEvent.QuantityUpdated(product.name))
            } else {
                val newCartItem = CartItem(
                    productId = product.id,
                    productName = product.name,
                    productSku = product.sku,
                    price = product.price,
                    quantityInCart = quantityToAdd,
                    availableStock = product.quantity // Store initial stock for reference
                )
                _cartUiState.update { it.copy(items = it.items + newCartItem) }
                _cartEvents.emit(CartEvent.ItemAdded(product.name))
            }
            recalculateTotals()
        }
    }

    fun updateQuantityInCart(productId: Long, newQuantity: Int) {
        viewModelScope.launch {
            val items = _cartUiState.value.items
            val itemToUpdate = items.find { it.productId == productId } ?: return@launch

            if (newQuantity <= 0) {
                removeProductFromCart(productId)
            } else {
                // Here, we use the 'availableStock' stored in CartItem which was the stock at time of adding.
                // This might not reflect real-time stock if other sales happen.
                // For more robust stock checking during cart updates, we might need to re-fetch product.
                // For now, let's assume CartItem.availableStock is sufficient for this check.
                // Or, better yet, use the product's original full stock quantity as the limit.
                val product = productRepository.getProductById(productId).firstOrNull() // Corrected
                val actualAvailableStock = product?.quantity ?: itemToUpdate.availableStock // Fallback to cart item's stock

                if (newQuantity > actualAvailableStock) {
                    _cartEvents.emit(CartEvent.StockUnavailable(itemToUpdate.productName, newQuantity, actualAvailableStock))
                    // Optionally, set quantity to max available stock instead of outright rejecting
                    val updatedItems = items.map {
                        if (it.productId == productId) it.copy(quantityInCart = actualAvailableStock) else it
                    }
                    _cartUiState.update { it.copy(items = updatedItems) }
                    _cartEvents.emit(CartEvent.QuantityUpdated(itemToUpdate.productName + " (max stock applied)"))

                } else {
                     val updatedItems = items.map {
                        if (it.productId == productId) it.copy(quantityInCart = newQuantity) else it
                    }
                    _cartUiState.update { it.copy(items = updatedItems) }
                    _cartEvents.emit(CartEvent.QuantityUpdated(itemToUpdate.productName))
                }
            }
            recalculateTotals()
        }
    }

    fun removeProductFromCart(productId: Long) {
        val itemToRemove = _cartUiState.value.items.find { it.productId == productId }
        _cartUiState.update { currentState ->
            currentState.copy(items = currentState.items.filterNot { it.productId == productId })
        }
        itemToRemove?.let {
            viewModelScope.launch { _cartEvents.emit(CartEvent.ItemRemoved(it.productName)) }
        }
        recalculateTotals()
    }

    fun clearCart() {
        _cartUiState.value = CartUiState(taxRate = _cartUiState.value.taxRate) // Keep tax rate
        viewModelScope.launch { _cartEvents.emit(CartEvent.CartCleared) }
        // Totals are implicitly recalculated by setting a new state or can be forced.
    }

    fun setTaxRate(newRate: Double) {
        if (newRate >= 0.0 && newRate <= 1.0) { // Assuming rate is like 0.07 for 7%
            _cartUiState.update { it.copy(taxRate = newRate) }
            recalculateTotals()
        } else {
            viewModelScope.launch { _cartEvents.emit(CartEvent.Error("Invalid tax rate. Must be between 0.0 and 1.0.")) }
        }
    }


    private fun recalculateTotals() {
        _cartUiState.update { currentState ->
            val subtotal = currentState.items.sumOf { it.getTotalPrice() }
            val taxAmount = subtotal * currentState.taxRate
            val total = subtotal + taxAmount
            currentState.copy(
                subtotal = subtotal,
                taxAmount = taxAmount,
                total = total
            )
        }
    }

    // To be called during checkout to get final cart state for order creation
    fun getCurrentCartContents(): List<CartItem> {
        return _cartUiState.value.items
    }

    fun getCurrentCartTotal(): Double {
        return _cartUiState.value.total
    }

    fun checkout() {
        viewModelScope.launch {
            val currentCartState = _cartUiState.value
            if (currentCartState.items.isEmpty()) {
                _cartEvents.emit(CartEvent.Error("Cart is empty. Cannot checkout."))
                return@launch
            }

            // 1. Verify stock one last time (important step)
            val productIds = currentCartState.items.map { it.productId }
            // This is a simplified check. A real app might fetch all products at once.
            // For an offline app, the data might not change rapidly, but it's good practice.
            var stockIssueFound = false
            for (item in currentCartState.items) {
                val product = productRepository.getProductById(item.productId).firstOrNull() // Corrected
                if (product == null || product.quantity < item.quantityInCart) {
                    _cartEvents.emit(CartEvent.StockUnavailable(
                        item.productName,
                        item.quantityInCart,
                        product?.quantity ?: 0
                    ))
                    // Optionally, adjust cart item quantity to available stock or remove if 0
                    if (product == null || product.quantity == 0) {
                        removeProductFromCart(item.productId) // auto recalculates
                    } else {
                        updateQuantityInCart(item.productId, product.quantity) // auto recalculates
                    }
                    stockIssueFound = true
                }
            }

            if (stockIssueFound) {
                _cartEvents.emit(CartEvent.Error("Stock levels changed. Please review your cart."))
                // Totals would have been recalculated by remove/updateQuantityInCart
                return@launch
            }

            // If we reach here, stock is fine according to this final check.
            // Re-fetch the state because it might have changed if stock issues were corrected.
            val validatedCartState = _cartUiState.value
            if (validatedCartState.items.isEmpty() && stockIssueFound) { // Cart became empty due to stock correction
                 _cartEvents.emit(CartEvent.Error("Cart became empty due to stock adjustments. Cannot checkout."))
                return@launch
            }


            // 2. Create Order and OrderItems
            val order = com.example.jetpackpos.data.model.Order(totalAmount = validatedCartState.total)
            val orderItems = validatedCartState.items.map { cartItem ->
                com.example.jetpackpos.data.model.OrderItem(
                    // id is auto-generated by Room
                    orderId = 0, // Will be set by Room or DAO layer if Order is inserted first
                    productId = cartItem.productId,
                    quantitySold = cartItem.quantityInCart,
                    priceAtPurchase = cartItem.price,
                    productNameAtPurchase = cartItem.productName,
                    productSkuAtPurchase = cartItem.productSku
                )
            }

            try {
                // 3. Save Order and OrderItems (transactionally via OrderRepository)
                val newOrderId = orderRepository.insertOrderWithItems(order, orderItems)

                // 4. Update product stock
                // This should ideally be part of a larger transaction if the DB supported it across tables easily.
                // With Room, we do it sequentially. If stock update fails, order is still placed.
                // More robust systems might use a two-phase commit or compensating transactions.
                // For this offline app, sequential is acceptable.
                validatedCartState.items.forEach { cartItem -> // Changed from currentCartState to validatedCartState
                    val product = productRepository.getProductById(cartItem.productId).firstOrNull() // Corrected
                    if (product != null) {
                        val newQuantity = product.quantity - cartItem.quantityInCart
                        productRepository.updateStock(cartItem.productId, newQuantity.coerceAtLeast(0))
                    }
                }

                // 5. Clear cart
                clearCart() // This also recalculates totals to 0

                // 6. Emit success event
                _cartEvents.emit(CartEvent.OrderPlaced(newOrderId, validatedCartState.total))

            } catch (e: Exception) {
                _cartEvents.emit(CartEvent.Error("Checkout failed: ${e.message}"))
            }
        }
    }
}
