package com.example.my_app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.core.net.toUri

@Composable
fun AddEditItemDialog(
    title: String,
    initialTitle: String = "",
    initialDescription: String = "",
    initialPrice: String = "",
    initialImageUri: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, price: Double, imageUri: String?) -> Unit
) {
    val context = LocalContext.current
    var t by remember { mutableStateOf(initialTitle) }
    var d by remember { mutableStateOf(initialDescription) }
    var p by remember { mutableStateOf(initialPrice) }
    var selectedUri by remember { mutableStateOf(initialImageUri?.toUri()) }
    var error by remember { mutableStateOf<String?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            selectedUri = uri
        }
    }

    LaunchedEffect(initialTitle, initialDescription, initialPrice, initialImageUri) {
        t = initialTitle
        d = initialDescription
        p = initialPrice
        selectedUri = initialImageUri?.toUri()
        error = null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray)
                        .clickable {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedUri != null) {
                        AsyncImage(
                            model = selectedUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text("Нажмите, чтобы выбрать фото", color = Color.DarkGray)
                    }
                }

                Spacer(Modifier.height(10.dp))

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
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val price = p.replace(",", ".").replace("\\s+".toRegex(), "").toDoubleOrNull()
                when {
                    t.isBlank() -> error = "Название не может быть пустым"
                    price == null -> error = "Введите корректную цену (например 9999.99)"
                    else -> onConfirm(t, d, price, selectedUri?.toString())
                }
            }) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}