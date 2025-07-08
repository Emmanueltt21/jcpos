package com.example.jetpackpos.ui.screens.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jetpackpos.data.model.OrderItem
import java.text.NumberFormat

@Composable
fun OrderItemView(item: OrderItem, currencyFormat: NumberFormat) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.productNameAtPurchase, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text("SKU: ${item.productSkuAtPurchase}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "${currencyFormat.format(item.priceAtPurchase)} x ${item.quantitySold}",
                fontSize = 14.sp
            )
        }
        Text(
            currencyFormat.format(item.priceAtPurchase * item.quantitySold),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
