package com.example.jetpackpos.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackpos.data.model.OrderWithItems
import com.example.jetpackpos.data.repository.OrderRepository
import com.example.jetpackpos.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface OrderDetailsUiState {
    object Loading : OrderDetailsUiState
    data class Success(val orderWithItems: OrderWithItems) : OrderDetailsUiState
    data class Error(val message: String) : OrderDetailsUiState
    object NotFound : OrderDetailsUiState
}

@HiltViewModel
class OrderDetailsViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<OrderDetailsUiState>(OrderDetailsUiState.Loading)
    val uiState: StateFlow<OrderDetailsUiState> = _uiState.asStateFlow()

    private val orderId: Long = checkNotNull(savedStateHandle[Screen.OrderDetails.ARG_ORDER_ID])

    init {
        loadOrderDetails()
    }

    fun loadOrderDetails() { // Make public if refresh is needed
        _uiState.value = OrderDetailsUiState.Loading
        viewModelScope.launch {
            orderRepository.getOrderWithItemsById(orderId)
                .catch { e ->
                    _uiState.value = OrderDetailsUiState.Error(e.message ?: "Unknown error loading order details")
                }
                .collect { orderWithItems ->
                    if (orderWithItems != null) {
                        _uiState.value = OrderDetailsUiState.Success(orderWithItems)
                    } else {
                        _uiState.value = OrderDetailsUiState.NotFound
                    }
                }
        }
    }
}
