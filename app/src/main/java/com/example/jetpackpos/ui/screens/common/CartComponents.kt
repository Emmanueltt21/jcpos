package com.example.jetpackpos.ui.screens.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jetpackpos.data.model.CartItem
import java.text.NumberFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartSummaryView(
    modifier: Modifier = Modifier,
    cartItems: List<CartItem>,
    subtotal: Double,
    taxAmount: Double,
    total: Double,
    onQuantityChange: (productId: Long, newQuantity: Int) -> Unit,
    onRemoveItem: (productId: Long) -> Unit,
    onClearCart: () -> Unit,
    onCheckout: () -> Unit,
    isCheckoutButtonVisible: Boolean = true // To hide it in POSScreen's summary
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }

    Column(modifier = modifier.fillMaxHeight()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Current Cart", style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = onClearCart, enabled = cartItems.isNotEmpty()) {
                Icon(Icons.Filled.RemoveShoppingCart, "Clear Cart")
            }
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        if (cartItems.isEmpty()) {
            Text(
                "Cart is empty.",
                modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally),
                textAlign = TextAlign.Center
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(cartItems, key = { it.id }) { item ->
                    CartListItemView(
                        item = item,
                        onQuantityChange = { newQuantity -> onQuantityChange(item.productId, newQuantity) },
                        onRemoveItem = { onRemoveItem(item.productId) },
                        currencyFormat = currencyFormat
                    )
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        CartTotalRow("Subtotal:", currencyFormat.format(subtotal))
        CartTotalRow("Tax:", currencyFormat.format(taxAmount))
        CartTotalRow("Total:", currencyFormat.format(total), isTotal = true)

        if(isCheckoutButtonVisible) {
            Button(
                onClick = onCheckout,
                enabled = cartItems.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text("Proceed to Checkout")
            }
        }
    }
}

@Composable
fun CartListItemView(
    item: CartItem,
    onQuantityChange: (Int) -> Unit,
    onRemoveItem: () -> Unit,
    currencyFormat: NumberFormat
) {
    var quantityInput by remember(item.quantityInCart) { mutableStateOf(item.quantityInCart.toString()) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Card(border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(item.productName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text(
                        currencyFormat.format(item.price) + " x ${item.quantityInCart} = " + currencyFormat.format(item.getTotalPrice()),
                        fontSize = 13.sp
                    )
                }
                IconButton(onClick = onRemoveItem, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.DeleteForever, "Remove Item", tint = MaterialTheme.colorScheme.error)
                }
            }
            OutlinedTextField(
                value = quantityInput,
                onValueChange = { value ->
                    quantityInput = value.filter { it.isDigit() }.take(3) // Allow only digits, max 3
                },
                label = { Text("Qty", fontSize = 12.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    onQuantityChange(quantityInput.toIntOrNull() ?: 0)
                    keyboardController?.hide()
                }),
                modifier = Modifier.fillMaxWidth().height(60.dp),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
            )
        }
    }
}

@Composable
fun CartTotalRow(label: String, amount: String, isTotal: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal, fontSize = if(isTotal) 18.sp else 16.sp)
        Text(
            amount,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            fontSize = if(isTotal) 18.sp else 16.sp,
            color = if(isTotal) MaterialTheme.colorScheme.primary else LocalContentColor.current
        )
    }
}
