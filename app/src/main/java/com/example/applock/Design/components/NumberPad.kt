package com.example.applock.Design.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NumberPad(
    onNumberClick: (String) -> Unit,
    onDelete: () -> Unit
) {

    val numbers = listOf(
        "1","2","3",
        "4","5","6",
        "7","8","9",
        "","0","X"
    )

    LazyVerticalGrid(columns = GridCells.Fixed(3)) {
        items(numbers.size) { i ->

            val num = numbers[i]

            Button(
                onClick = {
                    when (num) {
                        "X" -> onDelete()
                        "" -> {}
                        else -> onNumberClick(num)
                    }
                },
                modifier = Modifier.padding(10.dp)
            ) {
                Text(num)
            }
        }
    }
}