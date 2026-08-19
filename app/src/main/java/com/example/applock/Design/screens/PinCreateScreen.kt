package com.example.applock.Design.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.applock.Design.components.NumberPad
import com.example.applock.data.DataStoreManager
import com.example.applock.service.BiometricHelper
import kotlinx.coroutines.launch

@Composable
fun PinCreateScreen(
    onNext: (String) -> Unit
) {

    // ----------------------------------------
    // PIN STATE
    // ----------------------------------------

    var pin by remember {
        mutableStateOf("")
    }

    var pinLength by remember {
        mutableStateOf(6)
    }

    // ----------------------------------------
    // DROPDOWN STATE
    // ----------------------------------------

    var expanded by remember {
        mutableStateOf(false)
    }

    // ----------------------------------------
    // FINGERPRINT STATE
    // ----------------------------------------

    var fingerprintEnabled by remember {
        mutableStateOf(false)
    }

    val context =
        androidx.compose.ui.platform.LocalContext.current

    val scope =
        rememberCoroutineScope()

    val dataStore =
        remember {
            DataStoreManager(context)
        }

    // ----------------------------------------
    // COLORS
    // ----------------------------------------

    val backgroundColor =
        Color(0xFF29A0F0)

    val numberButtonColor =
        Color(0xFF69B9F3)

    // ----------------------------------------
    // MAIN SCREEN
    // ----------------------------------------

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.Center
        ) {

            Spacer(
                modifier = Modifier.height(35.dp)
            )

            // --------------------------------
            // TITLE
            // --------------------------------

            Text(
                text = "Set passcode",
                color = Color.White,
                fontSize = 25.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            // --------------------------------
            // STEP INDICATOR
            // --------------------------------

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(
                            color = Color.White,
                            shape = CircleShape
                        ),
                    contentAlignment =
                        Alignment.Center
                ) {

                    Text(
                        text = "1",
                        color = backgroundColor,
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .width(95.dp)
                        .height(2.dp)
                        .background(Color.White)
                )

                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .border(
                            width = 2.dp,
                            color = Color.White,
                            shape = CircleShape
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
                modifier = Modifier.height(28.dp)
            )

            // --------------------------------
            // PIN LENGTH DROPDOWN
            // --------------------------------

            Box {

                Row(
                    modifier = Modifier
                        .width(170.dp)
                        .height(35.dp)
                        .border(
                            width = 2.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            expanded = true
                        }
                        .padding(horizontal = 10.dp),

                    verticalAlignment =
                        Alignment.CenterVertically,

                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    Text(
                        text = "$pinLength - Digit pin",
                        color = Color.White,
                        fontSize = 15.sp
                    )

                    Icon(
                        imageVector =
                            Icons.Default.KeyboardArrowDown,

                        contentDescription =
                            "Select PIN length",

                        tint = Color.White
                    )
                }

                // --------------------------------
                // DROPDOWN MENU
                // --------------------------------

                DropdownMenu(
                    expanded = expanded,

                    onDismissRequest = {
                        expanded = false
                    }
                ) {

                    // --------------------------------
                    // 4 DIGIT
                    // --------------------------------

                    DropdownMenuItem(

                        text = {
                            Text("4 - Digit pin")
                        },

                        onClick = {

                            pinLength = 4

                            pin = ""

                            expanded = false
                        }
                    )

                    // --------------------------------
                    // 6 DIGIT
                    // --------------------------------

                    DropdownMenuItem(

                        text = {
                            Text("6 - Digit pin")
                        },

                        onClick = {

                            pinLength = 6

                            pin = ""

                            expanded = false
                        }
                    )

                    // --------------------------------
                    // FINGERPRINT
                    // --------------------------------

                    DropdownMenuItem(

                        text = {

                            Row(
                                modifier =
                                    Modifier.fillMaxWidth(),

                                verticalAlignment =
                                    Alignment.CenterVertically,

                                horizontalArrangement =
                                    Arrangement.SpaceBetween
                            ) {

                                Text(
                                    text = "Fingerprint"
                                )

                                Switch(
                                    checked =
                                        fingerprintEnabled,

                                    onCheckedChange = { enabled ->

                                        if (enabled) {

                                            // Check device biometric
                                            if (
                                                BiometricHelper
                                                    .isBiometricAvailable(
                                                        context
                                                    )
                                            ) {

                                                fingerprintEnabled =
                                                    true

                                                scope.launch {

                                                    dataStore
                                                        .saveFingerprintEnabled(
                                                            true
                                                        )
                                                }

                                            } else {

                                                fingerprintEnabled =
                                                    false

                                                scope.launch {

                                                    dataStore
                                                        .saveFingerprintEnabled(
                                                            false
                                                        )
                                                }
                                            }

                                        } else {

                                            fingerprintEnabled =
                                                false

                                            scope.launch {

                                                dataStore
                                                    .saveFingerprintEnabled(
                                                        false
                                                    )
                                            }
                                        }
                                    }
                                )
                            }
                        },

                        onClick = {

                            val enabled =
                                !fingerprintEnabled

                            if (enabled) {

                                if (
                                    BiometricHelper
                                        .isBiometricAvailable(
                                            context
                                        )
                                ) {

                                    fingerprintEnabled =
                                        true

                                    scope.launch {

                                        dataStore
                                            .saveFingerprintEnabled(
                                                true
                                            )
                                    }

                                }

                            } else {

                                fingerprintEnabled =
                                    false

                                scope.launch {

                                    dataStore
                                        .saveFingerprintEnabled(
                                            false
                                        )
                                }
                            }
                        }
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(40.dp)
            )

            // --------------------------------
            // DESCRIPTION
            // --------------------------------

            Text(
                text = "Create $pinLength - Digit pin",
                color = Color.White,
                fontSize = 16.sp
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            // --------------------------------
            // PIN DOTS
            // --------------------------------

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(14.dp)
            ) {

                repeat(pinLength) { index ->

                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(
                                color =
                                    if (
                                        index < pin.length
                                    ) {
                                        Color.White
                                    } else {
                                        Color.White.copy(
                                            alpha = 0.45f
                                        )
                                    },

                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(60.dp)
            )

            // --------------------------------
            // NUMBER PAD
            // --------------------------------

            NumberPad(

                onNumberClick = { number ->

                    val newPin =
                        pin + number

                    if (
                        newPin.length <= pinLength
                    ) {

                        pin = newPin

                        if (
                            newPin.length == pinLength
                        ) {

                            onNext(newPin)
                        }
                    }
                },

                onDelete = {

                    if (pin.isNotEmpty()) {

                        pin =
                            pin.dropLast(1)
                    }
                },

                buttonColor =
                    numberButtonColor
            )
        }
    }
}