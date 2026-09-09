package com.example.applock.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
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

        private const val TAG =
            "AppLockService"

        private const val RELOCK_AFTER_QUITTING =
            "relock_after_quitting"

        private const val RELOCK_AFTER_SCREEN_OFF =
            "relock_after_screen_off"

        // =====================================================
        // FOREGROUND SERVICE
        // =====================================================

        private const val NOTIFICATION_CHANNEL_ID =
            "applock_service_channel"

        private const val NOTIFICATION_ID =
            1001
    }

    // =========================================================
    // COROUTINE
    // =========================================================

    private val serviceScope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.IO
        )

    // =========================================================
    // MAIN HANDLER
    // =========================================================

    private val handler =
        Handler(
            Looper.getMainLooper()
        )

    // =========================================================
    // PACKAGE TRACKING
    // =========================================================

    private var lastPackageName:
            String? =
        null

    private var checkingPackage:
            String? =
        null

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
    // SERVICE CONNECTION
    // =========================================================

    @Volatile
    private var serviceConnected =
        false

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

                if (
                    relockOption ==
                    RELOCK_AFTER_SCREEN_OFF
                ) {

                    if (
                        AppLockServiceHolder
                            .currentUnlockedApp != null
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
    // FOREGROUND NOTIFICATION
    // =========================================================

    private fun startForegroundServiceNotification() {

        try {

            // =================================================
            // CREATE NOTIFICATION CHANNEL
            // =================================================

            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O
            ) {

                val channel =
                    NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        "AppLock Protection",
                        NotificationManager
                            .IMPORTANCE_LOW
                    ).apply {

                        description =
                            "Keeps AppLock protection active"

                        setShowBadge(false)
                    }

                val notificationManager =
                    getSystemService(
                        NotificationManager::class.java
                    )

                notificationManager
                    .createNotificationChannel(
                        channel
                    )
            }

            // =================================================
            // CREATE NOTIFICATION
            // =================================================

            val notification =
                if (
                    Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.O
                ) {

                    Notification.Builder(
                        this,
                        NOTIFICATION_CHANNEL_ID
                    )
                        .setContentTitle(
                            "AppLock is active"
                        )
                        .setContentText(
                            "AppLock protection is running"
                        )
                        .setSmallIcon(
                            applicationInfo.icon
                        )
                        .setOngoing(true)
                        .setAutoCancel(false)
                        .setCategory(
                            Notification.CATEGORY_SERVICE
                        )
                        .build()

                } else {

                    @Suppress("DEPRECATION")
                    Notification.Builder(
                        this
                    )
                        .setContentTitle(
                            "AppLock is active"
                        )
                        .setContentText(
                            "AppLock protection is running"
                        )
                        .setSmallIcon(
                            applicationInfo.icon
                        )
                        .setOngoing(true)
                        .setAutoCancel(false)
                        .setCategory(
                            Notification.CATEGORY_SERVICE
                        )
                        .build()
                }

            // =================================================
            // START FOREGROUND SERVICE
            // =================================================

            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.Q
            ) {

                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo
                        .FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )

            } else {

                startForeground(
                    NOTIFICATION_ID,
                    notification
                )
            }

            Log.d(
                TAG,
                "================================"
            )

            Log.d(
                TAG,
                "FOREGROUND SERVICE STARTED"
            )

            Log.d(
                TAG,
                "================================"
            )

        } catch (e: Exception) {

            Log.e(
                TAG,
                "FOREGROUND SERVICE START ERROR",
                e
            )
        }
    }

    // =========================================================
    // ON CREATE
    // =========================================================

    override fun onCreate() {

        super.onCreate()

        Log.d(
            TAG,
            "================================"
        )

        Log.d(
            TAG,
            "SERVICE CREATED"
        )

        Log.d(
            TAG,
            "================================"
        )

        // =====================================================
        // START FOREGROUND IMMEDIATELY
        // =====================================================

        startForegroundServiceNotification()

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

            Log.d(
                TAG,
                "SCREEN OFF RECEIVER REGISTERED"
            )

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
            "========================================"
        )

        Log.d(
            TAG,
            "ACCESSIBILITY SERVICE CONNECTED"
        )

        Log.d(
            TAG,
            "========================================"
        )

        val info =
            AccessibilityServiceInfo().apply {

                eventTypes =
                    AccessibilityEvent
                        .TYPE_WINDOW_STATE_CHANGED or
                            AccessibilityEvent
                                .TYPE_WINDOWS_CHANGED

                feedbackType =
                    AccessibilityServiceInfo
                        .FEEDBACK_GENERIC

                notificationTimeout =
                    100

                flags =
                    AccessibilityServiceInfo
                        .FLAG_REPORT_VIEW_IDS
            }

        serviceInfo =
            info

        serviceConnected =
            true

        handler.removeCallbacks(
            resetUnlockedAppRunnable
        )

        AppLockServiceHolder
            .isLockScreenOpen =
            false

        checkingPackage =
            null

        lastPackageName =
            null

        Log.d(
            TAG,
            "SERVICE CONFIGURATION APPLIED"
        )

        Log.d(
            TAG,
            "EVENT TYPES = ${info.eventTypes}"
        )

        Log.d(
            TAG,
            "FLAGS = ${info.flags}"
        )

        Log.d(
            TAG,
            "SERVICE READY"
        )
    }

    // =========================================================
    // GET RELOCK DELAY
    // =========================================================

    private fun getRelockDelayMillis():
            Long {

        return when (relockDelay) {

            "five_seconds",
            "5_seconds" -> {

                5_000L
            }

            "fifteen_seconds",
            "15_seconds" -> {

                15_000L
            }

            "thirty_seconds",
            "30_seconds" -> {

                30_000L
            }

            "one_minute",
            "1_minute" -> {

                60_000L
            }

            "two_minutes",
            "2_minutes" -> {

                120_000L
            }

            "five_minutes",
            "5_minutes" -> {

                300_000L
            }

            "never" -> {

                0L
            }

            else -> {

                0L
            }
        }
    }

    // =========================================================
    // SCHEDULE RELOCK
    // =========================================================

    private fun scheduleRelock(
        reason: String
    ) {

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

        if (
            delayMillis <= 0L
        ) {

            Log.d(
                TAG,
                "NO DELAY -> RELOCK NOW"
            )

            resetUnlockedAppRunnable.run()

            return
        }

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

        // =====================================================
        // BASIC CHECKS
        // =====================================================

        if (!serviceConnected) {
            return
        }

        if (event == null) {
            return
        }

        if (
            event.eventType !=
            AccessibilityEvent
                .TYPE_WINDOW_STATE_CHANGED &&
            event.eventType !=
            AccessibilityEvent
                .TYPE_WINDOWS_CHANGED
        ) {
            return
        }

        // =====================================================
        // GET PACKAGE
        // =====================================================

        val packageName =
            event.packageName
                ?.toString()
                ?: return

        // =====================================================
        // IGNORE SYSTEM UI
        // =====================================================

        if (
            packageName ==
            "com.android.systemui"
        ) {
            return
        }

        // =====================================================
        // CHECK LAUNCHER
        // =====================================================

        val isLauncher =
            packageName.contains(
                "launcher",
                ignoreCase = true
            )

        if (isLauncher) {

            val cameFromApp =
                lastPackageName != null &&
                        !lastPackageName!!
                            .contains(
                                "launcher",
                                ignoreCase = true
                            )

            if (
                cameFromApp &&
                AppLockServiceHolder
                    .currentUnlockedApp != null
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
                        AppLockServiceHolder
                            .currentUnlockedApp
                    }"
                )

                Log.d(
                    TAG,
                    "================================"
                )

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

            AppLockServiceHolder
                .isLockScreenOpen =
                false

            checkingPackage =
                null

            lastPackageName =
                packageName

            return
        }

        // =====================================================
        // IGNORE OUR OWN APP
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
            AppLockServiceHolder
                .isLockScreenOpen
        ) {
            return
        }

        // =====================================================
        // APP ALREADY UNLOCKED
        // =====================================================

        if (
            AppLockServiceHolder
                .currentUnlockedApp ==
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
        // PACKAGE ALREADY BEING CHECKED
        // =====================================================

        if (
            checkingPackage ==
            packageName
        ) {
            return
        }

        // =====================================================
        // MARK PACKAGE AS CHECKING
        // =====================================================

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

                // =================================================
                // APP IS LOCKED
                // =================================================

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

                    handler.removeCallbacks(
                        resetUnlockedAppRunnable
                    )

                    AppLockServiceHolder
                        .isLockScreenOpen =
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

                    try {

                        startActivity(
                            intent
                        )

                        Log.d(
                            TAG,
                            "LOCK SCREEN STARTED = $packageName"
                        )

                        checkingPackage =
                            null

                    } catch (
                        e: Exception
                    ) {

                        Log.e(
                            TAG,
                            "FAILED TO OPEN LOCK SCREEN",
                            e
                        )

                        AppLockServiceHolder
                            .isLockScreenOpen =
                            false

                        checkingPackage =
                            null
                    }

                } else {

                    // =================================================
                    // APP IS NOT LOCKED
                    // =================================================

                    Log.d(
                        TAG,
                        "APP NOT LOCKED = $packageName"
                    )

                    checkingPackage =
                        null
                }

            } catch (
                e: Exception
            ) {

                Log.e(
                    TAG,
                    "CHECK LOCKED APP ERROR",
                    e
                )

                checkingPackage =
                    null
            }
        }

        // =====================================================
        // SAVE LAST PACKAGE
        // =====================================================

        lastPackageName =
            packageName
    }

    // =========================================================
    // INTERRUPT
    // =========================================================

    override fun onInterrupt() {

        Log.d(
            TAG,
            "================================"
        )

        Log.d(
            TAG,
            "SERVICE INTERRUPTED"
        )

        Log.d(
            TAG,
            "================================"
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
            "================================"
        )

        Log.d(
            TAG,
            "SERVICE UNBOUND"
        )

        Log.d(
            TAG,
            "================================"
        )

        serviceConnected =
            false

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
            "================================"
        )

        Log.d(
            TAG,
            "SERVICE REBOUND"
        )

        Log.d(
            TAG,
            "================================"
        )

        serviceConnected =
            true

        checkingPackage =
            null

        lastPackageName =
            null

        AppLockServiceHolder
            .isLockScreenOpen =
            false

        // =====================================================
        // ENSURE FOREGROUND
        // =====================================================

        startForegroundServiceNotification()
    }

    // =========================================================
    // DESTROY
    // =========================================================

    override fun onDestroy() {

        Log.d(
            TAG,
            "================================"
        )

        Log.d(
            TAG,
            "SERVICE DESTROYED"
        )

        Log.d(
            TAG,
            "================================"
        )

        serviceConnected =
            false

        handler.removeCallbacks(
            resetUnlockedAppRunnable
        )

        checkingPackage =
            null

        lastPackageName =
            null

        AppLockServiceHolder
            .isLockScreenOpen =
            false

        try {

            unregisterReceiver(
                screenOffReceiver
            )

            Log.d(
                TAG,
                "SCREEN OFF RECEIVER UNREGISTERED"
            )

        } catch (
            e: Exception
        ) {

            Log.e(
                TAG,
                "SCREEN OFF RECEIVER UNREGISTER ERROR",
                e
            )
        }

        serviceScope.cancel()

        // =====================================================
        // STOP FOREGROUND
        // =====================================================

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.N
        ) {

            stopForeground(
                STOP_FOREGROUND_REMOVE
            )

        } else {

            @Suppress("DEPRECATION")
            stopForeground(
                true
            )
        }

        super.onDestroy()
    }
}