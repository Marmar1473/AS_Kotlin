package com.example.my_app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_app.ui.theme.My_appTheme
import com.example.my_app.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            My_appTheme {
                DiscountCalculatorScreen(viewModel)
            }
        }
    }
}

@Composable
fun DiscountCalculatorScreen(viewModel: MainViewModel) {
    val uiState by viewModel.state.collectAsState()

    var amountInput by remember { mutableStateOf("") }
    var discountInput by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Калькулятор скидки",
                fontSize = 24.sp,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = amountInput,
                onValueChange = { amountInput = it },
                label = { Text("Сумма (₸)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = discountInput,
                onValueChange = { discountInput = it },
                label = { Text("Скидка (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.calculate(amountInput, discountInput) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Рассчитать")
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.errorText.isNotEmpty()) {
                Text(
                    text = uiState.errorText,
                    color = Color.Red,
                    fontSize = 16.sp
                )
            }

            if (uiState.isValid && uiState.resultText.isNotEmpty()) {
                Text(
                    text = uiState.resultText,
                    color = Color(0xFF4CAF50),
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}