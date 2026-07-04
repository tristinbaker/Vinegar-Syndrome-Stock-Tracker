package com.tristinbaker.vsalerts.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tristinbaker.vsalerts.network.ProductDetail
import com.tristinbaker.vsalerts.network.ProductVariant
import com.tristinbaker.vsalerts.util.formatCents
import com.tristinbaker.vsalerts.util.formatStockLabel

@Composable
fun VariantPickerDialog(
    product: ProductDetail,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (ProductVariant) -> Unit,
    title: String = "Track which edition?",
    confirmLabel: String = "Track",
) {
    val defaultVariant = product.variants.firstOrNull { it.title.contains("Limited", ignoreCase = true) }
        ?: product.variants.firstOrNull()
    var selected by remember(product.handle) { mutableStateOf(defaultVariant) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                product.variants.forEach { variant ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = variant.id == selected?.id,
                                onClick = { selected = variant },
                            )
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = variant.id == selected?.id, onClick = { selected = variant })
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(variant.title)
                            val stockLabel = variant.inventoryQuantity?.let { formatStockLabel(it) } ?: "stock unknown"
                            Text("${formatCents(variant.price)} · $stockLabel")
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.padding(8.dp))
            } else {
                TextButton(
                    onClick = { selected?.let(onConfirm) },
                    enabled = selected != null,
                ) {
                    Text(confirmLabel)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
