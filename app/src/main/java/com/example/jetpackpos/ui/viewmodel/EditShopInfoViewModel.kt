package com.example.jetpackpos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackpos.data.model.ShopInfo
import com.example.jetpackpos.data.repository.ShopInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShopInfoFormState(
    val shopName: String = "",
    val contactNumber: String = "",
    val email: String = "",
    val address: String = "",
    val currencySymbol: String = "$",
    val taxPercentage: String = "0.0", // String for TextField

    val shopNameError: String? = null,
    val currencySymbolError: String? = null,
    val taxPercentageError: String? = null,
    val emailError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = true // Start with loading true to fetch initial data
)

sealed class EditShopInfoEvent {
    object ShopInfoSaved : EditShopInfoEvent()
    data class Error(val message: String) : EditShopInfoEvent()
}

@HiltViewModel
class EditShopInfoViewModel @Inject constructor(
    private val shopInfoRepository: ShopInfoRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(ShopInfoFormState())
    val formState: StateFlow<ShopInfoFormState> = _formState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<EditShopInfoEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadShopInfo()
    }

    private fun loadShopInfo() {
        viewModelScope.launch {
            shopInfoRepository.getShopInfo().firstOrNull()?.let { info ->
                _formState.value = ShopInfoFormState(
                    shopName = info.shopName,
                    contactNumber = info.contactNumber ?: "",
                    email = info.email ?: "",
                    address = info.address ?: "",
                    currencySymbol = info.currencySymbol,
                    taxPercentage = (info.taxPercentage * 100).toString(), // Display as percentage
                    isLoading = false
                )
            } ?: run {
                // Should ideally not happen if repository provides a default
                _formState.value = _formState.value.copy(isLoading = false, generalError = "Could not load shop info.")
            }
        }
    }

    fun onShopNameChange(name: String) {
        _formState.update { it.copy(shopName = name, shopNameError = null, generalError = null) }
    }
    fun onContactNumberChange(contact: String) {
        _formState.update { it.copy(contactNumber = contact, generalError = null) }
    }
    fun onEmailChange(email: String) {
        _formState.update { it.copy(email = email, emailError = null, generalError = null) }
    }
    fun onAddressChange(address: String) {
        _formState.update { it.copy(address = address, generalError = null) }
    }
    fun onCurrencySymbolChange(symbol: String) {
        _formState.update { it.copy(currencySymbol = symbol, currencySymbolError = null, generalError = null) }
    }
    fun onTaxPercentageChange(tax: String) {
        _formState.update { it.copy(taxPercentage = tax, taxPercentageError = null, generalError = null) }
    }

    fun saveShopInfo() {
        val currentFormState = _formState.value
        if (!validateForm(currentFormState)) return

        _formState.update { it.copy(isLoading = true, generalError = null) }
        viewModelScope.launch {
            try {
                val taxDouble = currentFormState.taxPercentage.toDoubleOrNull()
                if (taxDouble == null) {
                     _formState.update { it.copy(isLoading = false, taxPercentageError = "Invalid tax percentage", generalError = "Invalid tax percentage.")}
                    return@launch
                }

                val shopInfo = ShopInfo(
                    shopName = currentFormState.shopName.trim(),
                    contactNumber = currentFormState.contactNumber.trim().takeIf { it.isNotBlank() },
                    email = currentFormState.email.trim().takeIf { it.isNotBlank() },
                    address = currentFormState.address.trim().takeIf { it.isNotBlank() },
                    currencySymbol = currentFormState.currencySymbol.trim(),
                    taxPercentage = taxDouble / 100.0 // Convert from percentage string to decimal
                )
                shopInfoRepository.insertOrUpdateShopInfo(shopInfo)
                _eventFlow.emit(EditShopInfoEvent.ShopInfoSaved)
                _formState.update { it.copy(isLoading = false) } // Update loading state after saving
            } catch (e: Exception) {
                _formState.update { it.copy(isLoading = false, generalError = "Error saving shop info: ${e.message}") }
                _eventFlow.emit(EditShopInfoEvent.Error("Failed to save shop info: ${e.message}"))
            }
        }
    }

    private fun validateForm(state: ShopInfoFormState): Boolean {
        var isValid = true
        if (state.shopName.isBlank()) {
            _formState.update { it.copy(shopNameError = "Shop name cannot be empty.") }
            isValid = false
        }
        if (state.currencySymbol.isBlank()) {
            _formState.update { it.copy(currencySymbolError = "Currency symbol cannot be empty.") }
            isValid = false
        }
        val tax = state.taxPercentage.toDoubleOrNull()
        if (tax == null || tax < 0.0 || tax > 100.0) { // Validate as percentage 0-100
            _formState.update { it.copy(taxPercentageError = "Enter a valid tax rate (0-100).") }
            isValid = false
        }
        if (state.email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _formState.update { it.copy(emailError = "Invalid email format.") }
            isValid = false
        }

        if(!isValid) _formState.update { it.copy(generalError = "Please correct the highlighted fields.") }
        return isValid
    }
}
