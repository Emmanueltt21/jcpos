package com.example.jetpackpos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackpos.data.model.OrderWithItems
import com.example.jetpackpos.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TransactionListUiState {
    object Loading : TransactionListUiState
    data class Success(val orders: List<OrderWithItems>) : TransactionListUiState
    data class Error(val message: String) : TransactionListUiState
}

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TransactionListUiState>(TransactionListUiState.Loading)
    val uiState: StateFlow<TransactionListUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            orderRepository.getAllOrdersWithItems()
                .map<List<OrderWithItems>, TransactionListUiState> { orders -> TransactionListUiState.Success(orders) }
                .catch { e -> _uiState.value = TransactionListUiState.Error(e.message ?: "Unknown error loading transactions") }
                .stateIn( // Convert to StateFlow
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000L),
                    initialValue = TransactionListUiState.Loading
                )
                .collect { MappedState -> // Collect the mapped state
                    _uiState.value = MappedState
                }
        }
    }
}
