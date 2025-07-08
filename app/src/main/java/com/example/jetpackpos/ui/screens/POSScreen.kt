package com.example.jetpackpos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
// import com.example.jetpackpos.data.model.CartItem // No longer needed here directly
import com.example.jetpackpos.ui.screens.common.CartSummaryView // Import common component
import com.example.jetpackpos.data.model.Product
import com.example.jetpackpos.ui.viewmodel.CartEvent
import com.example.jetpackpos.ui.viewmodel.CartViewModel
import com.example.jetpackpos.ui.viewmodel.ProductListUiState
import com.example.jetpackpos.ui.viewmodel.ProductListViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POSScreen( // Renamed from SalesScreen
    navController: NavController,
    productListViewModel: ProductListViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel()
) {
    val productListState by productListViewModel.productsUiState.collectAsState()
    val cartState by cartViewModel.cartUiState.collectAsState()
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge // For older Material, might be BadgeBox in M3
import androidx.compose.material3.BadgedBox // Correct for M3

    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) } // Define at screen level

    LaunchedEffect(key1 = Unit) {
        cartViewModel.cartEvents.collectLatest { event ->
            val message = when (event) {
                is CartEvent.ItemAdded -> "${event.productName} added to cart."
                is CartEvent.QuantityUpdated -> "${event.productName} quantity updated."
                is CartEvent.ItemRemoved -> "${event.productName} removed from cart."
                is CartEvent.StockUnavailable -> "Cannot add ${event.requested} of ${event.productName}. Only ${event.available} in stock."
                is CartEvent.OrderPlaced -> "Order #${event.orderId} placed successfully for ${currencyFormat.format(event.totalAmount)}."
                is CartEvent.Error -> "Error: ${event.message}"
                CartEvent.CartCleared -> "Cart cleared."
            }
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
        }
    }

    val cartItemCount = cartState.items.sumOf { it.quantityInCart }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Point of Sale") },
                actions = {
                    BadgedBox(
                        badge = {
                            if (cartItemCount > 0) {
                                Badge { Text("$cartItemCount") }
                            }
                        }
                    ) {
                        IconButton(onClick = { navController.navigate(com.example.jetpackpos.ui.navigation.CartDetails.route) }) {
                            Icon(
                                imageVector = Icons.Filled.ShoppingCart,
                                contentDescription = "Open Cart"
                            )
                        }
                    }
                }
            )
        }
import androidx.compose.material.icons.filled.QrCodeScanner // For Scan button

    ) { paddingValues ->
        Column( // Changed from Row to Column to stack elements vertically
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                // .padding(8.dp) // Main padding will be on children
        ) {
            // Row for Scan button and Total Amount
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val scope = rememberCoroutineScope() // For launching snackbar
                Button(
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Scan Product - Not Implemented Yet", duration = SnackbarDuration.Short)
                        }
                    },
                    modifier = Modifier.weight(0.3f)
                ) {
                    Icon(Icons.Filled.QrCodeScanner, contentDescription = "Scan Product")
                    Spacer(Modifier.width(4.dp))
                    Text("Scan")
                }
                Text(
                    text = "Cart Total: ${currencyFormat.format(cartState.total)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(0.7f),
                    textAlign = TextAlign.End
                )
            }

            // Search Bar
            OutlinedTextField(
                value = productListViewModel.searchQuery.collectAsState().value, // Assuming ProductListViewModel is accessible and has searchQuery
                onValueChange = { productListViewModel.onSearchQueryChange(it) },
                label = { Text("Search Products") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Products") },
                trailingIcon = {
                    if (productListViewModel.searchQuery.collectAsState().value.isNotEmpty()) {
                        IconButton(onClick = { productListViewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear Search")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
            )

            // Main content Row for Product List and Cart Summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Takes remaining space
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                 // Product List Area
                Column(modifier = Modifier.weight(0.6f)) {
                    // Text("Available Products", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp)) // Title moved or implicit
                    when (val state = productListState) {
                    is ProductListUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    is ProductListUiState.Error -> Text("Error loading products: ${state.message}", color = MaterialTheme.colorScheme.error)
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items // Correct import for LazyVerticalGrid items
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Image // For placeholder

                    is ProductListUiState.Success -> {
                        if (state.products.isEmpty()) {
                            Text(
                                if (productListViewModel.searchQuery.collectAsState().value.isNotBlank()) "No products match search." else "No products in inventory.",
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
                            )
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 150.dp), // Or GridCells.Fixed(2)
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(top = 8.dp) // Add padding above the grid
                            ) {
                                items(state.products, key = { it.id }) { product ->
                                    val cartItem = cartState.items.find { item -> item.productId == product.id }
                                    ProductGridItem(
                                        product = product,
                                        quantityInCart = cartItem?.quantityInCart ?: 0,
                                        onAddToCart = { cartViewModel.addProductToCart(product) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.width(8.dp))
            Divider(modifier = Modifier.fillMaxHeight().width(1.dp))
            Spacer(Modifier.width(8.dp))

            // Cart Area
            Column(modifier = Modifier.weight(0.4f)) {
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
                    onCheckout = {
                        cartViewModel.checkout() // Call the actual checkout method
                    },
                    isCheckoutButtonVisible = false // Hide checkout button in POS sidebar summary
                )
            }
        }
    }
}

@Composable
fun ProductGridItem(
    product: Product,
    quantityInCart: Int,
    onAddToCart: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAddToCart, enabled = product.quantity > 0)
            .aspectRatio(0.8f), // Adjust aspect ratio for grid item appearance
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (product.quantity > 0) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Placeholder for Image
                Box(
                    modifier = Modifier
                        .weight(1f) // Image takes most space
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Image, // From InventoryScreen placeholder
                        contentDescription = product.name,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(product.name, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center, maxLines = 2)
                Text(
                    currencyFormat.format(product.price),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Stock: ${product.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (product.quantity < 1) MaterialTheme.colorScheme.error.copy(alpha=0.7f)
                            else if (product.quantity < 5) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (quantityInCart > 0) {
                Badge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "$quantityInCart",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 10.sp
                    )
                }
            }
             if (product.quantity == 0) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Out of Stock", color = MaterialTheme.colorScheme.surface, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// CartSummaryView, CartListItemView, and CartTotalRow are now in ui.screens.common.CartComponents.kt
