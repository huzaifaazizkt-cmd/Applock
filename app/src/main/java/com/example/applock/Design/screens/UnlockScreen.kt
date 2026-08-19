package com.example.applock.Design.screens

import android.app.Activity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.applock.Design.components.NumberPad
import com.example.applock.data.DataStoreManager
import kotlinx.coroutines.flow.first

@Composable
fun UnlockScreen(
    onUnlockSuccess: () -> Unit
) {

    val context =
        LocalContext.current

    val dataStore =
        remember {
            DataStoreManager(context)
        }

    // --------------------------------
    // ENTERED PIN
    // --------------------------------

    var enteredPin by remember {
        mutableStateOf("")
    }

    // --------------------------------
    // SAVED PIN
    // --------------------------------

    var savedPin by remember {
        mutableStateOf("")
    }

    // --------------------------------
    // ERROR
    // --------------------------------

    var error by remember {
        mutableStateOf("")
    }

    // --------------------------------
    // FINGERPRINT ENABLED
    // --------------------------------

    var fingerprintEnabled by remember {
        mutableStateOf(false)
    }

    // --------------------------------
    // LOAD DATA
    // --------------------------------

    LaunchedEffect(Unit) {

        savedPin =
            dataStore
                .getPin()
                .first()
                .orEmpty()

        fingerprintEnabled =
            dataStore
                .getFingerprintEnabled()
                .first()
    }

    // --------------------------------
    // PIN LENGTH
    // --------------------------------

    val pinLength =
        if (savedPin.isNotEmpty()) {
            savedPin.length
        } else {
            6
        }

    // --------------------------------
    // CHECK PIN
    // --------------------------------

    fun checkPin() {

        if (savedPin.isEmpty()) {
            return
        }

        if (enteredPin == savedPin) {

            error = ""

            onUnlockSuccess()

        } else {

            error =
                "Enter your correct password"

            enteredPin = ""
        }
    }

    // --------------------------------
    // BIOMETRIC
    // --------------------------------

    fun showBiometric() {

        val activity =
            context as? FragmentActivity
                ?: return

        val biometricManager =
            BiometricManager.from(context)

        val result =
            biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.BIOMETRIC_WEAK
            )

        if (
            result !=
            BiometricManager.BIOMETRIC_SUCCESS
        ) {

            error =
                "Fingerprint is not available"

            return
        }

        val executor =
            activity.mainExecutor

        val biometricPrompt =
            BiometricPrompt(
                activity,
                executor,

                object :
                    BiometricPrompt.AuthenticationCallback() {

                    override fun onAuthenticationSucceeded(
                        result:
                        BiometricPrompt.AuthenticationResult
                    ) {

                        super.onAuthenticationSucceeded(
                            result
                        )

                        error = ""

                        onUnlockSuccess()
                    }

                    override fun onAuthenticationFailed() {

                        super.onAuthenticationFailed()

                        error =
                            "Fingerprint not recognized"
                    }

                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence
                    ) {

                        super.onAuthenticationError(
                            errorCode,
                            errString
                        )

                        // User ne "Use PIN"
                        // ya cancel press kiya
                        error = ""
                    }
                }
            )

        val promptInfo =
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("AppLock")
                .setSubtitle("Scan your fingerprint")
                .setDescription(
                    "Use your fingerprint to unlock this app"
                )
                .setNegativeButtonText("Use PIN")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                            BiometricManager.Authenticators.BIOMETRIC_WEAK
                )
                .build()

        biometricPrompt.authenticate(
            promptInfo
        )
    }

    // --------------------------------
    // MAIN SCREEN
    // --------------------------------

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Color(0xFF29A0F0)
                )
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 30.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.Center
        ) {

            Spacer(
                modifier =
                    Modifier.height(35.dp)
            )

            // --------------------------------
            // TITLE
            // --------------------------------

            Text(
                text = "Enter passcode",
                color = Color.White,
                fontSize = 25.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(
                modifier =
                    Modifier.height(28.dp)
            )

            // --------------------------------
            // FINGERPRINT BUTTON
            // --------------------------------

            if (fingerprintEnabled) {

                Box(
                    modifier =
                        Modifier
                            .size(65.dp)
                            .background(
                                Color(0xFF69B9F3),
                                CircleShape
                            )
                            .clickable {

                                showBiometric()
                            },

                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Fingerprint,

                        contentDescription =
                            "Fingerprint",

                        tint =
                            Color.White,

                        modifier =
                            Modifier.size(40.dp)
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(18.dp)
                )
            }

            // --------------------------------
            // DESCRIPTION
            // --------------------------------

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

            // --------------------------------
            // PIN DOTS
            // --------------------------------

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(14.dp)
            ) {

                repeat(pinLength) { index ->

                    Box(
                        modifier =
                            Modifier
                                .size(14.dp)
                                .background(
                                    color =
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

                                    shape =
                                        CircleShape
                                )
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )

            // --------------------------------
            // ERROR
            // --------------------------------

            if (error.isNotEmpty()) {

                Text(
                    text = error,

                    color = Color.White,

                    fontSize = 14.sp,

                    fontWeight =
                        FontWeight.Medium
                )
            }

            Spacer(
                modifier =
                    Modifier.height(35.dp)
            )

            // --------------------------------
            // NUMBER PAD
            // --------------------------------

            NumberPad(

                onNumberClick = { number ->

                    if (
                        enteredPin.length <
                        pinLength
                    ) {

                        enteredPin += number

                        error = ""

                        if (
                            enteredPin.length ==
                            pinLength
                        ) {

                            checkPin()
                        }
                    }
                },

                onDelete = {

                    if (
                        enteredPin.isNotEmpty()
                    ) {

                        enteredPin =
                            enteredPin.dropLast(1)

                        error = ""
                    }
                },

                buttonColor =
                    Color(0xFF69B9F3)
            )
        }
    }
}