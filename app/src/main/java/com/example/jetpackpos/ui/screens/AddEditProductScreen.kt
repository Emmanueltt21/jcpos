package com.example.jetpackpos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check // Changed from Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.jetpackpos.ui.viewmodel.AddEditProductEvent
import com.example.jetpackpos.ui.viewmodel.AddEditProductViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    navController: NavController,
    // Removed productId from here as ViewModel handles it via SavedStateHandle
    viewModel: AddEditProductViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsState()
    val context = LocalContext.current // For Toasts or other context needs
    val snackbarHostState = remember { SnackbarHostState() }


    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AddEditProductEvent.ProductSaved -> {
                    navController.navigateUp()
                }
                is AddEditProductEvent.Error -> {
                     snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (formState.isEditing) "Edit Product" else "Add New Product") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.saveProduct() },
                modifier = Modifier.navigationBarsPadding()
            ) {
                Icon(Icons.Filled.Check, contentDescription = if (formState.isEditing) "Save Changes" else "Save Product")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        // Show loading indicator when fetching product for editing
        if (formState.isLoading && formState.isEditing && formState.currentProductId != null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Apply padding from Scaffold
                    .padding(horizontal = 16.dp, vertical = 8.dp) // Additional content padding
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp) // Spacing between form fields
            ) {
                formState.generalError?.let { error ->
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                }

                OutlinedTextField(
                    value = formState.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Product Name*") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = formState.nameError != null,
                    singleLine = true,
                    supportingText = { formState.nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                OutlinedTextField(
                    value = formState.sku,
                    onValueChange = viewModel::onSkuChange,
                    label = { Text("SKU (Stock Keeping Unit)*") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = formState.skuError != null,
                    singleLine = true,
                    supportingText = { formState.skuError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = formState.price,
                        onValueChange = viewModel::onPriceChange,
                        label = { Text("Price*") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = formState.priceError != null,
                        singleLine = true,
                        supportingText = { formState.priceError?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
                    )
                    OutlinedTextField(
                        value = formState.quantity,
                        onValueChange = viewModel::onQuantityChange,
                        label = { Text("Quantity*") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = formState.quantityError != null,
                        singleLine = true,
                        supportingText = { formState.quantityError?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
                    )
                }

                OutlinedTextField(
                    value = formState.category,
                    onValueChange = viewModel::onCategoryChange,
                    label = { Text("Category*") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = formState.categoryError != null,
                    singleLine = true,
                    supportingText = { formState.categoryError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )

                OutlinedTextField(
                    value = formState.imageUri ?: "",
                    onValueChange = viewModel::onImageUriChange,
                    label = { Text("Image URL (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Show loading indicator during save operation (for new or edit)
                if (formState.isLoading && (formState.currentProductId == null || !formState.isEditing) ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                Spacer(modifier = Modifier.height(72.dp)) // Space for FAB to not overlap content
            }
        }
    }
}
