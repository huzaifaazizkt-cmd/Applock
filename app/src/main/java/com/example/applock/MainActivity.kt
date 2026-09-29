package com.example.applock

import android.content.Intent
import android.os.Bundle
import android.util.Log

import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity

import com.example.applock.navigation.NavGraph
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration

private const val TAG = "AppLockAdMob"

private const val TEST_DEVICE_ID =
    "E5CA263188C38AF6BD96C4C84E9600BB"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ====================================================
        // CONFIGURE ADMOB TEST DEVICE
        // ====================================================

        try {

            val requestConfiguration =
                RequestConfiguration.Builder()
                    .setTestDeviceIds(
                        listOf(TEST_DEVICE_ID)
                    )
                    .build()

            MobileAds.setRequestConfiguration(
                requestConfiguration
            )

            Log.d(
                TAG,
                "TEST DEVICE CONFIGURED"
            )

            Log.d(
                TAG,
                "Test Device ID = $TEST_DEVICE_ID"
            )

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Failed to configure AdMob test device",
                e
            )
        }

        // ====================================================
        // INITIALIZE ADMOB
        // ====================================================

        Log.d(
            TAG,
            "Initializing Google Mobile Ads SDK..."
        )

        MobileAds.initialize(this) {

            Log.d(
                TAG,
                "AdMob initialized successfully"
            )
        }

        // ====================================================
        // RESET PASSWORD
        // ====================================================

        val openResetPassword =
            intent.getBooleanExtra(
                "openResetPassword",
                false
            )

        // ====================================================
        // FIRST / FRESH ACTIVITY CREATION
        // ====================================================
        //
        // Fresh MainActivity always starts with StartScreen.
        //
        // StartScreen itself decides:
        //
        // Existing app:
        // StartScreen -> UnlockScreen
        //
        // First installation:
        // StartScreen -> WelcomeScreen
        //
        // ====================================================

        val startDestination =
            if (openResetPassword) {
                "resetCreate"
            } else {
                "startScreen"
            }

        Log.d(
            TAG,
            "================================"
        )

        Log.d(
            TAG,
            "MAIN ACTIVITY CREATED"
        )

        Log.d(
            TAG,
            "START DESTINATION = $startDestination"
        )

        Log.d(
            TAG,
            "================================"
        )

        setContent {

            NavGraph(
                context = this,
                startDestination = startDestination
            )
        }
    }

    // ========================================================
    // EXISTING ACTIVITY REOPENED
    // ========================================================
    //
    // This happens when MainActivity is still alive and the
    // user opens AppLock again.
    //
    // Do NOT show StartScreen here.
    //
    // Directly show UnlockScreen.
    //
    // ========================================================

    override fun onNewIntent(
        intent: Intent
    ) {
        super.onNewIntent(intent)

        setIntent(intent)

        val openResetPassword =
            intent.getBooleanExtra(
                "openResetPassword",
                false
            )

        Log.d(
            TAG,
            "================================"
        )

        Log.d(
            TAG,
            "MAIN ACTIVITY NEW INTENT"
        )

        Log.d(
            TAG,
            "================================"
        )

        if (openResetPassword) {

            setContent {

                NavGraph(
                    context = this,
                    startDestination = "resetCreate"
                )
            }

            return
        }

        // ====================================================
        // NORMAL REOPEN
        // ====================================================

        setContent {

            NavGraph(
                context = this,
                startDestination = "unlockScreen"
            )
        }
    }
}