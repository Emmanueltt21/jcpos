package com.example.jetpackpos.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.Icons // Consolidated import
import androidx.compose.material.icons.automirrored.filled.ListAlt // For All Orders on Home
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Business // For About on Home
import androidx.compose.material.icons.filled.Category // For Categories
import androidx.compose.material.icons.filled.ExitToApp // For Sign Out on Home
import androidx.compose.material.icons.filled.Group // For Customers
import androidx.compose.material.icons.filled.Home // For Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Payment // For Payment Methods
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store // For Shop Info
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Inventory : Screen("inventory", "Inventory", Icons.Filled.Inventory)
    object POS : Screen("pos", "POS", Icons.Filled.PointOfSale) // Renamed from Sales
    object Transactions : Screen("transactions", "Transactions", Icons.Filled.Assessment)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)

    // Home screen grid items (some might navigate to existing screens)
    object Customers : Screen("customers", "Customers", Icons.Filled.Group)
    object Reports : Screen("reports", "Reports", Icons.Filled.Assessment) // Reusing Assessment icon
    object AllOrders : Screen("all_orders", "All Orders", Icons.AutoMirrored.Filled.ListAlt) // Often same as Transactions
    object AboutApp : Screen("about_app", "About", Icons.Filled.Business)
    object SignOut : Screen("sign_out", "Sign Out", Icons.Filled.ExitToApp)


    // Product specific screens
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
    Screen.Home,
    Screen.POS,
    Screen.Transactions,
    Screen.Settings
)

// Home screen grid item definitions (used for creating the grid)
// Some of these directly map to existing screens or new placeholder screens
val homeGridItems = listOf(
    Screen.Inventory,
    Screen.Customers,
    Screen.Reports,
    Screen.AllOrders, // This will likely navigate to Screen.Transactions
    Screen.AboutApp,
    Screen.SignOut
)

// Screens for Customer Management
object AddEditCustomer : Screen("add_edit_customer", "Add/Edit Customer") {
    const val ROUTE_WITH_ARG = "add_edit_customer?customerId={customerId}"
    const val ARG_CUSTOMER_ID = "customerId"
    fun routeWithArgs(customerId: Long? = null): String {
        return if (customerId != null) {
            "add_edit_customer?customerId=$customerId"
        } else {
            "add_edit_customer"
        }
    }
}

// Screens for Settings sub-sections
object EditShopInfo : Screen("edit_shop_info", "Edit Shop Information")
object CategoryList : Screen("category_list", "Manage Categories", Icons.Filled.Category)
object AddEditCategory : Screen("add_edit_category", "Add/Edit Category") {
     const val ROUTE_WITH_ARG = "add_edit_category?categoryId={categoryId}"
    const val ARG_CATEGORY_ID = "categoryId"
    fun routeWithArgs(categoryId: Long? = null): String {
        return if (categoryId != null) {
            "add_edit_category?categoryId=$categoryId"
        } else {
            "add_edit_category"
        }
    }
}
object PaymentMethodList : Screen("payment_method_list", "Manage Payment Methods", Icons.Filled.Payment) // Placeholder

// Checkout Screen (navigated from POS toolbar cart icon)
object Checkout : Screen("checkout", "Checkout") // Renamed from CartDetails

// Order Status / Receipt Screen
object OrderStatusScreen : Screen("order_status/{orderId}", "Order Status") {
    const val ARG_ORDER_ID = "orderId"
    fun routeWithArg(orderId: Long): String = "order_status/$orderId"
}
