package com.example.jetpackpos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.jetpackpos.ui.viewmodel.EditShopInfoEvent
import com.example.jetpackpos.ui.viewmodel.EditShopInfoViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditShopInfoScreen(
    navController: NavController,
    viewModel: EditShopInfoViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is EditShopInfoEvent.ShopInfoSaved -> {
                    snackbarHostState.showSnackbar("Shop information saved!", duration = SnackbarDuration.Short)
                    navController.navigateUp()
                }
                is EditShopInfoEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message, duration = SnackbarDuration.Short)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Edit Shop Information") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::saveShopInfo) {
                Icon(Icons.Filled.Check, "Save Shop Info")
            }
        }
    ) { paddingValues ->
        if (formState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                formState.generalError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                OutlinedTextField(
                    value = formState.shopName,
                    onValueChange = viewModel::onShopNameChange,
                    label = { Text("Shop Name*") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = formState.shopNameError != null,
                    singleLine = true,
                    supportingText = { formState.shopNameError?.let{ Text(it, color = MaterialTheme.colorScheme.error) } },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )

                OutlinedTextField(
                    value = formState.contactNumber,
                    onValueChange = viewModel::onContactNumberChange,
                    label = { Text("Contact Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                OutlinedTextField(
                    value = formState.email,
                    onValueChange = viewModel::onEmailChange,
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = formState.emailError != null,
                    singleLine = true,
                    supportingText = { formState.emailError?.let{ Text(it, color = MaterialTheme.colorScheme.error) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                OutlinedTextField(
                    value = formState.address,
                    onValueChange = viewModel::onAddressChange,
                    label = { Text("Shop Address") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    maxLines = 3
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = formState.currencySymbol,
                        onValueChange = viewModel::onCurrencySymbolChange,
                        label = { Text("Currency*") },
                        modifier = Modifier.weight(1f),
                        isError = formState.currencySymbolError != null,
                        singleLine = true,
                        supportingText = { formState.currencySymbolError?.let{ Text(it, color = MaterialTheme.colorScheme.error) } }
                    )
                     OutlinedTextField(
                        value = formState.taxPercentage,
                        onValueChange = viewModel::onTaxPercentageChange,
                        label = { Text("Tax %*") },
                        modifier = Modifier.weight(1f),
                        isError = formState.taxPercentageError != null,
                        singleLine = true,
                        supportingText = { formState.taxPercentageError?.let{ Text(it, color = MaterialTheme.colorScheme.error) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
                Spacer(modifier = Modifier.height(72.dp)) // For FAB
            }
        }
    }
}
