package com.example.jetpackpos.ui.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackpos.data.model.Product
import com.example.jetpackpos.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ProductListUiState {
    object Loading : ProductListUiState
    data class Success(val products: List<Product>) : ProductListUiState
    data class Error(val message: String) : ProductListUiState
}



@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val productsUiState: StateFlow<ProductListUiState> =
        searchQuery
            .debounce(300) // Add a debounce to avoid too many queries while typing
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    productRepository.getAllProducts()
                } else {
                    productRepository.searchProductsByNameOrSku(query)
                }
            }
            .map<List<Product>, ProductListUiState> { products -> ProductListUiState.Success(products) }
            .onStart { emit(ProductListUiState.Loading) } // Emit Loading before starting the flow
            .catch { e -> emit(ProductListUiState.Error(e.message ?: "Unknown error")) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = ProductListUiState.Loading // Initial state
            )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            try {
                productRepository.deleteProduct(product)
                // Optionally, show a success message via a SharedFlow or similar event mechanism
                // e.g., _toastMessage.emit("Product deleted")
            } catch (e: Exception) {
                // Optionally, show an error message
                // e.g., _toastMessage.emit("Error deleting product: ${e.message}")
            }
        }
    }
}
