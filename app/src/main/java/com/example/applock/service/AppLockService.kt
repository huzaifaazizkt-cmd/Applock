package com.example.applock.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppLockService : AccessibilityService() {

    companion object {

        private const val TAG = "AppLockService"

        private const val RELOCK_AFTER_QUITTING =
            "relock_after_quitting"

        private const val RELOCK_AFTER_SCREEN_OFF =
            "relock_after_screen_off"
    }

    private val serviceScope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.IO
        )

    private val handler =
        Handler(Looper.getMainLooper())

    // =========================================================
    // PACKAGE TRACKING
    // =========================================================

    private var lastPackageName: String? = null

    private var checkingPackage: String? = null

    // =========================================================
    // RELOCK OPTION
    // =========================================================

    @Volatile
    private var relockOption =
        RELOCK_AFTER_QUITTING

    // =========================================================
    // RELOCK DELAY
    // =========================================================

    @Volatile
    private var relockDelay =
        "never"

    // =========================================================
    // RESET UNLOCKED APP
    // =========================================================

    private val resetUnlockedAppRunnable =
        Runnable {

            Log.d(
                TAG,
                "================================"
            )

            Log.d(
                TAG,
                "RELOCK TIMER FINISHED"
            )

            Log.d(
                TAG,
                "CLEARING UNLOCKED APP"
            )

            Log.d(
                TAG,
                "================================"
            )

            AppLockServiceHolder.currentUnlockedApp =
                null

            AppLockServiceHolder.lastUnlockTime =
                0L

            AppLockServiceHolder.isLockScreenOpen =
                false

            checkingPackage =
                null
        }

    // =========================================================
    // SCREEN OFF RECEIVER
    // =========================================================

    private val screenOffReceiver =
        object : BroadcastReceiver() {

            override fun onReceive(
                context: Context?,
                intent: Intent?
            ) {

                if (
                    intent?.action !=
                    Intent.ACTION_SCREEN_OFF
                ) {
                    return
                }

                Log.d(
                    TAG,
                    "================================"
                )

                Log.d(
                    TAG,
                    "SCREEN OFF DETECTED"
                )

                Log.d(
                    TAG,
                    "CURRENT RELOCK OPTION = $relockOption"
                )

                Log.d(
                    TAG,
                    "CURRENT DELAY = $relockDelay"
                )

                Log.d(
                    TAG,
                    "================================"
                )

                // -------------------------------------------------
                // SCREEN OFF MODE
                // -------------------------------------------------

                if (
                    relockOption ==
                    RELOCK_AFTER_SCREEN_OFF
                ) {

                    // Only schedule if an app is currently unlocked.
                    if (
                        AppLockServiceHolder.currentUnlockedApp !=
                        null
                    ) {

                        Log.d(
                            TAG,
                            "SCREEN OFF MODE ACTIVE"
                        )

                        scheduleRelock(
                            reason = "SCREEN_OFF"
                        )

                    } else {

                        Log.d(
                            TAG,
                            "NO UNLOCKED APP -> NOTHING TO RELOCK"
                        )
                    }

                } else {

                    Log.d(
                        TAG,
                        "QUITTING MODE -> SCREEN OFF IGNORED"
                    )
                }
            }
        }

    // =========================================================
    // ON CREATE
    // =========================================================

    override fun onCreate() {

        super.onCreate()

        Log.d(
            TAG,
            "SERVICE CREATED"
        )

        // =====================================================
        // REGISTER SCREEN OFF RECEIVER
        // =====================================================

        try {

            val filter =
                IntentFilter(
                    Intent.ACTION_SCREEN_OFF
                )

            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.TIRAMISU
            ) {

                registerReceiver(
                    screenOffReceiver,
                    filter,
                    Context.RECEIVER_NOT_EXPORTED
                )

            } else {

                @Suppress("DEPRECATION")

                registerReceiver(
                    screenOffReceiver,
                    filter
                )
            }

        } catch (e: Exception) {

            Log.e(
                TAG,
                "SCREEN OFF RECEIVER REGISTER ERROR",
                e
            )
        }

        // =====================================================
        // OBSERVE RELOCK OPTION
        // =====================================================

        serviceScope.launch {

            try {

                DataStoreManager(
                    this@AppLockService
                )
                    .getRelockOption()
                    .collectLatest { option: String ->

                        relockOption =
                            option

                        Log.d(
                            TAG,
                            "RELOCK OPTION = $option"
                        )
                    }

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "RELOCK OPTION OBSERVER ERROR",
                    e
                )
            }
        }

        // =====================================================
        // OBSERVE RELOCK DELAY
        // =====================================================

        serviceScope.launch {

            try {

                DataStoreManager(
                    this@AppLockService
                )
                    .getRelockDelay()
                    .collectLatest { delay: String ->

                        relockDelay =
                            delay

                        Log.d(
                            TAG,
                            "RELOCK DELAY = $delay"
                        )
                    }

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "RELOCK DELAY OBSERVER ERROR",
                    e
                )
            }
        }
    }

    // =========================================================
    // SERVICE CONNECTED
    // =========================================================

    override fun onServiceConnected() {

        super.onServiceConnected()

        Log.d(
            TAG,
            "================================"
        )

        Log.d(
            TAG,
            "ACCESSIBILITY SERVICE CONNECTED"
        )

        Log.d(
            TAG,
            "================================"
        )

        serviceInfo =
            serviceInfo?.apply {

                eventTypes =
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                            AccessibilityEvent.TYPE_WINDOWS_CHANGED

                feedbackType =
                    AccessibilityServiceInfo.FEEDBACK_GENERIC

                notificationTimeout =
                    100

                flags =
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            }

        handler.removeCallbacks(
            resetUnlockedAppRunnable
        )

        AppLockServiceHolder.isLockScreenOpen =
            false

        checkingPackage =
            null

        lastPackageName =
            null
    }

    // =========================================================
    // GET DELAY
    // =========================================================

    private fun getRelockDelayMillis(): Long {

        return when (relockDelay) {

            "five_seconds",
            "5_seconds" ->
                5_000L

            "fifteen_seconds",
            "15_seconds" ->
                15_000L

            "thirty_seconds",
            "30_seconds" ->
                30_000L

            "one_minute",
            "1_minute" ->
                60_000L

            "two_minutes",
            "2_minutes" ->
                120_000L

            "five_minutes",
            "5_minutes" ->
                300_000L

            // No delay = immediately relock
            "never" ->
                0L

            else ->
                0L
        }
    }

    // =========================================================
    // SCHEDULE RELOCK
    // =========================================================

    private fun scheduleRelock(
        reason: String
    ) {

        // Cancel previous timer.
        handler.removeCallbacks(
            resetUnlockedAppRunnable
        )

        val delayMillis =
            getRelockDelayMillis()

        Log.d(
            TAG,
            "================================"
        )

        Log.d(
            TAG,
            "SCHEDULING RELOCK"
        )

        Log.d(
            TAG,
            "REASON = $reason"
        )

        Log.d(
            TAG,
            "DELAY KEY = $relockDelay"
        )

        Log.d(
            TAG,
            "DELAY MS = $delayMillis"
        )

        Log.d(
            TAG,
            "================================"
        )

        // =====================================================
        // NEVER = NO DELAY
        // =====================================================

        if (delayMillis <= 0L) {

            Log.d(
                TAG,
                "NO DELAY -> RELOCK NOW"
            )

            resetUnlockedAppRunnable.run()

            return
        }

        // =====================================================
        // DELAYED RELOCK
        // =====================================================

        handler.postDelayed(
            resetUnlockedAppRunnable,
            delayMillis
        )

        Log.d(
            TAG,
            "RELOCK TIMER STARTED"
        )
    }

    // =========================================================
    // ACCESSIBILITY EVENT
    // =========================================================

    override fun onAccessibilityEvent(
        event: AccessibilityEvent?
    ) {

        if (event == null) {
            return
        }

        if (
            event.eventType !=
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType !=
            AccessibilityEvent.TYPE_WINDOWS_CHANGED
        ) {
            return
        }

        val packageName =
            event.packageName
                ?.toString()
                ?: return

        // =====================================================
        // SYSTEM UI
        // =====================================================

        if (
            packageName ==
            "com.android.systemui"
        ) {
            return
        }

        // =====================================================
        // HOME / LAUNCHER
        // =====================================================

        val isLauncher =
            packageName.contains(
                "launcher",
                ignoreCase = true
            )

        if (isLauncher) {

            /*
             * IMPORTANT:
             *
             * Launcher generates MANY accessibility events.
             *
             * We only start the timer when we actually
             * TRANSITION from an app to Launcher.
             *
             * This prevents the timer from restarting
             * again and again.
             */

            val cameFromApp =
                lastPackageName != null &&
                        !lastPackageName!!.contains(
                            "launcher",
                            ignoreCase = true
                        )

            if (
                cameFromApp &&
                AppLockServiceHolder.currentUnlockedApp !=
                null
            ) {

                Log.d(
                    TAG,
                    "================================"
                )

                Log.d(
                    TAG,
                    "APP -> HOME DETECTED"
                )

                Log.d(
                    TAG,
                    "UNLOCKED APP = ${
                        AppLockServiceHolder.currentUnlockedApp
                    }"
                )

                Log.d(
                    TAG,
                    "================================"
                )

                // =================================================
                // RELOCK AFTER QUITTING
                // =================================================

                if (
                    relockOption ==
                    RELOCK_AFTER_QUITTING
                ) {

                    scheduleRelock(
                        reason = "QUITTING"
                    )

                } else {

                    Log.d(
                        TAG,
                        "SCREEN OFF MODE -> QUITTING IGNORED"
                    )
                }
            }

            AppLockServiceHolder.isLockScreenOpen =
                false

            lastPackageName =
                packageName

            checkingPackage =
                null

            return
        }

        // =====================================================
        // IGNORE APPLOCK
        // =====================================================

        if (
            packageName ==
            applicationContext.packageName
        ) {

            return
        }

        // =====================================================
        // LOCK SCREEN ALREADY OPEN
        // =====================================================

        if (
            AppLockServiceHolder.isLockScreenOpen
        ) {

            return
        }

        // =====================================================
        // SAME APP ALREADY UNLOCKED
        // =====================================================

        if (
            AppLockServiceHolder.currentUnlockedApp ==
            packageName
        ) {

            Log.d(
                TAG,
                "APP ALREADY UNLOCKED = $packageName"
            )

            lastPackageName =
                packageName

            return
        }

        // =====================================================
        // DUPLICATE CHECK
        // =====================================================

        if (
            checkingPackage ==
            packageName
        ) {

            return
        }

        checkingPackage =
            packageName

        // =====================================================
        // CHECK LOCKED APPS
        // =====================================================

        serviceScope.launch {

            try {

                val dataStore =
                    DataStoreManager(
                        this@AppLockService
                    )

                val lockedApps =
                    dataStore
                        .lockedAppsFlow
                        .first()

                Log.d(
                    TAG,
                    "LOCKED APPS = $lockedApps"
                )

                if (
                    packageName in lockedApps
                ) {

                    Log.d(
                        TAG,
                        "================================"
                    )

                    Log.d(
                        TAG,
                        "LOCK REQUIRED"
                    )

                    Log.d(
                        TAG,
                        "PACKAGE = $packageName"
                    )

                    Log.d(
                        TAG,
                        "================================"
                    )

                    // Cancel pending timer because
                    // the user is opening an app.
                    handler.removeCallbacks(
                        resetUnlockedAppRunnable
                    )

                    AppLockServiceHolder.isLockScreenOpen =
                        true

                    val intent =
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

                    startActivity(
                        intent
                    )

                } else {

                    Log.d(
                        TAG,
                        "APP NOT LOCKED = $packageName"
                    )

                    checkingPackage =
                        null
                }

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "CHECK LOCKED APP ERROR",
                    e
                )

                checkingPackage =
                    null
            }
        }

        lastPackageName =
            packageName
    }

    // =========================================================
    // INTERRUPTED
    // =========================================================

    override fun onInterrupt() {

        Log.d(
            TAG,
            "SERVICE INTERRUPTED"
        )
    }

    // =========================================================
    // UNBIND
    // =========================================================

    override fun onUnbind(
        intent: Intent?
    ): Boolean {

        Log.d(
            TAG,
            "SERVICE UNBOUND"
        )

        handler.removeCallbacks(
            resetUnlockedAppRunnable
        )

        checkingPackage =
            null

        return true
    }

    // =========================================================
    // REBIND
    // =========================================================

    override fun onRebind(
        intent: Intent?
    ) {

        super.onRebind(
            intent
        )

        Log.d(
            TAG,
            "SERVICE REBOUND"
        )

        handler.removeCallbacks(
            resetUnlockedAppRunnable
        )

        checkingPackage =
            null

        AppLockServiceHolder.isLockScreenOpen =
            false

        lastPackageName =
            null
    }

    // =========================================================
    // DESTROY
    // =========================================================

    override fun onDestroy() {

        Log.d(
            TAG,
            "SERVICE DESTROYED"
        )

        handler.removeCallbacks(
            resetUnlockedAppRunnable
        )

        try {

            unregisterReceiver(
                screenOffReceiver
            )

        } catch (e: Exception) {

            Log.e(
                TAG,
                "SCREEN OFF RECEIVER UNREGISTER ERROR",
                e
            )
        }

        serviceScope.cancel()

        super.onDestroy()
    }
}