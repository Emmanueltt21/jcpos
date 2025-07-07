package com.example.jetpackpos.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Inventory : Screen("inventory", "Inventory", Icons.Filled.Inventory)
    object Sales : Screen("sales", "Sales", Icons.Filled.PointOfSale)
    object Transactions : Screen("transactions", "Transactions", Icons.Filled.Assessment)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)

    // Product specific screens (will not be on bottom bar)
    object AddEditProduct : Screen("add_edit_product", "Add/Edit Product") {
        const val ROUTE_WITH_ARG = "add_edit_product?productId={productId}"
        const val ARG_PRODUCT_ID = "productId"
        fun routeWithArgs(productId: Long? = null): String {
            return if (productId != null) {
                "add_edit_product?productId=$productId"
            } else {
                "add_edit_product"
            }
        }
    }
    object ProductDetails : Screen("product_details/{productId}", "Product Details") {
        const val ARG_PRODUCT_ID = "productId"
        fun routeWithArg(productId: Long): String = "product_details/$productId"
    }

    // Order specific screens (will not be on bottom bar)
    object OrderDetails : Screen("order_details/{orderId}", "Order Details") {
        const val ARG_ORDER_ID = "orderId"
        fun routeWithArg(orderId: Long): String = "order_details/$orderId"
    }
}

// List of screens to be shown in the bottom navigation bar
val bottomNavScreens = listOf(
    Screen.Inventory,
    Screen.Sales,
    Screen.Transactions,
    Screen.Settings
)
