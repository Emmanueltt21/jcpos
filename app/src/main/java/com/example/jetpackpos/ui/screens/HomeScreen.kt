package com.example.jetpackpos.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.jetpackpos.ui.navigation.Screen
import com.example.jetpackpos.ui.navigation.homeGridItems

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Home") })
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(homeGridItems, key = { it.route }) { screen ->
                HomeGridItemView(
                    screen = screen,
                    onClick = {
                        when (screen) {
                            Screen.Inventory -> navController.navigate(Screen.Inventory.route)
                            Screen.AllOrders -> navController.navigate(Screen.Transactions.route) // Assuming AllOrders maps to Transactions
                            Screen.Customers -> navController.navigate(Screen.Customers.route)
                            Screen.Reports -> navController.navigate(Screen.Reports.route)
                            Screen.AboutApp -> navController.navigate(Screen.AboutApp.route)
                            Screen.SignOut -> {
                                // Handle Sign Out logic, for now a Toast
                                Toast.makeText(context, "Sign Out Clicked (Not Implemented)", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                // Should not happen if homeGridItems are well defined
                                Toast.makeText(context, "${screen.title} Clicked (Not Implemented)", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun HomeGridItemView(screen: Screen, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Makes items square-ish
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            screen.icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = screen.title,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(
                text = screen.title,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
