
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

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        /*
         * ====================================================
         * CONFIGURE ADMOB TEST DEVICE
         * ====================================================
         */

        try {

            val requestConfiguration =
                RequestConfiguration.Builder()
                    .setTestDeviceIds(
                        listOf(
                            TEST_DEVICE_ID
                        )
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

        /*
         * ====================================================
         * INITIALIZE GOOGLE MOBILE ADS SDK
         * ====================================================
         */

        Log.d(
            TAG,
            "Initializing Google Mobile Ads SDK..."
        )

        MobileAds.initialize(this) {

            Log.d(
                TAG,
                "AdMob initialized successfully"
            )

            Log.d(
                TAG,
                "================================"
            )
        }

        /*
         * ====================================================
         * OPEN RESET PASSWORD
         * ====================================================
         */

        val openResetPassword =
            intent.getBooleanExtra(
                "openResetPassword",
                false
            )

        /*
         * ====================================================
         * COMPOSE / NAVIGATION
         * ====================================================
         */

        setContent {

            NavGraph(
                context = this,

                startDestination =
                    if (openResetPassword) {
                        "resetCreate"
                    } else {
                        "startScreen"
                    }
            )
        }
    }

    /*
     * ========================================================
     * NEW INTENT
     * ========================================================
     */

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

        setContent {

            NavGraph(
                context = this,

                startDestination =
                    if (openResetPassword) {
                        "resetCreate"
                    } else {
                        "unlockScreen"
                    }
            )
        }
    }
}

