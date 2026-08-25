package com.example.applock.Design.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.applock.Design.components.NumberPad
import kotlin.math.sqrt

@Composable
fun PinCreateScreen(
    onNext: (String, String) -> Unit
) {

    var pin by remember { mutableStateOf("") }

    var pinLength by remember { mutableStateOf(6) }

    var authType by remember {
        mutableStateOf("6 - Digit pin")
    }

    var expanded by remember {
        mutableStateOf(false)
    }

    var selectedDots by remember {
        mutableStateOf<List<Int>>(emptyList())
    }

    val isPattern = authType == "Pattern"

    val backgroundColor = Color(0xFF189FFF)

    val patternDotColor = Color(0xFF83CCFF)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 26.dp,
                    end = 26.dp,
                    top = 24.dp,
                    bottom = 20.dp
                ),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            // =====================================================
            // TOP BAR
            // =====================================================

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(35.dp),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Box {

                    Row(
                        modifier = Modifier
                            .height(35.dp)
                            .clickable {
                                expanded = true
                            },

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Text(
                            text = authType,
                            color = Color.White,
                            fontSize = 14.sp
                        )

                        Icon(
                            imageVector =
                                Icons.Default.KeyboardArrowDown,

                            contentDescription = null,

                            tint = Color.White,

                            modifier =
                                Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,

                        onDismissRequest = {
                            expanded = false
                        }
                    ) {

                        DropdownMenuItem(
                            text = {
                                Text("4 - Digit pin")
                            },

                            onClick = {

                                authType =
                                    "4 - Digit pin"

                                pinLength = 4
                                pin = ""
                                selectedDots = emptyList()

                                expanded = false
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text("6 - Digit pin")
                            },

                            onClick = {

                                authType =
                                    "6 - Digit pin"

                                pinLength = 6
                                pin = ""
                                selectedDots = emptyList()

                                expanded = false
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text("Pattern")
                            },

                            onClick = {

                                authType = "Pattern"

                                pin = ""
                                selectedDots = emptyList()

                                expanded = false
                            }
                        )
                    }
                }
            }

            // =====================================================
            // STEP INDICATOR
            // =====================================================

            Spacer(
                modifier = Modifier.height(38.dp)
            )

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(31.dp)
                        .background(
                            Color.White.copy(alpha = 0.08f),
                            CircleShape
                        )
                        .border(
                            2.dp,
                            Color.White.copy(alpha = 0.35f),
                            CircleShape
                        ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Box(
                        modifier = Modifier
                            .size(21.dp)
                            .background(
                                Color.White,
                                CircleShape
                            ),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            text = "1",
                            color = backgroundColor,
                            fontSize = 15.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(2.dp)
                        .background(Color.White)
                )

                Box(
                    modifier = Modifier
                        .size(25.dp)
                        .border(
                            2.dp,
                            Color.White,
                            CircleShape
                        ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Text(
                        text = "2",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(65.dp)
            )

            // =====================================================
            // PIN
            // =====================================================

            if (!isPattern) {

                Text(
                    text =
                        "Create $pinLength - Digit pin",

                    color = Color.White,
                    fontSize = 18.sp
                )

                Spacer(
                    modifier = Modifier.height(18.dp)
                )

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(14.dp)
                ) {

                    repeat(pinLength) { index ->

                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .border(
                                    1.5.dp,
                                    Color.White,
                                    CircleShape
                                )
                                .background(
                                    if (index < pin.length)
                                        Color.White
                                    else
                                        Color.Transparent,
                                    CircleShape
                                )
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(82.dp)
                )

                NumberPad(

                    onNumberClick = { number ->

                        if (pin.length < pinLength) {
                            pin += number
                        }
                    },

                    onDelete = {

                        if (pin.isNotEmpty()) {
                            pin = pin.dropLast(1)
                        }
                    }
                )

                Spacer(
                    modifier = Modifier.height(55.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 30.dp),

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            30.dp,
                            Alignment.End
                        )
                ) {

                    Text(
                        text = "Reset",

                        color =
                            if (pin.isEmpty())
                                Color.White.copy(alpha = 0.35f)
                            else
                                Color.White,

                        fontSize = 20.sp,

                        modifier =
                            Modifier
                                .clickable(
                                    enabled =
                                        pin.isNotEmpty()
                                ) {
                                    pin = ""
                                }
                                .padding(10.dp)
                    )

                    Text(
                        text = "Continue",

                        color =
                            if (pin.length == pinLength)
                                Color.White
                            else
                                Color.White.copy(alpha = 0.35f),

                        fontSize = 20.sp,

                        modifier =
                            Modifier
                                .clickable(
                                    enabled =
                                        pin.length == pinLength
                                ) {

                                    onNext(
                                        "pin",
                                        pin
                                    )
                                }
                                .padding(10.dp)
                    )
                }

            } else {

                // =================================================
                // PATTERN
                // =================================================

                Text(
                    text = "Create Pattern",

                    color = Color.White,
                    fontSize = 18.sp
                )

                Spacer(
                    modifier = Modifier.height(25.dp)
                )

                Text(
                    text = "Connect at least 4 dots",

                    color =
                        Color.White.copy(alpha = 0.85f),

                    fontSize = 14.sp
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                PatternGrid(
                    selectedDots = selectedDots,

                    onPatternChanged = { dots ->
                        selectedDots = dots
                    },

                    dotColor = patternDotColor,

                    backgroundColor = backgroundColor
                )

                Spacer(
                    modifier = Modifier.height(40.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 30.dp),

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            30.dp,
                            Alignment.End
                        )
                ) {

                    Text(
                        text = "Reset",

                        color =
                            if (selectedDots.isEmpty())
                                Color.White.copy(alpha = 0.35f)
                            else
                                Color.White,

                        fontSize = 20.sp,

                        modifier =
                            Modifier
                                .clickable(
                                    enabled =
                                        selectedDots.isNotEmpty()
                                ) {
                                    selectedDots = emptyList()
                                }
                                .padding(10.dp)
                    )

                    Text(
                        text = "Continue",

                        color =
                            if (selectedDots.size >= 4)
                                Color.White
                            else
                                Color.White.copy(alpha = 0.35f),

                        fontSize = 20.sp,

                        modifier =
                            Modifier
                                .clickable(
                                    enabled =
                                        selectedDots.size >= 4
                                ) {

                                    onNext(
                                        "pattern",
                                        selectedDots.joinToString("-")
                                    )
                                }
                                .padding(10.dp)
                    )
                }
            }
        }
    }
}


// =============================================================
// PATTERN GRID
// =============================================================

@Composable
fun PatternGrid(
    selectedDots: List<Int>,
    onPatternChanged: (List<Int>) -> Unit,
    dotColor: Color,
    backgroundColor: Color
) {

    val latestOnPatternChanged by
    rememberUpdatedState(onPatternChanged)

    Box(
        modifier = Modifier
            .size(300.dp)
            .pointerInput(Unit) {

                var currentDots =
                    mutableListOf<Int>()

                detectDragGestures(

                    onDragStart = { offset ->

                        currentDots =
                            mutableListOf()

                        val dot =
                            findDot(
                                offset,
                                size.width.toFloat(),
                                size.height.toFloat()
                            )

                        if (dot != null) {

                            currentDots.add(dot)

                            latestOnPatternChanged(
                                currentDots.toList()
                            )
                        }
                    },

                    onDrag = { change, _ ->

                        change.consume()

                        val dot =
                            findDot(
                                change.position,
                                size.width.toFloat(),
                                size.height.toFloat()
                            )

                        if (
                            dot != null &&
                            !currentDots.contains(dot)
                        ) {

                            currentDots.add(dot)

                            latestOnPatternChanged(
                                currentDots.toList()
                            )
                        }
                    },

                    onDragEnd = {},

                    onDragCancel = {}
                )
            }
    ) {

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {

            val positions =
                getGridPositions(
                    size.width,
                    size.height
                )

            if (selectedDots.size >= 2) {

                for (
                i in 0 until selectedDots.size - 1
                ) {

                    drawLine(
                        color = Color.White,

                        start =
                            positions[selectedDots[i]],

                        end =
                            positions[selectedDots[i + 1]],

                        strokeWidth = 8f
                    )
                }
            }

            positions.forEachIndexed { index, position ->

                val selected =
                    selectedDots.contains(index)

                drawCircle(
                    color = dotColor,
                    radius = 17.5.dp.toPx(),
                    center = position
                )

                drawCircle(
                    color = backgroundColor,
                    radius = 13.5.dp.toPx(),
                    center = position
                )

                drawCircle(
                    color =
                        if (selected)
                            Color.White
                        else
                            dotColor,

                    radius = 9.dp.toPx(),

                    center = position
                )
            }
        }
    }
}


private fun getGridPositions(
    width: Float,
    height: Float
): List<Offset> {

    val x1 = width * 0.1667f
    val x2 = width * 0.5f
    val x3 = width * 0.8333f

    val y1 = height * 0.1667f
    val y2 = height * 0.5f
    val y3 = height * 0.8333f

    return listOf(
        Offset(x1, y1),
        Offset(x2, y1),
        Offset(x3, y1),

        Offset(x1, y2),
        Offset(x2, y2),
        Offset(x3, y2),

        Offset(x1, y3),
        Offset(x2, y3),
        Offset(x3, y3)
    )
}


private fun findDot(
    touch: Offset,
    width: Float,
    height: Float
): Int? {

    val positions =
        getGridPositions(width, height)

    positions.forEachIndexed { index, dot ->

        val dx = touch.x - dot.x
        val dy = touch.y - dot.y

        val distance =
            sqrt(dx * dx + dy * dy)

        if (distance <= 55f) {
            return index
        }
    }

    return null
}