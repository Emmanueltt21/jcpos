package com.example.jetpackpos.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.example.jetpackpos.ui.screens.* // Covers all screen composables
import com.example.jetpackpos.ui.viewmodel.CartViewModel
import com.example.jetpackpos.ui.viewmodel.ProductListViewModel


@Composable
fun AppNavigationGraph(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.Inventory.route) {
            InventoryScreen(navController)
        }

        // Nested graph for POS and Checkout to share CartViewModel
        navigation(startDestination = Screen.POS.route, route = NavGraphRoutes.POS_CHECKOUT_GRAPH) {
            composable(Screen.POS.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(NavGraphRoutes.POS_CHECKOUT_GRAPH) }
                val cartViewModel: CartViewModel = hiltViewModel(parentEntry)
                val productListViewModel: ProductListViewModel = hiltViewModel()
                POSScreen(navController, productListViewModel, cartViewModel)
            }
            composable(Checkout.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(NavGraphRoutes.POS_CHECKOUT_GRAPH) }
                val cartViewModel: CartViewModel = hiltViewModel(parentEntry)
                CheckoutScreen(navController, cartViewModel)
            }
        }

        composable(Screen.Transactions.route) {
            TransactionsScreen(navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }

        // Placeholder/Actual screens from Home
        composable(Screen.Customers.route) {
            CustomersScreen(navController)
        }
        composable(Screen.Reports.route) {
            ReportsScreen(navController)
        }
        composable(Screen.AboutApp.route) {
            AboutScreen(navController)
        }

        // Add/Edit Screens
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

        composable(
            route = OrderStatusScreen.route,
            arguments = listOf(navArgument(OrderStatusScreen.ARG_ORDER_ID) { type = NavType.LongType })
        ) {
            OrderStatusScreen(navController = navController)
        }

        composable(
            route = Screen.AddEditProduct.ROUTE_WITH_ARG,
            arguments = listOf(navArgument(Screen.AddEditProduct.ARG_PRODUCT_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) {
            AddEditProductScreen(navController = navController)
        }

        composable(
            route = Screen.ProductDetails.route,
            arguments = listOf(navArgument(Screen.ProductDetails.ARG_PRODUCT_ID) { type = NavType.LongType })
        ) {
            ProductDetailsScreen(navController = navController)
        }

        composable(
            route = Screen.OrderDetails.route,
            arguments = listOf(navArgument(Screen.OrderDetails.ARG_ORDER_ID) { type = NavType.LongType })
        ) {
            OrderDetailsScreen(navController = navController)
        }
    }
}
