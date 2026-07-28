package com.example.applock.Design.screens

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.applock.Design.components.NumberPad
import com.example.applock.data.DataStoreManager
import kotlinx.coroutines.launch

@Composable
fun PinConfirmScreen(
    navController: NavController,
    context: Context,
    pin: String
) {

    val scope = rememberCoroutineScope()
    val dataStore = DataStoreManager(context)

    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }   // ✅ NEW

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text("Confirm PIN")

        Spacer(modifier = Modifier.height(10.dp))

        // ✅ ERROR TEXT SHOW
        if (error.isNotEmpty()) {
            Text(
                text = error,
                color = Color.Red
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(confirmPin)

        Spacer(modifier = Modifier.height(20.dp))

        NumberPad(
            onNumberClick = {
                if (confirmPin.length < 6) {
                    confirmPin += it
                    error = ""   // ✅ typing pe error hide
                }
            },
            onDelete = {
                if (confirmPin.isNotEmpty()) {
                    confirmPin = confirmPin.dropLast(1)
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {

            if (confirmPin == pin) {

                scope.launch {
                    dataStore.savePin(pin)
                }

                navController.navigate("appList")

            } else {
                error = "Enter your correct password"   // ❌ ERROR SHOW
            }

        }) {
            Text("Next")
        }
    }
}