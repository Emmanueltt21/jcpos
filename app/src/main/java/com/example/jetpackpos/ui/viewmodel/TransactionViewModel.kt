package com.example.jetpackpos.ui.viewmodel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
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



@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val uiState: StateFlow<TransactionListUiState> =
        searchQuery
            .debounce(300)
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    orderRepository.getAllOrdersWithItems()
                } else {
                    orderRepository.searchOrders(query)
                }
            }
            .map<List<OrderWithItems>, TransactionListUiState> { orders -> TransactionListUiState.Success(orders) }
            .onStart { emit(TransactionListUiState.Loading) }
            .catch { e -> emit(TransactionListUiState.Error(e.message ?: "Unknown error loading transactions")) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = TransactionListUiState.Loading
            )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }
}
