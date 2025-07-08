package com.example.jetpackpos.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackpos.data.model.OrderWithItems
import com.example.jetpackpos.data.repository.OrderRepository
import com.example.jetpackpos.ui.navigation.OrderStatusScreen // For ARG_ORDER_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// Reusing OrderDetailsUiState as it's very similar
sealed interface OrderStatusUiState {
    object Loading : OrderStatusUiState
    data class Success(val orderWithItems: OrderWithItems) : OrderStatusUiState
    data class Error(val message: String) : OrderStatusUiState
    object NotFound : OrderStatusUiState
}

@HiltViewModel
class OrderStatusViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<OrderStatusUiState>(OrderStatusUiState.Loading)
    val uiState: StateFlow<OrderStatusUiState> = _uiState.asStateFlow()

    private val orderId: Long = checkNotNull(savedStateHandle[OrderStatusScreen.ARG_ORDER_ID])

    init {
        loadOrderDetails()
    }

    private fun loadOrderDetails() {
        _uiState.value = OrderStatusUiState.Loading
        viewModelScope.launch {
            orderRepository.getOrderWithItemsById(orderId)
                .catch { e ->
                    _uiState.value = OrderStatusUiState.Error(e.message ?: "Unknown error loading order details")
                }
                .collect { orderWithItems ->
                    if (orderWithItems != null) {
                        _uiState.value = OrderStatusUiState.Success(orderWithItems)
                    } else {
                        _uiState.value = OrderStatusUiState.NotFound
                    }
                }
        }
    }
}
