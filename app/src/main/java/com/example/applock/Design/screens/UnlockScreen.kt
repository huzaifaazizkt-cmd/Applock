package com.example.applock.Design.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.applock.Design.components.NumberPad
import com.example.applock.R
import com.example.applock.data.DataStoreManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.sqrt


@Composable
fun UnlockScreen(
    onUnlockSuccess: () -> Unit,
    onIntruderCapture: (Long) -> Unit = {},
    onFingerprintRequest: () -> Unit = {}
) {

    // =========================================================
    // CONTEXT
    // =========================================================

    val context =
        LocalContext.current

    val dataStore =
        remember {
            DataStoreManager(context)
        }

    val scope =
        rememberCoroutineScope()


    // =========================================================
    // STRING RESOURCES
    // =========================================================

    val patternNotMatchText =
        stringResource(
            R.string.pattern_not_match
        )

    val correctPasswordText =
        stringResource(
            R.string.enter_correct_password
        )


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

    var fingerprintEnabled by remember {
        mutableStateOf(false)
    }

    var vibrationEnabled by remember {
        mutableStateOf(false)
    }

    var hideTrackEnabled by remember {
        mutableStateOf(false)
    }


    // =========================================================
    // LOAD SAVED AUTH + SETTINGS
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

        fingerprintEnabled =
            dataStore
                .getFingerprintEnabled()
                .first()

        vibrationEnabled =
            dataStore
                .getVibrationEnabled()
                .first()

        hideTrackEnabled =
            dataStore
                .getHideTrackEnabled()
                .first()


        // =====================================================
        // AUTOMATIC FINGERPRINT
        // =====================================================

        if (
            fingerprintEnabled
        ) {

            delay(350)

            onFingerprintRequest()
        }
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
    // REGISTER WRONG ATTEMPT
    // =========================================================

    fun registerWrongAttempt() {

        scope.launch {

            try {

                // =================================================
                // CHECK INTRUDER ENABLED
                // =================================================

                val enabled =
                    dataStore
                        .getIntruderEnabled()
                        .first()

                if (!enabled) {

                    return@launch
                }


                // =================================================
                // GET CURRENT WRONG ATTEMPTS
                // =================================================

                val currentAttempts =
                    dataStore
                        .getIntruderWrongAttempts()
                        .first()


                val newAttempts =
                    currentAttempts + 1


                android.util.Log.d(
                    "INTRUDER_DEBUG",
                    "WRONG ATTEMPT = $newAttempts / 3"
                )


                // =================================================
                // 3 WRONG ATTEMPTS
                // =================================================

                if (
                    newAttempts >= 3
                ) {

                    // -------------------------------------------------
                    // RESET COUNTER
                    // -------------------------------------------------

                    dataStore
                        .resetIntruderWrongAttempts()


                    // -------------------------------------------------
                    // GET OBSERVATION TIME
                    // -------------------------------------------------

                    val observationTime =
                        dataStore
                            .getIntruderObservationTime()
                            .first()


                    android.util.Log.d(
                        "INTRUDER_DEBUG",
                        "OBSERVATION TIME = $observationTime"
                    )


                    // =================================================
                    // OBSERVATION TIME
                    //
                    // 0  = Immediately
                    // 5  = 5 seconds
                    // 15 = 15 seconds
                    // 30 = 30 seconds
                    // -1 = Never
                    // =================================================

                    val delayMillis =
                        when (
                            observationTime
                        ) {

                            // Immediately
                            0 ->
                                0L


                            // 5 seconds
                            5 ->
                                5_000L


                            // 15 seconds
                            15 ->
                                15_000L


                            // 30 seconds
                            30 ->
                                30_000L


                            // Never
                            -1 ->
                                return@launch


                            // Other values are treated
                            // as seconds
                            else ->
                                observationTime
                                    .coerceAtLeast(0)
                                    .toLong() * 1_000L
                        }


                    android.util.Log.d(
                        "INTRUDER_DEBUG",
                        "CAPTURE DELAY = $delayMillis ms"
                    )


                    // =================================================
                    // WAIT
                    // =================================================

                    if (
                        delayMillis > 0L
                    ) {

                        delay(
                            delayMillis
                        )
                    }


                    // =================================================
                    // CAPTURE INTRUDER PHOTO
                    // =================================================

                    onIntruderCapture(
                        System.currentTimeMillis()
                    )

                } else {

                    // =================================================
                    // SAVE WRONG ATTEMPT
                    // =================================================

                    dataStore
                        .saveIntruderWrongAttempts(
                            newAttempts
                        )
                }

            } catch (
                e: Exception
            ) {

                android.util.Log.e(
                    "INTRUDER_DEBUG",
                    "WRONG ATTEMPT ERROR",
                    e
                )
            }
        }
    }


    // =========================================================
    // SHOW PATTERN ERROR
    // =========================================================

    fun showPatternError() {

        registerWrongAttempt()

        clearErrorJob?.cancel()

        patternError =
            true

        error =
            patternNotMatchText

        clearErrorJob =
            scope.launch {

                delay(
                    1500
                )

                enteredPattern =
                    emptyList()

                patternError =
                    false

                error =
                    ""

                clearErrorJob =
                    null
            }
    }


    // =========================================================
    // CHECK PIN
    // =========================================================

    fun checkPin(
        pin: String
    ) {

        if (
            pin ==
            savedPin
        ) {

            error =
                ""

            enteredPin =
                ""


            // =====================================================
            // RESET WRONG ATTEMPTS AFTER SUCCESS
            // =====================================================

            scope.launch {

                try {

                    dataStore
                        .resetIntruderWrongAttempts()

                } catch (
                    e: Exception
                ) {

                    android.util.Log.e(
                        "INTRUDER_DEBUG",
                        "RESET ERROR",
                        e
                    )
                }
            }


            onUnlockSuccess()

        } else {

            registerWrongAttempt()

            error =
                correctPasswordText

            enteredPin =
                ""
        }
    }


    // =========================================================
    // ROOT
    // =========================================================

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    backgroundColor
                )
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = 30.dp
                    ),

            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.Center
        ) {

            Text(
                text =
                    if (
                        authType ==
                        "pattern"
                    ) {

                        stringResource(
                            R.string.draw_pattern
                        )

                    } else {

                        stringResource(
                            R.string.enter_passcode
                        )
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
            // PIN
            // =================================================

            if (
                authType ==
                "pin"
            ) {

                val pinLength =
                    if (
                        savedPin.isNotEmpty()
                    ) {

                        savedPin.length

                    } else {

                        6
                    }


                Text(
                    text =
                        stringResource(
                            R.string.enter_digit_pin,
                            pinLength
                        ),

                    color =
                        Color.White,

                    fontSize =
                        16.sp
                )


                Spacer(
                    modifier =
                        Modifier.height(18.dp)
                )


                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            14.dp
                        )
                ) {

                    repeat(
                        pinLength
                    ) { index ->

                        Box(
                            modifier =
                                Modifier
                                    .size(14.dp)
                                    .background(

                                        if (
                                            index <
                                            enteredPin.length
                                        ) {

                                            Color.White

                                        } else {

                                            Color.White.copy(
                                                alpha =
                                                    0.45f
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


                if (
                    error.isNotEmpty()
                ) {

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


                NumberPad(

                    onNumberClick = {
                            number ->

                        if (
                            enteredPin.length <
                            pinLength
                        ) {

                            val newPin =
                                enteredPin +
                                        number

                            enteredPin =
                                newPin

                            error =
                                ""


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
                                enteredPin.dropLast(
                                    1
                                )

                            error =
                                ""
                        }
                    },

                    buttonColor =
                        numberButtonColor
                )
            }


            // =================================================
            // PATTERN
            // =================================================

            else {

                Box(
                    modifier =
                        Modifier
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


                UnlockPatternGrid(

                    selectedDots =
                        enteredPattern,

                    isError =
                        patternError,

                    hideTrack =
                        hideTrackEnabled,

                    vibrationEnabled =
                        vibrationEnabled,

                    onPatternChanged = {
                            dots ->

                        enteredPattern =
                            dots


                        if (
                            patternError
                        ) {

                            clearErrorJob?.cancel()

                            clearErrorJob =
                                null

                            patternError =
                                false

                            error =
                                ""
                        }
                    },


                    onPatternFinished = {
                            pattern ->

                        val originalPattern =
                            savedPattern
                                .split("-")
                                .mapNotNull {
                                    it.toIntOrNull()
                                }


                        if (
                            pattern !=
                            originalPattern
                        ) {

                            showPatternError()

                            return@UnlockPatternGrid
                        }


                        clearErrorJob?.cancel()

                        clearErrorJob =
                            null

                        patternError =
                            false

                        error =
                            ""

                        enteredPattern =
                            emptyList()


                        // =================================================
                        // RESET WRONG ATTEMPTS AFTER SUCCESS
                        // =================================================

                        scope.launch {

                            try {

                                dataStore
                                    .resetIntruderWrongAttempts()

                            } catch (
                                e: Exception
                            ) {

                                android.util.Log.e(
                                    "INTRUDER_DEBUG",
                                    "RESET ERROR",
                                    e
                                )
                            }
                        }


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
// PATTERN GRID
// =============================================================

@Composable
private fun UnlockPatternGrid(

    selectedDots:
    List<Int>,

    isError:
    Boolean,

    hideTrack:
    Boolean,

    vibrationEnabled:
    Boolean,

    onPatternChanged:
        (List<Int>) -> Unit,

    onPatternFinished:
        (List<Int>) -> Unit,

    dotColor:
    Color,

    errorColor:
    Color,

    backgroundColor:
    Color

) {

    val context =
        LocalContext.current


    val latestOnPatternChanged by
    rememberUpdatedState(
        onPatternChanged
    )

    val latestOnPatternFinished by
    rememberUpdatedState(
        onPatternFinished
    )


    Box(
        modifier =
            Modifier
                .size(300.dp)
                .pointerInput(
                    vibrationEnabled
                ) {

                    var currentDots =
                        mutableListOf<Int>()

                    var patternFinished =
                        false


                    // =================================================
                    // VIBRATION
                    // =================================================

                    fun vibrate() {

                        if (
                            !vibrationEnabled
                        ) {

                            return
                        }


                        try {

                            if (
                                Build.VERSION.SDK_INT >=
                                Build.VERSION_CODES.S
                            ) {

                                val vibratorManager =
                                    context.getSystemService(
                                        Context.VIBRATOR_MANAGER_SERVICE
                                    ) as VibratorManager


                                vibratorManager
                                    .defaultVibrator
                                    .vibrate(

                                        VibrationEffect.createOneShot(
                                            35L,
                                            VibrationEffect.DEFAULT_AMPLITUDE
                                        )
                                    )

                            } else {

                                @Suppress(
                                    "DEPRECATION"
                                )

                                val vibrator =
                                    context.getSystemService(
                                        Context.VIBRATOR_SERVICE
                                    ) as Vibrator


                                if (
                                    Build.VERSION.SDK_INT >=
                                    Build.VERSION_CODES.O
                                ) {

                                    vibrator.vibrate(

                                        VibrationEffect.createOneShot(
                                            35L,
                                            VibrationEffect.DEFAULT_AMPLITUDE
                                        )
                                    )

                                } else {

                                    @Suppress(
                                        "DEPRECATION"
                                    )

                                    vibrator.vibrate(
                                        35L
                                    )
                                }
                            }

                        } catch (
                            e: Exception
                        ) {

                            android.util.Log.e(
                                "PATTERN_VIBRATION",
                                "VIBRATION ERROR",
                                e
                            )
                        }
                    }


                    // =================================================
                    // DRAG GESTURES
                    // =================================================

                    detectDragGestures(

                        // =================================================
                        // DRAG START
                        // =================================================

                        onDragStart = {
                                offset ->

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

                                vibrate()

                                latestOnPatternChanged(
                                    currentDots.toList()
                                )
                            }
                        },


                        // =================================================
                        // DRAG
                        // =================================================

                        onDrag = {
                                change,
                                _ ->

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

                                vibrate()

                                latestOnPatternChanged(
                                    currentDots.toList()
                                )
                            }
                        },


                        // =================================================
                        // DRAG END
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
                        // DRAG CANCEL
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
            // TRACK / LINES
            // =====================================================

            if (
                !hideTrack &&
                selectedDots.size >= 2
            ) {

                for (
                i in 0 until
                        selectedDots.size - 1
                ) {

                    drawLine(

                        color =
                            if (
                                isError
                            ) {

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


                // Outer circle
                drawCircle(

                    color =
                        dotColor,

                    radius =
                        17.5.dp.toPx(),

                    center =
                        position
                )


                // Inner background
                drawCircle(

                    color =
                        backgroundColor,

                    radius =
                        13.5.dp.toPx(),

                    center =
                        position
                )


                // Selected dot
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
// POSITIONS
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

        Offset(
            x1,
            y1
        ),

        Offset(
            x2,
            y1
        ),

        Offset(
            x3,
            y1
        ),

        Offset(
            x1,
            y2
        ),

        Offset(
            x2,
            y2
        ),

        Offset(
            x3,
            y2
        ),

        Offset(
            x1,
            y3
        ),

        Offset(
            x2,
            y3
        ),

        Offset(
            x3,
            y3
        )
    )
}


// =============================================================
// FIND DOT
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