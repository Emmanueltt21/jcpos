package com.example.jetpackpos.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.jetpackpos.data.model.Product
import com.example.jetpackpos.ui.navigation.Screen
import com.example.jetpackpos.ui.viewmodel.ProductListUiState
import com.example.jetpackpos.ui.viewmodel.ProductListViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    navController: NavController,
    viewModel: ProductListViewModel = hiltViewModel()
) {
    val uiState by viewModel.productsUiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<Product?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Inventory Management") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.AddEditProduct.routeWithArgs()) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Product")
            }
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction

        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                label = { Text("Search Products (Name/SKU)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Icon") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear Search")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
            )

            Box(
                modifier = Modifier.weight(1f) // Ensure LazyColumn takes remaining space
            ) {
                when (val state = uiState) {
                    is ProductListUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is ProductListUiState.Error -> {
                        Text(
                            text = "Error: ${state.message}",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }
                    is ProductListUiState.Success -> {
                        if (state.products.isEmpty()) {
                            Text(
                                text = if (searchQuery.isBlank()) "No products found. Tap the '+' button to add." else "No products match your search.",
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(16.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.products, key = { product -> product.id }) { product ->
                                    ProductListItem(
                                        product = product,
                                        onEditClick = {
                                            navController.navigate(Screen.AddEditProduct.routeWithArgs(product.id))
                                        },
                                        onDeleteClick = {
                                            showDeleteDialog = product
                                        },
                                        onClick = {
                                            // navController.navigate(Screen.ProductDetails.routeWithArg(product.id))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        showDeleteDialog?.let { productToDelete ->
            AlertDialog(
                    onDismissRequest = { showDeleteDialog = null },
                    title = { Text("Delete Product") },
                    text = { Text("Are you sure you want to delete '${productToDelete.name}'? This action cannot be undone.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteProduct(productToDelete)
                                showDeleteDialog = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDeleteDialog = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ProductListItem(
    product: Product,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onClick: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault()) // Adjust locale as needed

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("SKU: ${product.sku}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Category: ${product.category}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    currencyFormat.format(product.price),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("Qty: ${product.quantity}", fontSize = 14.sp)
                if (product.quantity < 5) { // Low stock indicator
                    Text(
                        "Low Stock",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit Product")
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete Product", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
