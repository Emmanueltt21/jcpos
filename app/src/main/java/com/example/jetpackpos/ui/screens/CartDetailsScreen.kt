package com.example.jetpackpos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.jetpackpos.ui.screens.common.CartSummaryView // Ensure this import is correct
import com.example.jetpackpos.ui.viewmodel.CartEvent
import com.example.jetpackpos.ui.viewmodel.CartViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartDetailsScreen(
    navController: NavController,
    cartViewModel: CartViewModel = hiltViewModel() // Shared ViewModel or passed instance
) {
    val cartState by cartViewModel.cartUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }

    LaunchedEffect(key1 = Unit) {
        cartViewModel.cartEvents.collectLatest { event ->
            val message = when (event) {
                is CartEvent.ItemAdded -> "${event.productName} added to cart." // Should not happen here
                is CartEvent.QuantityUpdated -> "${event.productName} quantity updated."
                is CartEvent.ItemRemoved -> "${event.productName} removed from cart."
                is CartEvent.StockUnavailable -> "Cannot set ${event.requested} of ${event.productName}. Only ${event.available} in stock."
                is CartEvent.OrderPlaced -> {
                    navController.popBackStack() // Go back from cart after order placed
                    "Order #${event.orderId} placed successfully for ${currencyFormat.format(event.totalAmount)}."
                }
                is CartEvent.Error -> "Error: ${event.message}"
                CartEvent.CartCleared -> "Cart cleared."
            }
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Review Cart & Checkout") },
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
                .padding(16.dp) // Outer padding for the screen
        ) {
            // Re-use or adapt CartSummaryView logic here
            // For simplicity, directly embedding similar logic:
            CartSummaryView( // Assuming CartSummaryView is made public or recreated here
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
                onCheckout = { cartViewModel.checkout() }
            )
        }
    }
}
