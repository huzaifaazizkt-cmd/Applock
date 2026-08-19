package com.example.applock.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.applock.Design.screens.LockScreenActivity
import com.example.applock.data.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppLockService : AccessibilityService() {

    private val TAG = "APPLOCK_DEBUG"

    private val scope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.IO
        )

    private val handler =
        Handler(Looper.getMainLooper())

    // =================================================
    // RESET UNLOCKED APP
    // =================================================

    private val resetUnlockedAppRunnable =
        Runnable {

            Log.d(
                TAG,
                "HOME -> RESET UNLOCKED APP"
            )

            AppLockServiceHolder.currentUnlockedApp =
                null

            AppLockServiceHolder.lastUnlockTime =
                0L
        }

    // =================================================
    // SERVICE CONNECTED
    // =================================================

    override fun onServiceConnected() {

        super.onServiceConnected()

        Log.d(TAG, "================================")
        Log.d(TAG, "ACCESSIBILITY SERVICE CONNECTED")
        Log.d(TAG, "SERVICE IS RUNNING")
        Log.d(TAG, "================================")
    }

    // =================================================
    // ACCESSIBILITY EVENT
    // =================================================

    override fun onAccessibilityEvent(
        event: AccessibilityEvent?
    ) {

        if (event == null) {
            return
        }

        if (
            event.eventType !=
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        ) {
            return
        }

        val packageName =
            event.packageName
                ?.toString()
                ?: return

        Log.d(
            TAG,
            "CURRENT PACKAGE = $packageName"
        )

        // =================================================
        // SYSTEM UI
        // =================================================

        if (
            packageName == "com.android.systemui"
        ) {

            return
        }

        // =================================================
        // HOME / LAUNCHER
        // =================================================

        if (
            packageName.contains(
                "launcher",
                ignoreCase = true
            )
        ) {

            Log.d(
                TAG,
                "HOME / LAUNCHER DETECTED"
            )

            handler.removeCallbacks(
                resetUnlockedAppRunnable
            )

            handler.postDelayed(
                resetUnlockedAppRunnable,
                500L
            )

            AppLockServiceHolder.isLockScreenOpen =
                false

            return
        }

        // =================================================
        // LOCK SCREEN ALREADY OPEN
        // =================================================

        if (
            AppLockServiceHolder.isLockScreenOpen
        ) {

            Log.d(
                TAG,
                "LOCK SCREEN ALREADY OPEN"
            )

            return
        }

        // =================================================
        // APPLOCK MAIN APP
        // =================================================

        if (
            packageName == this.packageName
        ) {

            Log.d(
                TAG,
                "APPLOCK APP DETECTED"
            )

            return
        }

        // =================================================
        // CHECK LOCKED APPS
        // =================================================

        scope.launch {

            try {

                val lockedApps =
                    DataStoreManager(
                        this@AppLockService
                    )
                        .lockedAppsFlow
                        .first()

                Log.d(
                    TAG,
                    "LOCKED APPS = $lockedApps"
                )

                // -------------------------------------------------
                // APP NOT LOCKED
                // -------------------------------------------------

                if (
                    !lockedApps.contains(
                        packageName
                    )
                ) {

                    Log.d(
                        TAG,
                        "APP NOT LOCKED = $packageName"
                    )

                    return@launch
                }

                // -------------------------------------------------
                // ALREADY UNLOCKED
                // -------------------------------------------------

                if (
                    AppLockServiceHolder.currentUnlockedApp ==
                    packageName
                ) {

                    Log.d(
                        TAG,
                        "APP ALREADY UNLOCKED = $packageName"
                    )

                    return@launch
                }

                // -------------------------------------------------
                // LOCK REQUIRED
                // -------------------------------------------------

                Log.d(
                    TAG,
                    "================================"
                )

                Log.d(
                    TAG,
                    "LOCK REQUIRED = $packageName"
                )

                Log.d(
                    TAG,
                    "================================"
                )

                handler.removeCallbacks(
                    resetUnlockedAppRunnable
                )

                AppLockServiceHolder.isLockScreenOpen =
                    true

                // -------------------------------------------------
                // START LOCK SCREEN
                // -------------------------------------------------

                withContext(
                    Dispatchers.Main
                ) {

                    try {

                        val lockIntent =
                            Intent(
                                this@AppLockService,
                                LockScreenActivity::class.java
                            ).apply {

                                putExtra(
                                    "packageName",
                                    packageName
                                )

                                addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK or
                                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                                )
                            }

                        Log.d(
                            TAG,
                            "STARTING LOCK SCREEN"
                        )

                        startActivity(
                            lockIntent
                        )

                        Log.d(
                            TAG,
                            "LOCK SCREEN STARTED"
                        )

                    } catch (e: Exception) {

                        Log.e(
                            TAG,
                            "LOCK SCREEN START ERROR",
                            e
                        )

                        AppLockServiceHolder.isLockScreenOpen =
                            false
                    }
                }

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "LOCK CHECK ERROR",
                    e
                )
            }
        }
    }

    // =================================================
    // INTERRUPT
    // =================================================

    override fun onInterrupt() {

        Log.d(
            TAG,
            "ACCESSIBILITY SERVICE INTERRUPTED"
        )
    }

    // =================================================
    // DESTROY
    // =================================================

    override fun onDestroy() {

        Log.d(
            TAG,
            "ACCESSIBILITY SERVICE DESTROYED"
        )

        handler.removeCallbacks(
            resetUnlockedAppRunnable
        )

        AppLockServiceHolder.isLockScreenOpen =
            false

        AppLockServiceHolder.currentUnlockedApp =
            null

        AppLockServiceHolder.lastUnlockTime =
            0L

        scope.cancel()

        super.onDestroy()
    }
}