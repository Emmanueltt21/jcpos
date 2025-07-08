package com.example.jetpackpos.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.jetpackpos.data.model.OrderWithItems
import com.example.jetpackpos.ui.viewmodel.ReportFilterType
import com.example.jetpackpos.ui.viewmodel.ReportsViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReportsScreen(
    navController: NavController,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports") },
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
                .padding(16.dp)
        ) {
            // Filter Buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ReportFilterType.entries.forEach { filterType -> // Changed from values() to entries
                    Button(
                        onClick = { viewModel.onFilterChange(filterType) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.selectedFilter == filterType) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (uiState.selectedFilter == filterType) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(filterType.name.replace("_", " ").lowercase().replaceFirstChar { it.titlecase() })
                    }
                }
            }

            // Chart Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading && uiState.chartData == null) { // Show loader only if chart data isn't there yet
                    CircularProgressIndicator()
                } else if (uiState.error != null && uiState.chartData == null) {
                     Text("Error loading chart: ${uiState.error}")
                }
                else {
                    Text("Chart Placeholder for ${uiState.selectedFilter.name}\nData: ${uiState.chartData ?: "N/A"}", textAlign = TextAlign.Center)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Transactions (${uiState.selectedFilter.name})", style = MaterialTheme.typography.titleMedium)
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp))
            } else if (uiState.error != null) {
                Text("Error loading transactions: ${uiState.error}", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top=16.dp))
            }
            else if (uiState.transactions.isEmpty()) {
                Text("No transactions found for this period.", modifier = Modifier.padding(top=16.dp).align(Alignment.CenterHorizontally))
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(uiState.transactions, key = { it.order.id }) { orderWithItems ->
                        ReportTransactionItem(
                            orderWithItems = orderWithItems,
                            currencyFormat = currencyFormat,
                            dateFormat = dateFormat
                        )
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun ReportTransactionItem(
    orderWithItems: OrderWithItems,
    currencyFormat: NumberFormat,
    dateFormat: SimpleDateFormat
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Order #${orderWithItems.order.id}", fontWeight = FontWeight.SemiBold)
            Text(dateFormat.format(Date(orderWithItems.order.timestamp)), style = MaterialTheme.typography.bodySmall)
            Text("${orderWithItems.items.sumOf { it.quantitySold }} items", style = MaterialTheme.typography.bodySmall)
        }
        Text(currencyFormat.format(orderWithItems.order.totalAmount), fontWeight = FontWeight.Medium)
    }
}
