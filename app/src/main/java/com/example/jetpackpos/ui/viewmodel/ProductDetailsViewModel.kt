package com.example.jetpackpos.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackpos.data.model.Product
import com.example.jetpackpos.data.repository.ProductRepository
import com.example.jetpackpos.ui.navigation.Screen // Ensure this path is correct
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ProductDetailsUiState {
    object Loading : ProductDetailsUiState
    data class Success(val product: Product) : ProductDetailsUiState
    data class Error(val message: String) : ProductDetailsUiState
    object NotFound : ProductDetailsUiState
}

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductDetailsUiState>(ProductDetailsUiState.Loading)
    val uiState: StateFlow<ProductDetailsUiState> = _uiState.asStateFlow()

    private val productId: Long = checkNotNull(savedStateHandle[Screen.ProductDetails.ARG_PRODUCT_ID])

    init {
        loadProductDetails()
    }

    fun loadProductDetails() { // Public for potential refresh
        _uiState.value = ProductDetailsUiState.Loading
        viewModelScope.launch {
            productRepository.getProductById(productId)
                .catch { e ->
                    _uiState.value = ProductDetailsUiState.Error(e.message ?: "Unknown error loading product details")
                }
                .collect { product ->
                    if (product != null) {
                        _uiState.value = ProductDetailsUiState.Success(product)
                    } else {
                        _uiState.value = ProductDetailsUiState.NotFound
                    }
                }
        }
    }
}
