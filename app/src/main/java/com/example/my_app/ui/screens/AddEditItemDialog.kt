package com.example.my_app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddEditItemDialog(
    title: String,
    initialTitle: String = "",
    initialDescription: String = "",
    initialPrice: String = "",
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, price: Double) -> Unit
) {
    var t by remember { mutableStateOf(initialTitle) }
    var d by remember { mutableStateOf(initialDescription) }
    var p by remember { mutableStateOf(initialPrice) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(initialTitle, initialDescription, initialPrice) {
        t = initialTitle
        d = initialDescription
        p = initialPrice
        error = null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = t,
                    onValueChange = { t = it; error = null },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = d,
                    onValueChange = { d = it; error = null },
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = p,
                    onValueChange = { p = it; error = null },
                    label = { Text("Цена (например 9999.99)") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(error!!)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val price = p.replace(",", ".").toDoubleOrNull()
                if (t.isBlank()) {
                    error = "Введите название"
                    return@TextButton
                }
                if (price == null) {
                    error = "Цена должна быть числом"
                    return@TextButton
                }
                onConfirm(t, d, price)
            }) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
