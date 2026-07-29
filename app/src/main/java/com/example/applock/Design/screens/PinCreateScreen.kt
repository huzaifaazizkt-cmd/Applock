package com.example.applock.Design.screens


import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.applock.Design.components.NumberPad


@Composable
fun PinCreateScreen(onNext: (String) -> Unit) {

    var pin by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text("Set Passcode",    style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, fontSize = 20.sp)

        Spacer(modifier = Modifier.height(20.dp))

        Text(pin)

        NumberPad(
            onNumberClick = {
                if (pin.length < 6) pin += it
            },
            onDelete = {
                if (pin.isNotEmpty()) pin = pin.dropLast(1)
            }
        )

        Button(onClick = {
            if (pin.length == 6) onNext(pin)
        }) {
            Text("Next")
        }
    }
}