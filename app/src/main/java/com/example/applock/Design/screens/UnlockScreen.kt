package com.example.applock.Design.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.applock.Design.components.NumberPad
import com.example.applock.data.DataStoreManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.sqrt


@Composable
fun UnlockScreen(
    onUnlockSuccess: () -> Unit
) {

    val context = LocalContext.current

    val dataStore = remember {
        DataStoreManager(context)
    }

    val scope = rememberCoroutineScope()


    // =========================================================
    // STATES
    // =========================================================

    var authType by remember {
        mutableStateOf("pin")
    }

    var savedPin by remember {
        mutableStateOf("")
    }

    var savedPattern by remember {
        mutableStateOf("")
    }

    var enteredPin by remember {
        mutableStateOf("")
    }

    var enteredPattern by remember {
        mutableStateOf<List<Int>>(emptyList())
    }

    var error by remember {
        mutableStateOf("")
    }

    var patternError by remember {
        mutableStateOf(false)
    }

    var clearErrorJob by remember {
        mutableStateOf<Job?>(null)
    }


    // =========================================================
    // LOAD SAVED AUTH
    // =========================================================

    LaunchedEffect(Unit) {

        authType =
            dataStore
                .getAuthType()
                .first()
                .orEmpty()
                .lowercase()

        savedPin =
            dataStore
                .getPin()
                .first()
                .orEmpty()

        savedPattern =
            dataStore
                .getPattern()
                .first()
                .orEmpty()
    }


    // =========================================================
    // COLORS
    // =========================================================

    val backgroundColor =
        Color(0xFF29A0F0)

    val numberButtonColor =
        Color(0xFF69B9F3)

    val patternDotColor =
        Color(0xFF83CCFF)

    val errorColor =
        Color.Red


    // =========================================================
    // CLEAR PATTERN ERROR
    // =========================================================

    fun clearPatternError() {

        clearErrorJob?.cancel()

        clearErrorJob = null

        patternError = false

        error = ""

        enteredPattern = emptyList()
    }


    // =========================================================
    // SHOW PATTERN ERROR
    // =========================================================

    fun showPatternError() {

        // Cancel previous timer
        clearErrorJob?.cancel()

        // Show red error state
        patternError = true

        error = "Pattern does not match"


        clearErrorJob = scope.launch {

            // Error stays visible for exactly 1.5 seconds
            delay(1500)


            // Return everything to normal
            enteredPattern = emptyList()

            patternError = false

            error = ""

            clearErrorJob = null
        }
    }


    // =========================================================
    // CHECK PIN
    // =========================================================

    fun checkPin(
        pin: String
    ) {

        if (pin == savedPin) {

            error = ""

            enteredPin = ""

            onUnlockSuccess()

        } else {

            error =
                "Enter your correct password"

            enteredPin = ""
        }
    }


    // =========================================================
    // ROOT
    // =========================================================

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {


        // =====================================================
        // MAIN COLUMN
        // =====================================================

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.Center
        ) {


            // =================================================
            // MAIN HEADING
            // =================================================

            Text(
                text =
                    if (authType == "pattern") {
                        "Draw pattern"
                    } else {
                        "Enter passcode"
                    },

                color =
                    Color.White,

                fontSize =
                    25.sp,

                fontWeight =
                    FontWeight.Normal
            )


            Spacer(
                modifier =
                    Modifier.height(15.dp)
            )


            // =================================================
            // PIN MODE
            // =================================================

            if (authType == "pin") {

                val pinLength =
                    if (savedPin.isNotEmpty()) {
                        savedPin.length
                    } else {
                        6
                    }


                // ---------------------------------------------
                // PIN HEADING
                // ---------------------------------------------

                Text(
                    text =
                        "Enter $pinLength - Digit pin",

                    color =
                        Color.White,

                    fontSize =
                        16.sp
                )


                Spacer(
                    modifier =
                        Modifier.height(18.dp)
                )


                // ---------------------------------------------
                // PIN DOTS
                // ---------------------------------------------

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(14.dp)
                ) {

                    repeat(pinLength) { index ->

                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(

                                    if (
                                        index <
                                        enteredPin.length
                                    ) {
                                        Color.White
                                    } else {
                                        Color.White.copy(
                                            alpha = 0.45f
                                        )
                                    },

                                    CircleShape
                                )
                        )
                    }
                }


                Spacer(
                    modifier =
                        Modifier.height(25.dp)
                )


                // ---------------------------------------------
                // PIN ERROR
                // ---------------------------------------------

                if (error.isNotEmpty()) {

                    Text(
                        text =
                            error,

                        color =
                            Color.White,

                        fontSize =
                            14.sp
                    )
                }


                Spacer(
                    modifier =
                        Modifier.height(35.dp)
                )


                // ---------------------------------------------
                // NUMBER PAD
                // ---------------------------------------------

                NumberPad(

                    onNumberClick = { number ->

                        if (
                            enteredPin.length <
                            pinLength
                        ) {

                            val newPin =
                                enteredPin + number

                            enteredPin =
                                newPin

                            error =
                                ""


                            // AUTO CHECK
                            // No Continue button.

                            if (
                                newPin.length ==
                                pinLength
                            ) {

                                checkPin(
                                    newPin
                                )
                            }
                        }
                    },


                    onDelete = {

                        if (
                            enteredPin.isNotEmpty()
                        ) {

                            enteredPin =
                                enteredPin.dropLast(1)

                            error =
                                ""
                        }
                    },


                    buttonColor =
                        numberButtonColor
                )
            }



            else {


                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .fillMaxWidth(),

                    contentAlignment =
                        Alignment.Center
                ) {

                    if (
                        patternError &&
                        error.isNotEmpty()
                    ) {

                        Text(
                            text =
                                error,

                            color =
                                Color.Red,

                            fontSize =
                                20.sp
                        )
                    }
                }


                // =================================================
                // PATTERN GRID
                // =================================================

                UnlockPatternGrid(

                    selectedDots =
                        enteredPattern,

                    isError =
                        patternError,


                    onPatternChanged = { dots ->

                        enteredPattern =
                            dots


                        // ---------------------------------
                        // NEW PATTERN STARTED
                        // ---------------------------------

                        if (patternError) {

                            clearErrorJob?.cancel()

                            clearErrorJob =
                                null

                            patternError =
                                false

                            error =
                                ""
                        }
                    },


                    onPatternFinished = { pattern ->

                        // ---------------------------------
                        // SAVED PATTERN
                        // ---------------------------------

                        val originalPattern =
                            savedPattern
                                .split("-")
                                .mapNotNull {
                                    it.toIntOrNull()
                                }


                        // ---------------------------------
                        // WRONG PATTERN
                        // ---------------------------------

                        if (
                            pattern !=
                            originalPattern
                        ) {

                            showPatternError()

                            return@UnlockPatternGrid
                        }


                        // ---------------------------------
                        // CORRECT PATTERN
                        // ---------------------------------

                        clearErrorJob?.cancel()

                        clearErrorJob =
                            null

                        patternError =
                            false

                        error =
                            ""

                        enteredPattern =
                            emptyList()


                        // ---------------------------------
                        // AUTO UNLOCK
                        // ---------------------------------

                        onUnlockSuccess()
                    },


                    dotColor =
                        patternDotColor,

                    errorColor =
                        errorColor,

                    backgroundColor =
                        backgroundColor
                )
            }
        }
    }
}


// =============================================================
// UNLOCK PATTERN GRID
// =============================================================

@Composable
private fun UnlockPatternGrid(

    selectedDots: List<Int>,

    isError: Boolean,

    onPatternChanged:
        (List<Int>) -> Unit,

    onPatternFinished:
        (List<Int>) -> Unit,

    dotColor: Color,

    errorColor: Color,

    backgroundColor: Color

) {

    val latestOnPatternChanged by
    rememberUpdatedState(
        onPatternChanged
    )

    val latestOnPatternFinished by
    rememberUpdatedState(
        onPatternFinished
    )


    Box(
        modifier = Modifier
            .size(300.dp)

            .pointerInput(Unit) {

                var currentDots =
                    mutableListOf<Int>()

                var patternFinished =
                    false


                detectDragGestures(

                    // =================================================
                    // START DRAWING
                    // =================================================

                    onDragStart = { offset ->

                        currentDots =
                            mutableListOf()

                        patternFinished =
                            false


                        val dot =
                            findUnlockDot(

                                touch =
                                    offset,

                                width =
                                    size.width.toFloat(),

                                height =
                                    size.height.toFloat()
                            )


                        if (
                            dot != null
                        ) {

                            currentDots.add(
                                dot
                            )

                            latestOnPatternChanged(
                                currentDots.toList()
                            )
                        }
                    },


                    // =================================================
                    // DRAG
                    // =================================================

                    onDrag = { change, _ ->

                        change.consume()


                        val dot =
                            findUnlockDot(

                                touch =
                                    change.position,

                                width =
                                    size.width.toFloat(),

                                height =
                                    size.height.toFloat()
                            )


                        if (
                            dot != null &&
                            !currentDots.contains(
                                dot
                            )
                        ) {

                            currentDots.add(
                                dot
                            )

                            latestOnPatternChanged(
                                currentDots.toList()
                            )
                        }
                    },


                    // =================================================
                    // END
                    // =================================================

                    onDragEnd = {

                        if (
                            !patternFinished
                        ) {

                            patternFinished =
                                true


                            val finalPattern =
                                currentDots.toList()


                            if (
                                finalPattern.isNotEmpty()
                            ) {

                                latestOnPatternFinished(
                                    finalPattern
                                )
                            }
                        }
                    },


                    // =================================================
                    // CANCEL
                    // =================================================

                    onDragCancel = {

                        if (
                            !patternFinished
                        ) {

                            patternFinished =
                                true


                            val finalPattern =
                                currentDots.toList()


                            if (
                                finalPattern.isNotEmpty()
                            ) {

                                latestOnPatternFinished(
                                    finalPattern
                                )
                            }
                        }
                    }
                )
            }
    ) {


        // =========================================================
        // CANVAS
        // =========================================================

        Canvas(
            modifier =
                Modifier.fillMaxSize()
        ) {

            val positions =
                getUnlockPositions(

                    width =
                        size.width,

                    height =
                        size.height
                )


            // =====================================================
            // CONNECTING LINES
            // =====================================================

            if (
                selectedDots.size >= 2
            ) {

                for (
                i in 0 until
                        selectedDots.size - 1
                ) {

                    drawLine(

                        color =
                            if (isError) {
                                errorColor
                            } else {
                                Color.White
                            },

                        start =
                            positions[
                                selectedDots[i]
                            ],

                        end =
                            positions[
                                selectedDots[i + 1]
                            ],

                        strokeWidth =
                            8f
                    )
                }
            }


            // =====================================================
            // DOTS
            // =====================================================

            positions.forEachIndexed {
                    index,
                    position ->

                val selected =
                    selectedDots.contains(
                        index
                    )


                // =================================================
                // OUTER CIRCLE
                // =================================================

                drawCircle(

                    color =
                        dotColor,

                    radius =
                        17.5.dp.toPx(),

                    center =
                        position
                )


                // =================================================
                // GAP
                // =================================================

                drawCircle(

                    color =
                        backgroundColor,

                    radius =
                        13.5.dp.toPx(),

                    center =
                        position
                )


                // =================================================
                // INNER DOT
                // =================================================

                drawCircle(

                    color =
                        when {

                            selected &&
                                    isError ->

                                errorColor


                            selected ->

                                Color.White


                            else ->

                                dotColor
                        },

                    radius =
                        9.dp.toPx(),

                    center =
                        position
                )
            }
        }
    }
}


// =============================================================
// DOT POSITIONS
// EXACT SAME POSITION AS CONFIRM SCREEN
// =============================================================

private fun getUnlockPositions(
    width: Float,
    height: Float
): List<Offset> {

    val x1 =
        width * 0.1667f

    val x2 =
        width * 0.5f

    val x3 =
        width * 0.8333f


    val y1 =
        height * 0.1667f

    val y2 =
        height * 0.5f

    val y3 =
        height * 0.8333f


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


// =============================================================
// FIND DOT
// SAME TOUCH RANGE AS CONFIRM SCREEN
// =============================================================

private fun findUnlockDot(
    touch: Offset,
    width: Float,
    height: Float
): Int? {

    val positions =
        getUnlockPositions(

            width =
                width,

            height =
                height
        )


    positions.forEachIndexed {
            index,
            dot ->

        val dx =
            touch.x - dot.x

        val dy =
            touch.y - dot.y

        val distance =
            sqrt(
                dx * dx +
                        dy * dy
            )


        if (
            distance <= 55f
        ) {

            return index
        }
    }


    return null
}