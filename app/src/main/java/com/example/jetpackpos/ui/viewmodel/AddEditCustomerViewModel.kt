package com.example.jetpackpos.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackpos.data.model.Customer
import com.example.jetpackpos.data.repository.CustomerRepository
import com.example.jetpackpos.ui.navigation.AddEditCustomer // For ARG_CUSTOMER_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomerFormState(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",

    val nameError: String? = null,
    val phoneError: String? = null,
    val emailError: String? = null,
    // Address is optional, so likely no error state unless specific validation is added
    val generalError: String? = null,

    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val currentCustomerId: Long? = null
)

sealed class AddEditCustomerEvent {
    object CustomerSaved : AddEditCustomerEvent()
    data class Error(val message: String) : AddEditCustomerEvent()
}

@HiltViewModel
class AddEditCustomerViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _formState = MutableStateFlow(CustomerFormState())
    val formState = _formState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<AddEditCustomerEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val currentCustomerId: Long? = savedStateHandle.get<String>(AddEditCustomer.ARG_CUSTOMER_ID)?.toLongOrNull()

    init {
        if (currentCustomerId != null) {
            _formState.value = _formState.value.copy(isLoading = true, isEditing = true, currentCustomerId = currentCustomerId)
            loadCustomer(currentCustomerId)
        }
    }

    fun onNameChange(name: String) {
        _formState.value = _formState.value.copy(name = name, nameError = null, generalError = null)
    }

    fun onPhoneChange(phone: String) {
        _formState.value = _formState.value.copy(phone = phone, phoneError = null, generalError = null)
    }

    fun onEmailChange(email: String) {
        _formState.value = _formState.value.copy(email = email, emailError = null, generalError = null)
    }

    fun onAddressChange(address: String) {
        _formState.value = _formState.value.copy(address = address, generalError = null)
    }

    private fun loadCustomer(customerId: Long) {
        viewModelScope.launch {
            val customer = customerRepository.getCustomerById(customerId).firstOrNull()
            if (customer != null) {
                _formState.value = _formState.value.copy(
                    name = customer.name,
                    phone = customer.phoneNumber ?: "",
                    email = customer.email ?: "",
                    address = customer.address ?: "",
                    isLoading = false
                )
            } else {
                _formState.value = _formState.value.copy(isLoading = false, generalError = "Failed to load customer.")
                _eventFlow.emit(AddEditCustomerEvent.Error("Customer not found."))
            }
        }
    }

    fun saveCustomer() {
        val currentFormState = _formState.value
        if (!validateForm(currentFormState)) return

        _formState.value = currentFormState.copy(isLoading = true, generalError = null)

        viewModelScope.launch {
            try {
                // Uniqueness checks
                if (currentFormState.phone.isNotBlank() && customerRepository.isPhoneNumberExisting(currentFormState.phone, currentFormState.currentCustomerId)) {
                    _formState.value = _formState.value.copy(isLoading = false, phoneError = "Phone number already exists.", generalError = "Phone number already exists.")
                    return@launch
                }
                if (currentFormState.email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(currentFormState.email).matches()){
                     _formState.value = _formState.value.copy(isLoading = false, emailError = "Invalid email format.", generalError = "Invalid email format.")
                    return@launch
                }
                if (currentFormState.email.isNotBlank() && customerRepository.isEmailExisting(currentFormState.email, currentFormState.currentCustomerId)) {
                    _formState.value = _formState.value.copy(isLoading = false, emailError = "Email already exists.", generalError = "Email already exists.")
                    return@launch
                }

                val customer = Customer(
                    id = currentFormState.currentCustomerId ?: 0,
                    name = currentFormState.name.trim(),
                    phoneNumber = currentFormState.phone.trim().takeIf { it.isNotBlank() },
                    email = currentFormState.email.trim().takeIf { it.isNotBlank() },
                    address = currentFormState.address.trim().takeIf { it.isNotBlank() }
                )

                if (currentFormState.isEditing) {
                    customerRepository.updateCustomer(customer)
                } else {
                    customerRepository.insertCustomer(customer)
                }
                _eventFlow.emit(AddEditCustomerEvent.CustomerSaved)

            } catch (e: Exception) {
                _formState.value = _formState.value.copy(isLoading = false, generalError = "Error saving customer: ${e.message}")
                _eventFlow.emit(AddEditCustomerEvent.Error("Failed to save customer: ${e.message}"))
            }
        }
    }

    private fun validateForm(state: CustomerFormState): Boolean {
        var isValid = true
        var nameError: String? = null
        var phoneError: String? = null
        var emailError: String? = null

        if (state.name.isBlank()) {
            nameError = "Customer name cannot be empty."
            isValid = false
        }

        // Basic phone validation (e.g. not blank if provided, maybe length or digits only)
        // For simplicity, just checking if it's not blank for now if provided.
        // More complex validation (e.g. E.164) is out of scope for this step.

        // Basic email validation
        if (state.email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            emailError = "Invalid email address format."
            isValid = false
        }

        _formState.value = state.copy(
            nameError = nameError,
            phoneError = phoneError,
            emailError = emailError
        )
        return isValid
    }
}
