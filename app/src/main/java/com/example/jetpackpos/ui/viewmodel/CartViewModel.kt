package com.example.jetpackpos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackpos.data.model.CartItem
import com.example.jetpackpos.data.model.Customer
import com.example.jetpackpos.data.model.Product
import com.example.jetpackpos.data.repository.CustomerRepository
import com.example.jetpackpos.data.repository.OrderRepository
import com.example.jetpackpos.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

val predefinedPaymentMethods = listOf("Cash", "Card", "PayPal", "Other") // Expanded list

data class CartUiState(
    // Cart items and totals
    val items: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val taxRate: Double = 0.0, // Example: 0.07 for 7% tax.
    val taxAmount: Double = 0.0,
    val total: Double = 0.0,

    // Checkout specific state
    val customers: List<Customer> = emptyList(),
    val selectedCustomerId: Long? = null,
    val selectedPaymentMethod: String = predefinedPaymentMethods.first(),
    val isGuestCheckout: Boolean = true,
    val isLoadingCustomers: Boolean = false,
    val isLoadingCheckout: Boolean = false
)

sealed class CartEvent {
    data class ItemAdded(val productName: String) : CartEvent()
    data class QuantityUpdated(val productName: String) : CartEvent()
    data class ItemRemoved(val productName: String) : CartEvent()
    data class StockUnavailable(val productName: String, val requested: Int, val available: Int) : CartEvent()
    data class OrderPlaced(val orderId: Long, val totalAmount: Double) : CartEvent() // Carries orderId for navigation
    data class Error(val message: String) : CartEvent()
    object CartCleared : CartEvent()
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
    private val customerRepository: CustomerRepository // Added
) : ViewModel() {

    private val _cartUiState = MutableStateFlow(CartUiState())
    val cartUiState: StateFlow<CartUiState> = _cartUiState.asStateFlow()

    private val _cartEvents = MutableSharedFlow<CartEvent>()
    val cartEvents = _cartEvents.asSharedFlow()

    init {
        loadCustomers()
        // Initialize with default tax rate from settings if available, or a default
        // For now, if ShopInfo provides tax, use it.
        // This requires ShopInfoRepository, or tax is passed differently.
        // Let's assume tax rate is managed by settings or a default for now.
    }

    private fun loadCustomers() {
        viewModelScope.launch {
            _cartUiState.update { it.copy(isLoadingCustomers = true) }
            customerRepository.getAllCustomers()
                .catch { e ->
                    _cartEvents.emit(CartEvent.Error("Failed to load customers: ${e.message}"))
                    _cartUiState.update { it.copy(isLoadingCustomers = false) }
                }
                .collect { customers ->
                    _cartUiState.update { it.copy(customers = customers, isLoadingCustomers = false) }
                }
        }
    }

    fun onCustomerSelected(customerId: Long?) {
        _cartUiState.update { it.copy(selectedCustomerId = customerId, isGuestCheckout = customerId == null) }
    }

    fun onToggleGuestCheckout(isGuest: Boolean) {
        _cartUiState.update {
            it.copy(
                isGuestCheckout = isGuest,
                selectedCustomerId = if (isGuest) null else it.selectedCustomerId
            )
        }
    }

    fun onPaymentMethodSelected(paymentMethod: String) {
        _cartUiState.update { it.copy(selectedPaymentMethod = paymentMethod) }
    }

    fun addProductToCart(product: Product, quantityToAdd: Int = 1) {
        viewModelScope.launch {
            if (quantityToAdd <= 0) return@launch

            val availableStock = product.quantity
            val existingCartItem = _cartUiState.value.items.find { it.productId == product.id }
            val currentQuantityInCart = existingCartItem?.quantityInCart ?: 0
            val newPotentialQuantity = currentQuantityInCart + quantityToAdd

            if (newPotentialQuantity > availableStock) {
                _cartEvents.emit(CartEvent.StockUnavailable(product.name, newPotentialQuantity, availableStock))
                return@launch
            }

            if (existingCartItem != null) {
                val updatedItems = _cartUiState.value.items.map {
                    if (it.productId == product.id) it.copy(quantityInCart = newPotentialQuantity) else it
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
                    availableStock = product.quantity
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
                val product = productRepository.getProductById(productId).firstOrNull()
                val actualAvailableStock = product?.quantity ?: itemToUpdate.availableStock

                if (newQuantity > actualAvailableStock) {
                    _cartEvents.emit(CartEvent.StockUnavailable(itemToUpdate.productName, newQuantity, actualAvailableStock))
                    val updatedItems = items.map {
                        if (it.productId == productId) it.copy(quantityInCart = actualAvailableStock) else it
                    }
                    _cartUiState.update { it.copy(items = updatedItems) }
                    _cartEvents.emit(CartEvent.QuantityUpdated(itemToUpdate.productName + " (max stock)"))
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
        // Preserve customer, payment method, and tax rate during cart clear
        _cartUiState.update { currentState ->
            currentState.copy(
                items = emptyList(),
                subtotal = 0.0,
                taxAmount = 0.0,
                total = 0.0
                // isLoadingCustomers, customers, selectedCustomerId, selectedPaymentMethod, isGuestCheckout, taxRate remain
            )
        }
        viewModelScope.launch { _cartEvents.emit(CartEvent.CartCleared) }
    }

    fun setTaxRate(newRate: Double) { // Rate as decimal, e.g., 0.07 for 7%
        if (newRate >= 0.0 && newRate <= 1.0) {
            _cartUiState.update { it.copy(taxRate = newRate) }
            recalculateTotals()
        } else {
            viewModelScope.launch { _cartEvents.emit(CartEvent.Error("Invalid tax rate.")) }
        }
    }

    private fun recalculateTotals() {
        _cartUiState.update { currentState ->
            val subtotal = currentState.items.sumOf { it.getTotalPrice() }
            val taxAmount = subtotal * currentState.taxRate
            val total = subtotal + taxAmount
            currentState.copy(subtotal = subtotal, taxAmount = taxAmount, total = total)
        }
    }

    fun checkout() {
        viewModelScope.launch {
            val currentCartState = _cartUiState.value
            if (currentCartState.items.isEmpty()) {
                _cartEvents.emit(CartEvent.Error("Cart is empty. Cannot checkout."))
                return@launch
            }
            _cartUiState.update { it.copy(isLoadingCheckout = true) }

            var stockIssueFound = false
            for (item in currentCartState.items) {
                val product = productRepository.getProductById(item.productId).firstOrNull()
                if (product == null || product.quantity < item.quantityInCart) {
                    _cartEvents.emit(CartEvent.StockUnavailable(item.productName, item.quantityInCart, product?.quantity ?: 0))
                    if (product == null || product.quantity == 0) removeProductFromCart(item.productId)
                    else updateQuantityInCart(item.productId, product.quantity)
                    stockIssueFound = true
                }
            }

            // State might have changed due to stock corrections, re-evaluate
            val validatedCartState = _cartUiState.value
            if (stockIssueFound) {
                 _cartUiState.update { it.copy(isLoadingCheckout = false) }
                _cartEvents.emit(CartEvent.Error("Stock levels changed. Please review your cart."))
                if (validatedCartState.items.isEmpty()) {
                     _cartEvents.emit(CartEvent.Error("Cart became empty due to stock adjustments."))
                }
                return@launch
            }

            val order = com.example.jetpackpos.data.model.Order(
                totalAmount = validatedCartState.total,
                customerId = if (validatedCartState.isGuestCheckout) null else validatedCartState.selectedCustomerId,
                paymentMethod = validatedCartState.selectedPaymentMethod
            )
            val orderItems = validatedCartState.items.map { cartItem ->
                com.example.jetpackpos.data.model.OrderItem(
                    orderId = 0, // Will be set by DAO
                    productId = cartItem.productId,
                    quantitySold = cartItem.quantityInCart,
                    priceAtPurchase = cartItem.price,
                    productNameAtPurchase = cartItem.productName,
                    productSkuAtPurchase = cartItem.productSku
                )
            }

            try {
                val newOrderId = orderRepository.insertOrderWithItems(order, orderItems)
                validatedCartState.items.forEach { cartItem ->
                    val product = productRepository.getProductById(cartItem.productId).firstOrNull()
                    if (product != null) {
                        val newQuantity = product.quantity - cartItem.quantityInCart
                        productRepository.updateStock(cartItem.productId, newQuantity.coerceAtLeast(0))
                    }
                }
                _cartEvents.emit(CartEvent.OrderPlaced(newOrderId, validatedCartState.total))
                clearCart() // Clear cart items, but keep customer/payment method selection for next potential order
                 _cartUiState.update { it.copy(isLoadingCheckout = false) }

            } catch (e: Exception) {
                _cartUiState.update { it.copy(isLoadingCheckout = false) }
                _cartEvents.emit(CartEvent.Error("Checkout failed: ${e.message}"))
            }
        }
    }
}
