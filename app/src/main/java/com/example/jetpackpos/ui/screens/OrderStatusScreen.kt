package com.example.jetpackpos.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.jetpackpos.ui.navigation.Screen
import com.example.jetpackpos.ui.screens.common.OrderItemView // Import common component
import com.example.jetpackpos.ui.viewmodel.OrderStatusUiState
import com.example.jetpackpos.ui.viewmodel.OrderStatusViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderStatusScreen(
    navController: NavController,
    viewModel: OrderStatusViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Order Status") })
            // No back button by default, as it usually pops back to home after checkout
        },
        bottomBar = {
            Button(
                onClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Icon(Icons.Filled.Home, contentDescription = "Home Icon", modifier = Modifier.padding(end = 8.dp))
                Text("Back to Home")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when (val state = uiState) {
                is OrderStatusUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is OrderStatusUiState.Error -> Text("Error: ${state.message}", modifier = Modifier.align(Alignment.Center))
                is OrderStatusUiState.NotFound -> Text("Order Not Found.", modifier = Modifier.align(Alignment.Center))
                is OrderStatusUiState.Success -> {
                    val orderWithItems = state.orderWithItems
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Success",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Order #${orderWithItems.order.id} Placed Successfully!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Total: ${currencyFormat.format(orderWithItems.order.totalAmount)}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            "Paid via: ${orderWithItems.order.paymentMethod}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        orderWithItems.order.customerId?.let {
                             Text("Customer ID: $it", style = MaterialTheme.typography.bodySmall)
                             // In a real app, you might fetch and display customer name
                        }
                        Text(
                            "Date: ${dateFormat.format(Date(orderWithItems.order.timestamp))}",
                             style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Items:", style = MaterialTheme.typography.titleMedium)
                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            items(orderWithItems.items, key = { it.id }) { item ->
                                OrderItemView(item = item, currencyFormat = currencyFormat) // Reusing from OrderDetailsScreen
                                Divider()
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                        ) {
                            Button(onClick = { Toast.makeText(context, "Print PDF - Not Implemented", Toast.LENGTH_SHORT).show() }, enabled = true) {
                                Icon(Icons.Filled.ReceiptLong, contentDescription = "PDF", modifier = Modifier.padding(end = 4.dp))
                                Text("PDF Receipt")
                            }
                            Button(onClick = { Toast.makeText(context, "Print Thermal - Not Implemented", Toast.LENGTH_SHORT).show() }, enabled = true) {
                                Icon(Icons.Filled.Print, contentDescription = "Thermal", modifier = Modifier.padding(end = 4.dp))
                                Text("Thermal Print")
                            }
                        }
                    }
                }
            }
        }
    }
}
