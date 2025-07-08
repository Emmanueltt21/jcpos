package com.example.jetpackpos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.jetpackpos.data.model.OrderItem
import com.example.jetpackpos.ui.viewmodel.OrderDetailsUiState
import com.example.jetpackpos.ui.viewmodel.OrderDetailsViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    navController: NavController,
    viewModel: OrderDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when (val state = uiState) {
                is OrderDetailsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is OrderDetailsUiState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is OrderDetailsUiState.NotFound -> {
                     Text(
                        text = "Order not found.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is OrderDetailsUiState.Success -> {
                    val orderWithItems = state.orderWithItems
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text("Order ID: #${orderWithItems.order.id}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Date: ${dateFormat.format(Date(orderWithItems.order.timestamp))}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Items:", style = MaterialTheme.typography.titleMedium)
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(orderWithItems.items, key = { it.id }) { item ->
                                OrderItemView(item = item, currencyFormat = currencyFormat)
                                Divider()
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Amount: ", style = MaterialTheme.typography.titleMedium)
                            Text(
                                currencyFormat.format(orderWithItems.order.totalAmount),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        // Conceptual Print Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                        ) {
                            Button(onClick = { /* TODO: Implement PDF Print */ }, enabled = false) {
                                Text("Print PDF")
                            }
                            Button(onClick = { /* TODO: Implement Thermal Print */ }, enabled = false) {
                                Text("Print Thermal")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderItemView(item: OrderItem, currencyFormat: NumberFormat) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.productNameAtPurchase, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text("SKU: ${item.productSkuAtPurchase}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "${currencyFormat.format(item.priceAtPurchase)} x ${item.quantitySold}",
                fontSize = 14.sp
            )
        }
        Text(
            currencyFormat.format(item.priceAtPurchase * item.quantitySold),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
