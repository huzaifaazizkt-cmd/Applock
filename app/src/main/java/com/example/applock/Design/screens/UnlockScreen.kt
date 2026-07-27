package com.example.applock.Design.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.applock.data.DataStoreManager

@Composable
fun UnlockScreen(onUnlockSuccess: () -> Unit) {

    val context = LocalContext.current
    val dataStore = DataStoreManager(context)

    var pin by remember { mutableStateOf("") }
    var savedPin by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        dataStore.getPin().collect {
            if (it != null) savedPin = it
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text("Enter PIN")

        Spacer(modifier = Modifier.height(10.dp))

        TextField(
            value = pin,
            onValueChange = { pin = it }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            if (pin == savedPin) {
                onUnlockSuccess()
            }
        }) {
            Text("Unlock")
        }
    }
}