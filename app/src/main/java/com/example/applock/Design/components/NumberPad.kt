package com.example.applock.Design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NumberPad(
    onNumberClick: (String) -> Unit,
    onDelete: () -> Unit,
    buttonColor: Color = Color(0xFF69B9F3)
) {

    val numbers = listOf(
        "1", "2", "3",
        "4", "5", "6",
        "7", "8", "9",
        "", "0", "X"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        numbers.chunked(3).forEach { row ->

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                row.forEach { number ->

                    Box(
                        modifier = Modifier
                            .size(75.dp)
                            .background(
                                color = if (number.isEmpty()) {
                                    Color.Transparent
                                } else {
                                    buttonColor
                                },
                                shape = CircleShape
                            )
                            .clickable(
                                enabled = number.isNotEmpty()
                            ) {

                                when (number) {

                                    "X" -> {
                                        onDelete()
                                    }

                                    else -> {
                                        onNumberClick(number)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {

                        if (number.isNotEmpty()) {

                            Text(
                                text = number,
                                color = Color.White,
                                fontSize = 34.sp
                            )
                        }
                    }
                }
            }
        }
    }
}