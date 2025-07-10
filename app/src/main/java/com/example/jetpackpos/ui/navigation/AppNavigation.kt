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
    startDestination: String, // Added startDestination parameter
import com.example.jetpackpos.ui.screens.AboutScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.remember // Added import
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.jetpackpos.ui.screens.* // Import all screens

@Composable
fun AppNavigationGraph(
    navController: NavHostController,
    startDestination: String, // Added startDestination parameter
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination, // Use the parameter
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.Inventory.route) {
            InventoryScreen(navController)
        }
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.navigation
import com.example.jetpackpos.ui.viewmodel.CartViewModel
import com.example.jetpackpos.ui.viewmodel.ProductListViewModel

        navigation(startDestination = Screen.POS.route, route = NavGraphRoutes.POS_CHECKOUT_GRAPH) {
            composable(Screen.POS.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(NavGraphRoutes.POS_CHECKOUT_GRAPH) }
                val cartViewModel: CartViewModel = hiltViewModel(parentEntry)
                val productListViewModel: ProductListViewModel = hiltViewModel() // POS still needs its own ProductListViewModel
                POSScreen(navController, productListViewModel, cartViewModel)
            }
            composable(Checkout.route) { backStackEntry -> // Renamed from CartDetails.route
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(NavGraphRoutes.POS_CHECKOUT_GRAPH) }
                val cartViewModel: CartViewModel = hiltViewModel(parentEntry)
                CheckoutScreen(navController, cartViewModel) // Renamed from CartDetailsScreen
            }
        }
        composable(Screen.Transactions.route) {
            TransactionsScreen(navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }

        // Placeholder screens from Home
        composable(Screen.Customers.route) {
            CustomersScreen(navController)
        }
        composable(Screen.Reports.route) {
            ReportsScreen(navController)
        }
        composable(Screen.AboutApp.route) {
            AboutScreen(navController)
        }
        // Screen.AllOrders navigates to Screen.Transactions, handled in HomeScreen
        // Screen.SignOut is an action, not a screen, handled in HomeScreen

        composable(
            route = AddEditCustomer.ROUTE_WITH_ARG,
            arguments = listOf(navArgument(AddEditCustomer.ARG_CUSTOMER_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) {
            AddEditCustomerScreen(navController = navController)
        }

        composable(EditShopInfo.route) {
            EditShopInfoScreen(navController = navController)
        }
        composable(CategoryList.route) {
            CategoryListScreen(navController = navController)
        }
        composable(
            route = AddEditCategory.ROUTE_WITH_ARG,
            arguments = listOf(navArgument(AddEditCategory.ARG_CATEGORY_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) {
            AddEditCategoryScreen(navController = navController)
        }

        composable(Checkout.route) { // Renamed from CartDetails.route
            CheckoutScreen(navController = navController) // Renamed from CartDetailsScreen
        }

        composable(
            route = OrderStatusScreen.route, // Uses "order_status/{orderId}"
            arguments = listOf(navArgument(OrderStatusScreen.ARG_ORDER_ID) { type = NavType.LongType })
        ) {
            OrderStatusScreen(navController = navController)
        }

        composable(
            route = Screen.AddEditProduct.ROUTE_WITH_ARG,
            arguments = listOf(navArgument(Screen.AddEditProduct.ARG_PRODUCT_ID) {
                type = NavType.StringType // Use StringType for optional Long
                nullable = true
                defaultValue = null
            })
        ) {
             // ViewModel now handles this via SavedStateHandle
            AddEditProductScreen(navController = navController)
        }

        composable(
            route = Screen.ProductDetails.route, // Uses "product_details/{productId}"
            arguments = listOf(navArgument(Screen.ProductDetails.ARG_PRODUCT_ID) { type = NavType.LongType })
        ) {
            // ProductId is obtained by ViewModel via SavedStateHandle
            ProductDetailsScreen(navController = navController)
        }

        composable(
            route = Screen.OrderDetails.route, // Uses "order_details/{orderId}"
            arguments = listOf(navArgument(Screen.OrderDetails.ARG_ORDER_ID) { type = NavType.LongType })
        ) {
            // OrderId is obtained by ViewModel via SavedStateHandle
            OrderDetailsScreen(navController = navController)
        }
    }
}
