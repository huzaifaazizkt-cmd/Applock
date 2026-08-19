package com.example.applock.Design.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    var confirmPin by remember {
        mutableStateOf("")
    }

    var error by remember {
        mutableStateOf("")
    }

    // Create screen se aane wali PIN ki length
    // 4 ya 6 automatically
    val pinLength = pin.length

    // --------------------------------
    // COLORS
    // --------------------------------

    val backgroundColor = Color(0xFF29A0F0)
    val numberButtonColor = Color(0xFF69B9F3)

    // --------------------------------
    // CHECK CONFIRM PIN
    // --------------------------------

    fun checkConfirmPin() {

        if (confirmPin.length != pinLength) {
            return
        }

        // Correct PIN
        if (confirmPin == pin) {

            error = ""

            scope.launch {

                // PIN save
                dataStore.savePin(pin)

                // Save ke baad AppList
                // automatically open
                navController.navigate("appList") {

                    // Confirm screen ko back stack se hata do
                    popUpTo("confirm/{pin}") {
                        inclusive = true
                    }

                    launchSingleTop = true
                }
            }

        } else {

            // Wrong PIN
            error = "Enter your correct password"

            // Dobara enter karne ke liye clear
            confirmPin = ""
        }
    }

    // --------------------------------
    // MAIN SCREEN
    // --------------------------------

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
                text = "Confirm passcode",
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

                // STEP 1
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
                        text = "1",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }

                // LINE
                Box(
                    modifier = Modifier
                        .width(95.dp)
                        .height(2.dp)
                        .background(Color.White)
                )

                // STEP 2
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
                        text = "2",
                        color = backgroundColor,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(45.dp)
            )

            // --------------------------------
            // DESCRIPTION
            // --------------------------------

            Text(
                text = "Confirm $pinLength - Digit pin",
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
                                    if (index < confirmPin.length) {
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
                modifier = Modifier.height(20.dp)
            )

            // --------------------------------
            // ERROR
            // --------------------------------

            if (error.isNotEmpty()) {

                Text(
                    text = error,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            Spacer(
                modifier = Modifier.height(35.dp)
            )

            // --------------------------------
            // NUMBER PAD
            // --------------------------------

            NumberPad(

                onNumberClick = { number ->

                    if (confirmPin.length < pinLength) {

                        confirmPin += number

                        error = ""

                        // --------------------------------
                        // PIN COMPLETE
                        // Automatically check
                        // --------------------------------

                        if (confirmPin.length == pinLength) {
                            checkConfirmPin()
                        }
                    }
                },

                onDelete = {

                    if (confirmPin.isNotEmpty()) {

                        confirmPin =
                            confirmPin.dropLast(1)

                        error = ""
                    }
                },

                buttonColor = numberButtonColor
            )
        }
    }
}