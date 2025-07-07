package com.example.jetpackpos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.jetpackpos.ui.screens.AddEditProductScreen
import com.example.jetpackpos.ui.screens.InventoryScreen
import com.example.jetpackpos.ui.screens.OrderDetailsScreen
import com.example.jetpackpos.ui.screens.ProductDetailsScreen
import com.example.jetpackpos.ui.screens.SalesScreen
import com.example.jetpackpos.ui.screens.SettingsScreen
import com.example.jetpackpos.ui.screens.TransactionsScreen

@Composable
fun AppNavigationGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Inventory.route, // Default start screen
        modifier = modifier
    ) {
        composable(Screen.Inventory.route) {
            InventoryScreen(navController)
        }
        composable(Screen.Sales.route) {
            SalesScreen(navController)
        }
        composable(Screen.Transactions.route) {
            TransactionsScreen(navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }

        composable(
            route = Screen.AddEditProduct.ROUTE_WITH_ARG,
            arguments = listOf(navArgument(Screen.AddEditProduct.ARG_PRODUCT_ID) {
                type = NavType.StringType // Use StringType for optional Long
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val productIdString = backStackEntry.arguments?.getString(Screen.AddEditProduct.ARG_PRODUCT_ID)
            AddEditProductScreen(
                navController = navController,
                productId = productIdString?.toLongOrNull()
            )
        }

        composable(
            route = Screen.ProductDetails.route, // Uses "product_details/{productId}"
            arguments = listOf(navArgument(Screen.ProductDetails.ARG_PRODUCT_ID) { type = NavType.LongType })
        ) { backStackEntry ->
            ProductDetailsScreen(
                navController = navController,
                productId = backStackEntry.arguments!!.getLong(Screen.ProductDetails.ARG_PRODUCT_ID)
            )
        }

        composable(
            route = Screen.OrderDetails.route, // Uses "order_details/{orderId}"
            arguments = listOf(navArgument(Screen.OrderDetails.ARG_ORDER_ID) { type = NavType.LongType })
        ) { backStackEntry ->
            OrderDetailsScreen(
                navController = navController,
                orderId = backStackEntry.arguments!!.getLong(Screen.OrderDetails.ARG_ORDER_ID)
            )
        }
    }
}
