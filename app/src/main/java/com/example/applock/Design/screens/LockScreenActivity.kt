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

            setShowWhenLocked(true)

            setTurnScreenOn(true)
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


        // =====================================================
        // LOCK SCREEN OPEN
        // =====================================================

        AppLockServiceHolder
            .isLockScreenOpen =
            true


        // =====================================================
        // BACK = HOME
        // =====================================================

        onBackPressedDispatcher.addCallback(

            this,

            object : OnBackPressedCallback(true) {

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
                        "================================"
                    )


                    val packageName =
                        targetPackage


                    if (
                        !packageName.isNullOrEmpty()
                    ) {

                        // -----------------------------------------
                        // Save unlocked app
                        // -----------------------------------------

                        AppLockServiceHolder
                            .currentUnlockedApp =
                            packageName


                        AppLockServiceHolder
                            .lastUnlockTime =
                            System.currentTimeMillis()


                        Log.d(
                            TAG,
                            "UNLOCKED APP = $packageName"
                        )
                    }


                    // -----------------------------------------
                    // Lock screen no longer active
                    // -----------------------------------------

                    AppLockServiceHolder
                        .isLockScreenOpen =
                        false


                    // -----------------------------------------
                    // Open target
                    // -----------------------------------------

                    openApp()
                }
            )
        }
    }


    // =========================================================
    // GO HOME
    // =========================================================

    private fun goHome() {

        Log.d(
            TAG,
            "================================"
        )

        Log.d(
            TAG,
            "GO HOME"
        )

        Log.d(
            TAG,
            "================================"
        )


        // -----------------------------------------------------
        // Clear state
        // -----------------------------------------------------

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


            Log.d(
                TAG,
                "ANDROID HOME STARTED"
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


        // -----------------------------------------------------
        // Close LockScreenActivity
        // -----------------------------------------------------

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


        super.onDestroy()
    }
}