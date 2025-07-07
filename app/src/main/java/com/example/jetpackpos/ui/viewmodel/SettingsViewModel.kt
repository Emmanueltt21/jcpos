package com.example.jetpackpos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackpos.data.repository.OrderRepository
import com.example.jetpackpos.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SettingsEvent {
    object DatabaseResetSuccess : SettingsEvent()
    data class DatabaseResetError(val message: String) : SettingsEvent()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository
    // Consider injecting AppDatabase directly if you want a single "clearAllTables" method
    // on the database class itself, which might be cleaner.
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<SettingsEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

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
