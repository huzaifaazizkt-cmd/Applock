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

    val context =
        LocalContext.current

    val dataStore =
        remember {
            DataStoreManager(context)
        }

    val scope =
        rememberCoroutineScope()

    val patternNotMatchText =
        stringResource(
            R.string.pattern_not_match
        )

    val correctPasswordText =
        stringResource(
            R.string.enter_correct_password
        )

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

    var intruderObservationAttempts by remember {
        mutableStateOf(3)
    }

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

        intruderObservationAttempts =
            dataStore
                .getIntruderObservationAttempts()
                .first()

        if (
            fingerprintEnabled
        ) {

            delay(350)

            onFingerprintRequest()
        }
    }

    val backgroundColor =
        Color(0xFF29A0F0)

    val numberButtonColor =
        Color(0xFF69B9F3)

    val patternDotColor =
        Color(0xFF83CCFF)

    val errorColor =
        Color.Red

    fun registerWrongAttempt() {

        scope.launch {

            try {

                val enabled =
                    dataStore
                        .getIntruderEnabled()
                        .first()

                if (!enabled) {
                    return@launch
                }

                val selectedAttempts =
                    dataStore
                        .getIntruderObservationAttempts()
                        .first()

                intruderObservationAttempts =
                    selectedAttempts

                android.util.Log.d(
                    "INTRUDER_DEBUG",
                    "SELECTED ATTEMPTS = $selectedAttempts"
                )

                if (
                    selectedAttempts <= 0
                ) {

                    dataStore
                        .resetIntruderWrongAttempts()

                    android.util.Log.d(
                        "INTRUDER_DEBUG",
                        "INTRUDER CAPTURE DISABLED - NEVER"
                    )

                    return@launch
                }

                val currentAttempts =
                    dataStore
                        .getIntruderWrongAttempts()
                        .first()

                val newAttempts =
                    currentAttempts + 1

                android.util.Log.d(
                    "INTRUDER_DEBUG",
                    "WRONG ATTEMPT = $newAttempts / $selectedAttempts"
                )

                dataStore
                    .saveIntruderWrongAttempts(
                        newAttempts
                    )

                if (
                    newAttempts < selectedAttempts
                ) {

                    return@launch
                }

                dataStore
                    .resetIntruderWrongAttempts()

                val observationTime =
                    dataStore
                        .getIntruderObservationTime()
                        .first()

                android.util.Log.d(
                    "INTRUDER_DEBUG",
                    "OBSERVATION TIME = $observationTime seconds"
                )

                val delayMillis =
                    observationTime
                        .toLong()
                        .coerceAtLeast(0L) * 1_000L

                android.util.Log.d(
                    "INTRUDER_DEBUG",
                    "CAPTURE DELAY = $delayMillis ms"
                )

                if (
                    delayMillis > 0L
                ) {

                    delay(
                        delayMillis
                    )
                }

                val stillEnabled =
                    dataStore
                        .getIntruderEnabled()
                        .first()

                if (!stillEnabled) {

                    android.util.Log.d(
                        "INTRUDER_DEBUG",
                        "INTRUDER TURNED OFF BEFORE CAPTURE"
                    )

                    return@launch
                }

                android.util.Log.d(
                    "INTRUDER_DEBUG",
                    "STARTING INTRUDER CAPTURE"
                )

                onIntruderCapture(
                    System.currentTimeMillis()
                )

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

            } else {

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

                    detectDragGestures(

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

            positions.forEachIndexed {
                    index,
                    position ->

                val selected =
                    selectedDots.contains(
                        index
                    )

                drawCircle(

                    color =
                        dotColor,

                    radius =
                        17.5.dp.toPx(),

                    center =
                        position
                )

                drawCircle(

                    color =
                        backgroundColor,

                    radius =
                        13.5.dp.toPx(),

                    center =
                        position
                )

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