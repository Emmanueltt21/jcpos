package com.example.jetpackpos.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackpos.data.model.Product
import com.example.jetpackpos.data.repository.ProductRepository
import com.example.jetpackpos.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull

import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductFormState(
    val name: String = "",
    val price: String = "", // Use String for input, convert to Double for saving
    val sku: String = "",
    val quantity: String = "", // Use String for input, convert to Int for saving
    val category: String = "",
    val description: String = "", // New field
    val imageUri: String? = null,

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
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _formState = MutableStateFlow(ProductFormState())
    val formState = _formState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<AddEditProductEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val currentProductId: Long? = savedStateHandle.get<String>(Screen.AddEditProduct.ARG_PRODUCT_ID)?.toLongOrNull()

    init {
        if (currentProductId != null) {
            _formState.value = _formState.value.copy(isLoading = true, isEditing = true, currentProductId = currentProductId)
            loadProduct(currentProductId)
        }
    }

    fun onNameChange(name: String) {
        _formState.value = _formState.value.copy(name = name, nameError = null, generalError = null)
    }

    fun onPriceChange(price: String) {
        _formState.value = _formState.value.copy(price = price, priceError = null, generalError = null)
    }

    fun onSkuChange(sku: String) {
        _formState.value = _formState.value.copy(sku = sku, skuError = null, generalError = null)
    }

    fun onQuantityChange(quantity: String) {
        _formState.value = _formState.value.copy(quantity = quantity, quantityError = null, generalError = null)
    }

    fun onCategoryChange(category: String) {
        _formState.value = _formState.value.copy(category = category, categoryError = null, generalError = null)
    }

    fun onDescriptionChange(description: String) {
        _formState.value = _formState.value.copy(description = description, generalError = null)
    }

    fun onImageUriChange(uri: String?) {
        _formState.value = _formState.value.copy(imageUri = uri)
    }


    private fun loadProduct(productId: Long) {
        viewModelScope.launch {
            val product = productRepository.getProductById(productId).firstOrNull()
            if (product != null) {
                _formState.value = _formState.value.copy(
                    name = product.name,
                    price = product.price.toString(),
                    sku = product.sku,
                    quantity = product.quantity.toString(),
                    category = product.category,
                    description = product.description ?: "",
                    imageUri = product.imageUri,
                    isLoading = false
                )
            } else {
                _formState.value = _formState.value.copy(isLoading = false, generalError = "Failed to load product.")
                _eventFlow.emit(AddEditProductEvent.Error("Product not found."))
            }
        }
    }

    fun saveProduct() {
        val currentFormState = _formState.value
        if (!validateForm(currentFormState)) return

        _formState.value = currentFormState.copy(isLoading = true, generalError = null)

        viewModelScope.launch {
            try {
                val priceDouble = currentFormState.price.toDoubleOrNull()
                val quantityInt = currentFormState.quantity.toIntOrNull()

                if (priceDouble == null || quantityInt == null) {
                    _formState.value = _formState.value.copy(isLoading = false, generalError = "Invalid price or quantity format.")
                    return@launch
                }

                // SKU Uniqueness Check
                if (productRepository.isSkuExisting(currentFormState.sku, currentFormState.currentProductId)) {
                     _formState.value = _formState.value.copy(
                        isLoading = false,
                        skuError = "This SKU already exists. Please use a unique SKU.",
                        generalError = "SKU already exists."
                    )
                    return@launch
                }


                val product = Product(
                    id = currentFormState.currentProductId ?: 0, // If new, ID is 0 for auto-generate
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
                _formState.value = _formState.value.copy(isLoading = false, generalError = "Error saving product: ${e.message}")
                _eventFlow.emit(AddEditProductEvent.Error("Failed to save product: ${e.message}"))
            }
        }
    }

    private fun validateForm(state: ProductFormState): Boolean {
        var isValid = true
        var newNameError: String? = null
        var newPriceError: String? = null
        var newSkuError: String? = null
        var newQuantityError: String? = null
        var newCategoryError: String? = null

        if (state.name.isBlank()) {
            newNameError = "Product name cannot be empty."
            isValid = false
        }
        if (state.sku.isBlank()) {
            newSkuError = "SKU cannot be empty."
            isValid = false
        }
        if (state.category.isBlank()) {
            newCategoryError = "Category cannot be empty."
            isValid = false
        }

        val priceDouble = state.price.toDoubleOrNull()
        if (state.price.isBlank() || priceDouble == null || priceDouble < 0) {
            newPriceError = "Enter a valid non-negative price."
            isValid = false
        }

        val quantityInt = state.quantity.toIntOrNull()
        if (state.quantity.isBlank() || quantityInt == null || quantityInt < 0) {
            newQuantityError = "Enter a valid non-negative quantity."
            isValid = false
        }

        _formState.value = state.copy(
            nameError = newNameError,
            priceError = newPriceError,
            skuError = newSkuError,
            quantityError = newQuantityError,
            categoryError = newCategoryError
        )
        return isValid
    }
}
