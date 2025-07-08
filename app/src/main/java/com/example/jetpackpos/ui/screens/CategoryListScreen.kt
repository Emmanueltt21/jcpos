package com.example.jetpackpos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.jetpackpos.data.model.Category
import com.example.jetpackpos.ui.navigation.AddEditCategory
import com.example.jetpackpos.ui.viewmodel.CategoryListUiState
import com.example.jetpackpos.ui.viewmodel.CategoryListViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    navController: NavController,
    viewModel: CategoryListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<Category?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { message ->
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Manage Categories") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(AddEditCategory.routeWithArgs()) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Category")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState) {
                is CategoryListUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is CategoryListUiState.Error -> Text(
                    "Error: ${state.message}",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
                is CategoryListUiState.Success -> {
                    if (state.categories.isEmpty()) {
                        Text(
                            "No categories found. Tap '+' to add.",
                            modifier = Modifier.align(Alignment.Center).padding(16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.categories, key = { it.id }) { category ->
                                CategoryListItem(
                                    category = category,
                                    onEditClick = {
                                        navController.navigate(AddEditCategory.routeWithArgs(category.id))
                                    },
                                    onDeleteClick = { showDeleteDialog = category }
                                )
                            }
                        }
                    }
                }
            }
        }

        showDeleteDialog?.let { categoryToDelete ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Delete Category") },
                text = { Text("Are you sure you want to delete '${categoryToDelete.name}'? This may affect products using this category.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteCategory(categoryToDelete)
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
fun CategoryListItem(
    category: Category,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(category.name, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, modifier = Modifier.weight(1f))
            Row {
                IconButton(onClick = onEditClick, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Filled.Edit, "Edit Category")
                }
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Filled.Delete, "Delete Category", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
