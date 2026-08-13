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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppLockService : AccessibilityService() {

    private val TAG = "APPLOCK_DEBUG"

    private val scope =
        CoroutineScope(Dispatchers.IO)

    // ------------------------------------------------
    // Launcher ke baad delayed reset
    // ------------------------------------------------

    private val handler =
        Handler(Looper.getMainLooper())

    private val resetUnlockedAppRunnable =
        Runnable {

            Log.d(
                TAG,
                "HOME TIME FINISHED -> RESET UNLOCK"
            )

            AppLockServiceHolder.currentUnlockedApp =
                null

            AppLockServiceHolder.lastUnlockTime =
                0L
        }

    override fun onServiceConnected() {

        super.onServiceConnected()

        Log.d(TAG, "================================")
        Log.d(TAG, "ACCESSIBILITY SERVICE CONNECTED")
        Log.d(TAG, "================================")
    }

    override fun onAccessibilityEvent(
        event: AccessibilityEvent?
    ) {

        if (event == null) {
            return
        }

        // ------------------------------------------------
        // Sirf Window State Changed
        // ------------------------------------------------

        if (
            event.eventType !=
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        ) {
            return
        }

        val packageName =
            event.packageName?.toString()
                ?: return

        Log.d(
            TAG,
            "CURRENT PACKAGE = $packageName"
        )

        // ------------------------------------------------
        // APPLOCK MAIN APP
        // ------------------------------------------------

        if (packageName == this.packageName) {

            Log.d(
                TAG,
                "APPLOCK OPENED"
            )

            /*
             * Agar user AppLock khud open karta hai,
             * previous WhatsApp unlock state ko
             * yahan reset kar do.
             *
             * Isse AppLock ki screen par lock nahi aayega.
             */

            handler.removeCallbacks(
                resetUnlockedAppRunnable
            )

            AppLockServiceHolder.currentUnlockedApp =
                null

            AppLockServiceHolder.lastUnlockTime =
                0L

            AppLockServiceHolder.isLockScreenOpen =
                false

            return
        }

        // ------------------------------------------------
        // HOME / LAUNCHER
        // ------------------------------------------------

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
                1500L
            )

            AppLockServiceHolder.isLockScreenOpen =
                false

            return
        }

        // ------------------------------------------------
        // SYSTEM UI
        // ------------------------------------------------

        if (
            packageName ==
            "com.android.systemui"
        ) {

            Log.d(
                TAG,
                "SYSTEM UI DETECTED"
            )

            return
        }

        // ------------------------------------------------
        // LockScreen already open
        // ------------------------------------------------

        if (
            AppLockServiceHolder.isLockScreenOpen
        ) {

            Log.d(
                TAG,
                "LOCK SCREEN ALREADY OPEN"
            )

            return
        }

        // ------------------------------------------------
        // Locked apps read karo
        // ------------------------------------------------

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

                Log.d(
                    TAG,
                    "CHECKING APP = $packageName"
                )

                // ------------------------------------------------
                // Check current app locked hai?
                // ------------------------------------------------

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

                // ------------------------------------------------
                // Same app already unlocked
                // ------------------------------------------------

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

                // ------------------------------------------------
                // Lock required
                // ------------------------------------------------

                Log.d(
                    TAG,
                    "🔒 LOCK REQUIRED = $packageName"
                )

                // Old delayed reset cancel
                handler.removeCallbacks(
                    resetUnlockedAppRunnable
                )

                // LockScreen flag
                AppLockServiceHolder.isLockScreenOpen =
                    true

                withContext(
                    Dispatchers.Main
                ) {

                    try {

                        val lockIntent =
                            Intent(
                                this@AppLockService,
                                LockScreenActivity::class.java
                            )

                        lockIntent.putExtra(
                            "packageName",
                            packageName
                        )

                        lockIntent.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        )

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

                        AppLockServiceHolder
                            .isLockScreenOpen =
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

    override fun onInterrupt() {

        Log.d(
            TAG,
            "SERVICE INTERRUPTED"
        )
    }

    override fun onDestroy() {

        super.onDestroy()

        handler.removeCallbacks(
            resetUnlockedAppRunnable
        )

        AppLockServiceHolder.isLockScreenOpen =
            false

        AppLockServiceHolder.currentUnlockedApp =
            null

        AppLockServiceHolder.lastUnlockTime =
            0L

        Log.d(
            TAG,
            "SERVICE DESTROYED"
        )
    }
}