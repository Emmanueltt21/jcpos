package com.example.jetpackpos.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackpos.data.model.Product
import com.example.jetpackpos.data.repository.ProductRepository
import com.example.jetpackpos.ui.navigation.Screen // Corrected from AddEditProduct to Screen.AddEditProduct
import com.example.jetpackpos.data.repository.CategoryRepository // Moved to top
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.* // Wildcard for flow operators
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductFormState(
    val name: String = "",
    val price: String = "",
    val sku: String = "",
    val quantity: String = "",
    val category: String = "",
    val description: String = "",
    val imageUri: String? = null,
    val categories: List<com.example.jetpackpos.data.model.Category> = emptyList(),
    val isLoadingCategories: Boolean = false,
    val nameError: String? = null,
    val priceError: String? = null,
    val skuError: String? = null,
    val quantityError: String? = null,
    val categoryError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val currentProductId: Long? = null
)

sealed class AddEditProductEvent {
    object ProductSaved : AddEditProductEvent()
    data class Error(val message: String) : AddEditProductEvent()
}

@HiltViewModel
class AddEditProductViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _formState = MutableStateFlow(ProductFormState())
    val formState: StateFlow<ProductFormState> = _formState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<AddEditProductEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val currentProductId: Long? = savedStateHandle.get<String>(Screen.AddEditProduct.ARG_PRODUCT_ID)?.toLongOrNull()

    init {
        if (currentProductId != null) {
            _formState.value = _formState.value.copy(isLoading = true, isEditing = true, currentProductId = currentProductId)
            loadProduct(currentProductId)
        }
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _formState.update { it.copy(isLoadingCategories = true) }
            categoryRepository.getAllCategories()
                .catch { e ->
                    _formState.update { it.copy(isLoadingCategories = false, generalError = "Failed to load categories: ${e.message}") }
                }
                .collect { categories ->
                    _formState.update { it.copy(categories = categories, isLoadingCategories = false) }
                }
        }
    }

    fun onNameChange(name: String) {
        _formState.update { it.copy(name = name, nameError = null, generalError = null) }
    }

    fun onPriceChange(price: String) {
        _formState.update { it.copy(price = price, priceError = null, generalError = null) }
    }

    fun onSkuChange(sku: String) {
        _formState.update { it.copy(sku = sku, skuError = null, generalError = null) }
    }

    fun onQuantityChange(quantity: String) {
        _formState.update { it.copy(quantity = quantity, quantityError = null, generalError = null) }
    }

    fun onCategoryChange(category: String) {
        _formState.update { it.copy(category = category, categoryError = null, generalError = null) }
    }

    fun onDescriptionChange(description: String) {
        _formState.update { it.copy(description = description, generalError = null) }
    }

    fun onImageUriChange(uri: String?) {
        _formState.update { it.copy(imageUri = uri) }
    }

    private fun loadProduct(productId: Long) {
        viewModelScope.launch {
            val product = productRepository.getProductById(productId).firstOrNull()
            if (product != null) {
                _formState.update {
                    it.copy(
                        name = product.name,
                        price = product.price.toString(),
                        sku = product.sku,
                        quantity = product.quantity.toString(),
                        category = product.category,
                        description = product.description ?: "",
                        imageUri = product.imageUri,
                        isLoading = false
                    )
                }
            } else {
                _formState.update { it.copy(isLoading = false, generalError = "Failed to load product.") }
                _eventFlow.emit(AddEditProductEvent.Error("Product not found."))
            }
        }
    }

    fun saveProduct() {
        val currentFormState = _formState.value
        if (!validateForm(currentFormState)) return

        _formState.update { it.copy(isLoading = true, generalError = null) }

        viewModelScope.launch {
            try {
                val priceDouble = currentFormState.price.toDoubleOrNull()
                val quantityInt = currentFormState.quantity.toIntOrNull()

                if (priceDouble == null || quantityInt == null) {
                    _formState.update { it.copy(isLoading = false, generalError = "Invalid price or quantity format.") }
                    return@launch
                }

                if (productRepository.isSkuExisting(currentFormState.sku, currentFormState.currentProductId)) {
                     _formState.update {
                        it.copy(
                            isLoading = false,
                            skuError = "This SKU already exists. Please use a unique SKU.",
                            generalError = "SKU already exists."
                        )
                    }
                    return@launch
                }

                val product = Product(
                    id = currentFormState.currentProductId ?: 0,
                    name = currentFormState.name.trim(),
                    price = priceDouble,
                    sku = currentFormState.sku.trim(),
                    quantity = quantityInt,
                    category = currentFormState.category.trim(),
                    description = currentFormState.description.trim(),
                    imageUri = currentFormState.imageUri
                )

                if (currentFormState.isEditing) {
                    productRepository.updateProduct(product)
                } else {
                    productRepository.insertProduct(product)
                }
                _eventFlow.emit(AddEditProductEvent.ProductSaved)
            } catch (e: Exception) {
                _formState.update { it.copy(isLoading = false, generalError = "Error saving product: ${e.message}") }
                _eventFlow.emit(AddEditProductEvent.Error("Failed to save product: ${e.message}"))
            } finally {
                 _formState.update { it.copy(isLoading = false) } // Ensure isLoading is reset
            }
        }
    }

    private fun validateForm(state: ProductFormState): Boolean {
        val nameError = if (state.name.isBlank()) "Product name cannot be empty." else null
        val skuError = if (state.sku.isBlank()) "SKU cannot be empty." else null
        val categoryError = if (state.category.isBlank()) "Category cannot be empty." else null

        val priceDouble = state.price.toDoubleOrNull()
        val priceError = if (priceDouble == null || priceDouble < 0) "Enter a valid non-negative price." else null

        val quantityInt = state.quantity.toIntOrNull()
        val quantityError = if (quantityInt == null || quantityInt < 0) "Enter a valid non-negative quantity." else null

        val hasError = listOf(nameError, skuError, categoryError, priceError, quantityError).any { it != null }

        _formState.value = state.copy(
            nameError = nameError,
            skuError = skuError,
            categoryError = categoryError,
            priceError = priceError,
            quantityError = quantityError
        )

        return !hasError
    }
}
