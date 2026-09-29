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

private const val PREFS_NAME =
    "applock_session"

private const val KEY_REQUIRE_UNLOCK =
    "require_unlock_on_next_open"

class MainActivity : AppCompatActivity() {

    private var appWasStopped = false

    override fun onCreate(savedInstanceState: Bundle?) {
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
         * INITIALIZE ADMOB
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
        }

        /*
         * ====================================================
         * RESET PASSWORD
         * ====================================================
         */

        val openResetPassword =
            intent.getBooleanExtra(
                "openResetPassword",
                false
            )

        /*
         * ====================================================
         * CHECK IF APP MUST SHOW UNLOCK SCREEN
         * ====================================================
         */

        val preferences =
            getSharedPreferences(
                PREFS_NAME,
                MODE_PRIVATE
            )

        val requireUnlock =
            preferences.getBoolean(
                KEY_REQUIRE_UNLOCK,
                false
            )

        /*
         * ====================================================
         * START DESTINATION
         * ====================================================
         */

        val startDestination =

            when {

                openResetPassword -> {
                    "resetCreate"
                }

                requireUnlock -> {
                    "unlockScreen"
                }

                else -> {
                    "startScreen"
                }
            }

        /*
         * ====================================================
         * CLEAR PENDING UNLOCK
         *
         * It is cleared here because UnlockScreen will now
         * be shown during this app launch.
         * ====================================================
         */

        if (requireUnlock) {

            preferences
                .edit()
                .putBoolean(
                    KEY_REQUIRE_UNLOCK,
                    false
                )
                .apply()
        }

        /*
         * ====================================================
         * COMPOSE
         * ====================================================
         */

        setContent {

            NavGraph(
                context = this,
                startDestination = startDestination
            )
        }
    }

    /*
     * ========================================================
     * APP GOES INTO BACKGROUND
     * ========================================================
     *
     * When user leaves the app, mark that the next app
     * opening should require UnlockScreen.
     *
     * ========================================================
     */

    override fun onStop() {
        super.onStop()

        if (!isChangingConfigurations) {

            val preferences =
                getSharedPreferences(
                    PREFS_NAME,
                    MODE_PRIVATE
                )

            preferences
                .edit()
                .putBoolean(
                    KEY_REQUIRE_UNLOCK,
                    true
                )
                .apply()

            Log.d(
                TAG,
                "App stopped - unlock required on next open"
            )

            appWasStopped = true
        }
    }

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

        if (openResetPassword) {

            setContent {

                NavGraph(
                    context = this,
                    startDestination = "resetCreate"
                )
            }
        }
    }
}