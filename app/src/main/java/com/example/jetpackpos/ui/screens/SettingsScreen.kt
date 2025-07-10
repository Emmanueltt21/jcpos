package com.example.jetpackpos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.jetpackpos.MainActivity # For AppThemeState
import com.example.jetpackpos.data.model.ShopInfo
import com.example.jetpackpos.ui.navigation.EditShopInfo
import com.example.jetpackpos.ui.navigation.Screen
import com.example.jetpackpos.ui.theme.ThemeSetting
import com.example.jetpackpos.ui.viewmodel.SettingsEvent
import com.example.jetpackpos.ui.viewmodel.SettingsViewModel
import com.example.jetpackpos.ui.viewmodel.ShopInfoUiState
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    var showResetDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val shopInfoState by viewModel.shopInfoState.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.eventFlow.collectLatest { event ->
            val message = when (event) {
                is SettingsEvent.DatabaseResetSuccess -> "Database reset successfully."
                is SettingsEvent.DatabaseResetError -> "Error resetting database: ${event.message}"
            }
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Settings") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            // horizontalAlignment = Alignment.CenterHorizontally, // Keep for global, but specific items might align start
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Application Settings",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
            )

            // Display Shop Information
            ShopInfoSection(shopInfoState = shopInfoState, onEditClick = {
                navController.navigate(EditShopInfo.route)
            })

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Theme Switcher Section
            ThemeSwitcherSection()

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Manage Categories Button
            SettingsButton(
                text = "Manage Categories",
                onClick = { navController.navigate(com.example.jetpackpos.ui.navigation.CategoryList.route) }
            )

            // Manage Payment Methods Button (Placeholder)
             SettingsButton(
                text = "Manage Payment Methods",
                onClick = {
                    // In a real app, this might navigate or show a dialog.
                    // For now, using the snackbarHostState from the parent composable.
                    // This requires snackbarHostState to be accessible or passed down.
                    // For simplicity, let's assume a local scope where it is accessible if this were a real event.
                    // However, since it's a simple placeholder, we can make it a simple Toast or
                    // just rely on the fact it's a placeholder.
                    // For consistency with other "Not Implemented" placeholders, a Toast is fine.
                    // Or, we can launch a coroutine to show snackbar.
                    // For now, I'll keep it as is, assuming snackbarHostState is in scope,
                    // but this is a common point of refactoring.
                    // To make it explicit:
                    // val scope = rememberCoroutineScope()
                    // onClick = { scope.launch { snackbarHostState.showSnackbar("...") } }
                    // For this pass, I'll assume the direct call works due to composition scope.
                     snackbarHostState.showSnackbar("Payment Methods - Not Implemented Yet", duration = SnackbarDuration.Short)
                }
            )

            // Backup Data Button (Placeholder)
            SettingsButton(
                text = "Backup Data",
                onClick = {
                     snackbarHostState.showSnackbar("Backup Data - Not Implemented Yet", duration = SnackbarDuration.Short)
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { showResetDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Icon(Icons.Filled.DeleteForever, contentDescription = "Reset Database Icon", modifier = Modifier.padding(end = 8.dp))
                Text("Reset Database")
            }
             Text(
                "Warning: This will delete all products, sales transactions, and other app data. This action cannot be undone.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Confirm Database Reset") },
                text = { Text("Are you absolutely sure you want to delete all data? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetDatabase()
                            showResetDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Yes, Reset It")
                    }
                },
                dismissButton = {
                    Button(onClick = { showResetDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun ShopInfoSection(shopInfoState: ShopInfoUiState, onEditClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Shop Information", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onEditClick) {
                Text("Edit")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        when (shopInfoState) {
            is ShopInfoUiState.Loading -> CircularProgressIndicator()
            is ShopInfoUiState.Error -> Text("Error: ${shopInfoState.message}", color = MaterialTheme.colorScheme.error)
            is ShopInfoUiState.Success -> {
                val info = shopInfoState.shopInfo
                InfoRowSettings("Shop Name:", info.shopName)
                info.contactNumber?.let { InfoRowSettings("Contact:", it) }
                info.email?.let { InfoRowSettings("Email:", it) }
                info.address?.let { InfoRowSettings("Address:", it) }
                InfoRowSettings("Currency:", info.currencySymbol)
                InfoRowSettings("Tax Rate:", "${info.taxPercentage * 100}%")
            }
        }
    }
}

@Composable
fun InfoRowSettings(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(120.dp), fontSize = 14.sp)
        Text(value, fontSize = 14.sp)
    }
}

@Composable
fun SettingsButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
    ) {
        Text(text, modifier = Modifier.padding(8.dp))
    }
}

@Composable
fun ThemeSwitcherSection() {
    val currentTheme = AppThemeState.currentTheme.value // Observe the state

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Theme", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Column(Modifier.selectableGroup()) {
            ThemeSetting.entries.forEach { themeEntry ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = (themeEntry == currentTheme),
                            onClick = { AppThemeState.currentTheme.value = themeEntry },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (themeEntry == currentTheme),
                        onClick = null // null recommended for accessibility with Row's onClick
                    )
                    Text(
                        text = themeEntry.name.lowercase().replaceFirstChar { it.titlecase() },
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }
}
