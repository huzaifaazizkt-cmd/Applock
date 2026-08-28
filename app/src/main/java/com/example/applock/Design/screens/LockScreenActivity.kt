package com.example.applock.Design.screens

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import com.example.applock.service.AppLockServiceHolder

class LockScreenActivity : FragmentActivity() {

    private val TAG =
        "APPLOCK_DEBUG"

    private var targetPackage: String? =
        null

    // =========================================================
    // CREATE
    // =========================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        Log.d(
            TAG,
            "================================"
        )

        Log.d(
            TAG,
            "LOCK SCREEN CREATED"
        )

        Log.d(
            TAG,
            "================================"
        )

        // =====================================================
        // SHOW WHEN LOCKED
        // =====================================================

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O_MR1
        ) {

            setShowWhenLocked(
                true
            )

            setTurnScreenOn(
                true
            )
        }

        window.addFlags(

            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        // =====================================================
        // TARGET APP
        // =====================================================

        targetPackage =
            intent.getStringExtra(
                "packageName"
            )

        Log.d(
            TAG,
            "TARGET APP = $targetPackage"
        )

        if (
            targetPackage.isNullOrEmpty()
        ) {

            Log.e(
                TAG,
                "TARGET PACKAGE IS NULL"
            )

            goHome()

            return
        }

        // =====================================================
        // LOCK SCREEN OPEN
        // =====================================================

        AppLockServiceHolder.isLockScreenOpen =
            true

        // =====================================================
        // BACK = HOME
        // =====================================================

        onBackPressedDispatcher.addCallback(

            this,

            object :
                OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {

                    Log.d(
                        TAG,
                        "BACK PRESSED ON LOCK SCREEN"
                    )

                    goHome()
                }
            }
        )

        // =====================================================
        // UNLOCK SCREEN
        // =====================================================

        setContent {

            UnlockScreen(

                onUnlockSuccess = {

                    val packageName =
                        targetPackage

                    Log.d(
                        TAG,
                        "================================"
                    )

                    Log.d(
                        TAG,
                        "UNLOCK SUCCESS"
                    )

                    Log.d(
                        TAG,
                        "PACKAGE = $packageName"
                    )

                    Log.d(
                        TAG,
                        "================================"
                    )

                    if (
                        packageName.isNullOrEmpty()
                    ) {

                        goHome()

                        return@UnlockScreen
                    }

                    // =================================================
                    // SAVE UNLOCKED APP
                    // =================================================

                    AppLockServiceHolder.currentUnlockedApp =
                        packageName

                    AppLockServiceHolder.lastUnlockTime =
                        System.currentTimeMillis()

                    // =================================================
                    // CLOSE LOCK SCREEN STATE
                    // =================================================

                    AppLockServiceHolder.isLockScreenOpen =
                        false

                    // =================================================
                    // OPEN TARGET APP
                    // =================================================

                    openApp()
                }
            )
        }
    }

    // =========================================================
    // NEW INTENT
    // =========================================================

    override fun onNewIntent(
        intent: Intent
    ) {

        super.onNewIntent(
            intent
        )

        setIntent(
            intent
        )

        targetPackage =
            intent.getStringExtra(
                "packageName"
            )

        Log.d(
            TAG,
            "NEW TARGET PACKAGE = $targetPackage"
        )

        AppLockServiceHolder.isLockScreenOpen =
            true
    }

    // =========================================================
    // GO HOME
    // =========================================================

    private fun goHome() {

        Log.d(
            TAG,
            "GO HOME"
        )

        AppLockServiceHolder.clear()

        try {

            val homeIntent =
                Intent(
                    Intent.ACTION_MAIN
                ).apply {

                    addCategory(
                        Intent.CATEGORY_HOME
                    )

                    addFlags(

                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    )
                }

            startActivity(
                homeIntent
            )

        } catch (e: Exception) {

            Log.e(
                TAG,
                "HOME OPEN ERROR",
                e
            )
        }

        try {

            finishAffinity()

        } catch (e: Exception) {

            Log.e(
                TAG,
                "FINISH AFFINITY ERROR",
                e
            )
        }

        finishAndRemoveTask()
    }

    // =========================================================
    // OPEN TARGET APP
    // =========================================================

    private fun openApp() {

        val packageName =
            targetPackage

        if (
            packageName.isNullOrEmpty()
        ) {

            Log.e(
                TAG,
                "TARGET PACKAGE NULL"
            )

            goHome()

            return
        }

        try {

            Log.d(
                TAG,
                "OPENING APP = $packageName"
            )

            val appIntent =
                packageManager
                    .getLaunchIntentForPackage(
                        packageName
                    )

            if (
                appIntent == null
            ) {

                Log.e(
                    TAG,
                    "LAUNCH INTENT NULL = $packageName"
                )

                goHome()

                return
            }

            appIntent.addFlags(

                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            )

            startActivity(
                appIntent
            )

            Log.d(
                TAG,
                "TARGET APP STARTED = $packageName"
            )

        } catch (e: Exception) {

            Log.e(
                TAG,
                "OPEN APP ERROR",
                e
            )

            goHome()

            return
        }

        try {

            finishAffinity()

        } catch (e: Exception) {

            Log.e(
                TAG,
                "FINISH AFFINITY ERROR",
                e
            )
        }

        finishAndRemoveTask()
    }

    // =========================================================
    // DESTROY
    // =========================================================

    override fun onDestroy() {

        Log.d(
            TAG,
            "LOCK SCREEN DESTROYED"
        )

        /*
         * Do not clear currentUnlockedApp here.
         *
         * It is required by AccessibilityService so that
         * the just-unlocked application is not immediately
         * locked again.
         */

        AppLockServiceHolder.isLockScreenOpen =
            false

        super.onDestroy()
    }
}