package com.example.applock.Design.screens

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.applock.service.AppLockServiceHolder

class LockScreenActivity : ComponentActivity() {

    private val TAG = "APPLOCK_DEBUG"

    private var targetPackage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        Log.d(TAG, "================================")
        Log.d(TAG, "LOCK SCREEN CREATED")
        Log.d(TAG, "================================")

        // ------------------------------------------------
        // Lock screen ko screen ke upar show karo
        // ------------------------------------------------

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {

            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        // ------------------------------------------------
        // Kis app ko unlock karna hai
        // ------------------------------------------------

        targetPackage =
            intent.getStringExtra("packageName")

        Log.d(
            TAG,
            "TARGET APP = $targetPackage"
        )

        // ------------------------------------------------
        // Lock screen open
        // ------------------------------------------------

        AppLockServiceHolder.isLockScreenOpen = true

        // ------------------------------------------------
        // Unlock Screen
        // ------------------------------------------------

        setContent {

            UnlockScreen(

                onUnlockSuccess = {

                    Log.d(
                        TAG,
                        "UNLOCK SUCCESS"
                    )

                    targetPackage?.let { packageName ->

                        // Current app save karo
                        AppLockServiceHolder.currentUnlockedApp =
                            packageName

                        // Unlock ka time save karo
                        AppLockServiceHolder.lastUnlockTime =
                            System.currentTimeMillis()

                        Log.d(
                            TAG,
                            "UNLOCKED APP = $packageName"
                        )
                    }

                    AppLockServiceHolder.isLockScreenOpen = false

                    openApp()
                }
            )
        }
    }

    // ------------------------------------------------
    // Original app open karo
    // ------------------------------------------------

    private fun openApp() {

        try {

            val packageName = targetPackage

            if (packageName == null) {

                Log.e(
                    TAG,
                    "TARGET PACKAGE NULL"
                )

                finishAndRemoveTask()
                return
            }

            Log.d(
                TAG,
                "OPENING APP = $packageName"
            )

            val appIntent =
                packageManager.getLaunchIntentForPackage(
                    packageName
                )

            if (appIntent != null) {

                /*
                 * TASK_ON_HOME ki wajah se:
                 *
                 * WhatsApp -> Back
                 *        ↓
                 * Home
                 *
                 * AppLock ki list nahi khulegi.
                 */

                appIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_TASK_ON_HOME
                )

                startActivity(appIntent)

            } else {

                Log.e(
                    TAG,
                    "LAUNCH INTENT NULL"
                )
            }

        } catch (e: Exception) {

            Log.e(
                TAG,
                "OPEN APP ERROR",
                e
            )
        }

        // LockScreen ko task se remove karo
        finishAndRemoveTask()
    }

    override fun onDestroy() {

        super.onDestroy()

        Log.d(
            TAG,
            "LOCK SCREEN DESTROYED"
        )

        AppLockServiceHolder.isLockScreenOpen = false
    }
}
    }

    override fun onDestroy() {
        super.onDestroy()
        AppLockServiceHolder.isLockScreenOpen = false
    }
}
