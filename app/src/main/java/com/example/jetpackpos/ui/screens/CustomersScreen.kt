package com.example.jetpackpos.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.jetpackpos.data.model.Customer
import com.example.jetpackpos.ui.navigation.AddEditCustomer
import com.example.jetpackpos.ui.viewmodel.CustomerListUiState
import com.example.jetpackpos.ui.viewmodel.CustomerListViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    navController: NavController,
    viewModel: CustomerListViewModel = hiltViewModel()
) {
    val uiState by viewModel.customersUiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<Customer?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.deleteEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Customers") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(AddEditCustomer.routeWithArgs()) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Customer")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                label = { Text("Search Customers (Name, Phone, Email)") },
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

            Box(modifier = Modifier.weight(1f)) {
                when (val state = uiState) {
                    is CustomerListUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    is CustomerListUiState.Error -> Text(
                        "Error: ${state.message}",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                    is CustomerListUiState.Success -> {
                        if (state.customers.isEmpty()) {
                            Text(
                                text = if (searchQuery.isBlank()) "No customers found. Tap '+' to add." else "No customers match your search.",
                                modifier = Modifier.align(Alignment.Center).padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.customers, key = { it.id }) { customer ->
                                    CustomerListItem(
                                        customer = customer,
                                        onEditClick = {
                                            navController.navigate(AddEditCustomer.routeWithArgs(customer.id))
                                        },
                                        onDeleteClick = { showDeleteDialog = customer }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        showDeleteDialog?.let { customerToDelete ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Delete Customer") },
                text = { Text("Are you sure you want to delete '${customerToDelete.name}'? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteCustomer(customerToDelete)
                            showDeleteDialog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Delete") }
                },
                dismissButton = {
                    Button(onClick = { showDeleteDialog = null }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun CustomerListItem(
    customer: Customer,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                customer.phoneNumber?.takeIf { it.isNotBlank() }?.let {
                    Text("Phone: $it", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                customer.email?.takeIf { it.isNotBlank() }?.let {
                    Text("Email: $it", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                customer.address?.takeIf { it.isNotBlank() }?.let {
                    Text("Address: ${it.take(30)}${if(it.length > 30) "..." else ""}", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                }
            }
            Row {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Filled.Edit, "Edit Customer")
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Filled.Delete, "Delete Customer", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
