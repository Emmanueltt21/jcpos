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
import com.example.jetpackpos.data.model.CartItem
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
fun SalesScreen(
    navController: NavController,
    productListViewModel: ProductListViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel()
) {
    val productListState by productListViewModel.productsUiState.collectAsState()
    val cartState by cartViewModel.cartUiState.collectAsState()
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(title = { Text("Point of Sale") }) }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(8.dp)
        ) {
            // Product List Area
            Column(modifier = Modifier.weight(0.6f)) {
                Text("Available Products", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                when (val state = productListState) {
                    is ProductListUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    is ProductListUiState.Error -> Text("Error loading products: ${state.message}", color = MaterialTheme.colorScheme.error)
                    is ProductListUiState.Success -> {
                        if (state.products.isEmpty()) {
                            Text("No products available in inventory.")
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(state.products, key = { it.id }) { product ->
                                    ProductForSaleItem(
                                        product = product,
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
                    }
                )
            }
        }
    }
}

@Composable
fun ProductForSaleItem(product: Product, onAddToCart: () -> Unit) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold)
                Text("SKU: ${product.sku}", fontSize = 12.sp)
                Text(currencyFormat.format(product.price), color = MaterialTheme.colorScheme.primary)
                Text("Stock: ${product.quantity}", fontSize = 12.sp, color = if (product.quantity < 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onAddToCart, enabled = product.quantity > 0) {
                Icon(Icons.Filled.AddShoppingCart, contentDescription = "Add to Cart")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartSummaryView(
    cartItems: List<CartItem>,
    subtotal: Double,
    taxAmount: Double,
    total: Double,
    onQuantityChange: (productId: Long, newQuantity: Int) -> Unit,
    onRemoveItem: (productId: Long) -> Unit,
    onClearCart: () -> Unit,
    onCheckout: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = Modifier.fillMaxHeight()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
             Text("Current Cart", style = MaterialTheme.typography.titleMedium)
             IconButton(onClick = onClearCart, enabled = cartItems.isNotEmpty()) {
                 Icon(Icons.Filled.RemoveShoppingCart, "Clear Cart")
             }
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        if (cartItems.isEmpty()) {
            Text("Cart is empty. Add products from the list.", modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally), textAlign = TextAlign.Center)
        } else {
            LazyColumn(modifier = Modifier.weight(1f).padding(bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(cartItems, key = { it.id }) { item ->
                    CartListItemView(
                        item = item,
                        onQuantityChange = { newQuantity -> onQuantityChange(item.productId, newQuantity) },
                        onRemoveItem = { onRemoveItem(item.productId) },
                        currencyFormat = currencyFormat
                    )
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        CartTotalRow("Subtotal:", currencyFormat.format(subtotal))
        CartTotalRow("Tax:", currencyFormat.format(taxAmount)) // Assuming tax is calculated
        CartTotalRow("Total:", currencyFormat.format(total), isTotal = true)

        Button(
            onClick = onCheckout,
            enabled = cartItems.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("Proceed to Checkout")
        }
    }
}

@Composable
fun CartListItemView(
    item: CartItem,
    onQuantityChange: (Int) -> Unit,
    onRemoveItem: () -> Unit,
    currencyFormat: NumberFormat
) {
    var quantityInput by remember(item.quantityInCart) { mutableStateOf(item.quantityInCart.toString()) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Card(border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(item.productName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text(currencyFormat.format(item.price) + " x ${item.quantityInCart} = " + currencyFormat.format(item.getTotalPrice()), fontSize = 13.sp)
                }
                 IconButton(onClick = onRemoveItem, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.DeleteForever, "Remove Item", tint = MaterialTheme.colorScheme.error)
                }
            }
            OutlinedTextField(
                value = quantityInput,
                onValueChange = { value ->
                    quantityInput = value.filter { it.isDigit() }.take(3) // Allow only digits, max 3
                },
                label = { Text("Qty", fontSize = 12.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    onQuantityChange(quantityInput.toIntOrNull() ?: 0) // If input is invalid/empty, treat as 0 (triggers remove/validation in VM)
                    keyboardController?.hide()
                }),
                modifier = Modifier.fillMaxWidth().height(60.dp),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
            )
        }
    }
}


@Composable
fun CartTotalRow(label: String, amount: String, isTotal: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal, fontSize = if(isTotal) 18.sp else 16.sp)
        Text(amount, fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal, fontSize = if(isTotal) 18.sp else 16.sp, color = if(isTotal) MaterialTheme.colorScheme.primary else LocalContentColor.current)
    }
}
