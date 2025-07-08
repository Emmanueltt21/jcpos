package com.example.jetpackpos.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.jetpackpos.data.model.Customer
import com.example.jetpackpos.ui.navigation.OrderStatusScreen // For navigation after checkout
import com.example.jetpackpos.ui.screens.common.CartSummaryView
import com.example.jetpackpos.ui.viewmodel.CartEvent
import com.example.jetpackpos.ui.viewmodel.CartViewModel
import com.example.jetpackpos.ui.viewmodel.predefinedPaymentMethods
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavController,
    cartViewModel: CartViewModel = hiltViewModel()
) {
    val cartState by cartViewModel.cartUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }

    var customerDropdownExpanded by remember { mutableStateOf(false) }
    var paymentMethodDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        cartViewModel.cartEvents.collectLatest { event ->
            val message = when (event) {
                is CartEvent.QuantityUpdated -> "${event.productName} quantity updated."
                is CartEvent.ItemRemoved -> "${event.productName} removed from cart."
                is CartEvent.StockUnavailable -> "Cannot set ${event.requested} of ${event.productName}. Only ${event.available} in stock."
                is CartEvent.OrderPlaced -> {
                    // Navigate to OrderStatusScreen
                    navController.navigate(OrderStatusScreen.routeWithArg(event.orderId)) {
                        popUpTo(Screen.Home.route) // Pop back to home, clearing backstack up to home
                    }
                    "Order #${event.orderId} placed successfully for ${currencyFormat.format(event.totalAmount)}."
                }
                is CartEvent.Error -> "Error: ${event.message}"
                CartEvent.CartCleared -> "Cart cleared." // May not be shown if navigating away
                is CartEvent.ItemAdded -> "" // Should not happen here
            }
            if(message.isNotBlank()) {
                snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Review & Checkout") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()) // Make the whole screen scrollable
        ) {
            // Customer Selection Section
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Customer Information", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = cartState.isGuestCheckout,
                        onCheckedChange = { cartViewModel.onToggleGuestCheckout(it) }
                    )
                    Text("Continue as Guest")
                }

                if (!cartState.isGuestCheckout) {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (cartState.isLoadingCustomers) {
                        CircularProgressIndicator()
                    } else if (cartState.customers.isEmpty()) {
                        Text("No customers found. Add customers in the 'Customers' section.")
                    } else {
                        ExposedDropdownMenuBox(
                            expanded = customerDropdownExpanded,
                            onExpandedChange = { customerDropdownExpanded = !customerDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = cartState.customers.find { it.id == cartState.selectedCustomerId }?.name ?: "Select Customer",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Select Customer") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = customerDropdownExpanded,
                                onDismissRequest = { customerDropdownExpanded = false }
                            ) {
                                cartState.customers.forEach { customer ->
                                    DropdownMenuItem(
                                        text = { Text("${customer.name} (${customer.phoneNumber ?: "N/A"})") },
                                        onClick = {
                                            cartViewModel.onCustomerSelected(customer.id)
                                            customerDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Divider()

            // Payment Method Selection
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Payment Method", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = paymentMethodDropdownExpanded,
                    onExpandedChange = { paymentMethodDropdownExpanded = !paymentMethodDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = cartState.selectedPaymentMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Payment Method") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentMethodDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = paymentMethodDropdownExpanded,
                        onDismissRequest = { paymentMethodDropdownExpanded = false }
                    ) {
                        predefinedPaymentMethods.forEach { paymentMethod ->
                            DropdownMenuItem(
                                text = { Text(paymentMethod) },
                                onClick = {
                                    cartViewModel.onPaymentMethodSelected(paymentMethod)
                                    paymentMethodDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            Divider()

            // Cart Summary (re-uses the common component)
            Box(modifier = Modifier.padding(16.dp).weight(1f, fill = false)) { // fill = false so it doesn't expand unnecessarily
                 CartSummaryView(
                    cartItems = cartState.items,
                    subtotal = cartState.subtotal,
                    taxAmount = cartState.taxAmount,
                    total = cartState.total,
                    onQuantityChange = { productId, newQuantity ->
                        cartViewModel.updateQuantityInCart(productId, newQuantity)
                        keyboardController?.hide()
                    },
                    onRemoveItem = { cartViewModel.removeProductFromCart(it) },
                    onClearCart = { cartViewModel.clearCart() },
                    onCheckout = { cartViewModel.checkout() },
                    isCheckoutButtonVisible = true // Checkout button is visible here
                )
            }
            if(cartState.isLoadingCheckout){
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp))
            }
        }
    }
}
