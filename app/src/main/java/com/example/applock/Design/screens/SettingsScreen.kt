package com.example.applock.Design.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen() {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = "Settings",
            color = Color(0xFF333333),
            fontSize = 22.sp
        )
    }
}