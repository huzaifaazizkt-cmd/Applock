package com.example.applock.Design.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.applock.data.DataStoreManager
import kotlinx.coroutines.flow.first

@Composable
fun UnlockScreen(onUnlockSuccess: () -> Unit) {

    val context = LocalContext.current
    val dataStore = DataStoreManager(context)

    var pin by remember { mutableStateOf("") }
    var savedPin by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        savedPin = dataStore.getPin().first() ?: "123456"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { }
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // 🔹 Title
        Text(
            text = "Enter PIN",
            color = Color.White,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

        // 🔹 PIN Circles
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            repeat(6) { index ->
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            if (index < pin.length) Color.White else Color.Gray,
                            shape = CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // 🔹 Number Pad
        val numbers = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "⌫")
        )

        numbers.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.padding(5.dp)
            ) {
                row.forEach { num ->
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clickable {
                                when (num) {
                                    "⌫" -> {
                                        if (pin.isNotEmpty()) {
                                            pin = pin.dropLast(1)
                                        }
                                    }

                                    "" -> {}

                                    else -> {
                                        if (pin.length < 6) {
                                            pin += num
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = num,
                            color = Color.White,
                            fontSize = 22.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 🔹 Unlock Button
        Button(
            onClick = {
                if (pin == savedPin) {
                    onUnlockSuccess()
                } else {
                    pin = ""
                }
            }
        ) {
            Text("Unlock")
        }
    }
}