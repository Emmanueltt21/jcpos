package com.example.jetpackpos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackpos.data.repository.OrderRepository
import com.example.jetpackpos.data.model.ShopInfo
import com.example.jetpackpos.data.repository.ProductRepository
import com.example.jetpackpos.data.repository.ShopInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SettingsEvent {
    object DatabaseResetSuccess : SettingsEvent()
    data class DatabaseResetError(val message: String) : SettingsEvent()
}


sealed interface ShopInfoUiState {
    object Loading : ShopInfoUiState
    data class Success(val shopInfo: ShopInfo) : ShopInfoUiState
    data class Error(val message: String) : ShopInfoUiState
}


@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
    private val shopInfoRepository: ShopInfoRepository // Added
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<SettingsEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val _shopInfoState = MutableStateFlow<ShopInfoUiState>(ShopInfoUiState.Loading)
    val shopInfoState: StateFlow<ShopInfoUiState> = _shopInfoState.asStateFlow()

    init {
        loadShopInfo()
    }

    fun loadShopInfo() { // Public in case of refresh needed from UI
        viewModelScope.launch {
            _shopInfoState.value = ShopInfoUiState.Loading
            shopInfoRepository.getShopInfo()
                .catch { e -> _shopInfoState.value = ShopInfoUiState.Error(e.message ?: "Failed to load shop info") }
                .collect { info ->
                    _shopInfoState.value = ShopInfoUiState.Success(info)
                }
        }
    }

    fun resetDatabase() {
        viewModelScope.launch {
            try {
                // It's generally better to clear orders/orderItems first if there are foreign key constraints
                // that might prevent products from being deleted if they are referenced.
                // However, our OrderItem's FK to Product is ON DELETE SET_NULL, so order doesn't strictly matter.
                // Still, clearing dependent data first is a good habit.
                orderRepository.clearAllOrderData()
                productRepository.clearAllProducts()
                // If other tables existed, clear them here too.
                _eventFlow.emit(SettingsEvent.DatabaseResetSuccess)
            } catch (e: Exception) {
                _eventFlow.emit(SettingsEvent.DatabaseResetError(e.message ?: "Unknown error during database reset."))
            }
        }
    }
}
