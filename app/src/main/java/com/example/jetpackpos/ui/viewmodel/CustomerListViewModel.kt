package com.example.jetpackpos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackpos.data.model.Customer
import com.example.jetpackpos.data.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CustomerListUiState {
    object Loading : CustomerListUiState
    data class Success(val customers: List<Customer>) : CustomerListUiState
    data class Error(val message: String) : CustomerListUiState
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class CustomerListViewModel @Inject constructor(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Consider adding a SharedFlow for delete success/error messages if needed
    private val _deleteEvent = MutableSharedFlow<String>()
    val deleteEvent = _deleteEvent.asSharedFlow()

    val customersUiState: StateFlow<CustomerListUiState> =
        searchQuery
            .debounce(300)
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    customerRepository.getAllCustomers()
                } else {
                    customerRepository.searchCustomers(query)
                }
            }
            .map<List<Customer>, CustomerListUiState> { customers -> CustomerListUiState.Success(customers) }
            .onStart { emit(CustomerListUiState.Loading) }
            .catch { e -> emit(CustomerListUiState.Error(e.message ?: "Unknown error")) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = CustomerListUiState.Loading
            )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            try {
                customerRepository.deleteCustomer(customer)
                _deleteEvent.emit("Customer '${customer.name}' deleted successfully.")
            } catch (e: Exception) {
                 _deleteEvent.emit("Error deleting customer: ${e.message}")
            }
        }
    }
}
