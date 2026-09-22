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
            SupervisorJob() +
                    Dispatchers.IO
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

    private var lastPackageName: String? =
        null

    private var checkingPackage: String? =
        null


    // =========================================================
    // APP PROTECTION
    // =========================================================

    @Volatile
    private var appProtectionEnabled =
        false


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
                "RELOCK TIMER FINISHED"
            )

            AppLockServiceHolder
                .currentUnlockedApp =
                null

            AppLockServiceHolder
                .lastUnlockTime =
                0L

            AppLockServiceHolder
                .isLockScreenOpen =
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


                if (!appProtectionEnabled) {

                    Log.d(
                        TAG,
                        "SCREEN OFF -> PROTECTION OFF"
                    )

                    return
                }


                if (
                    relockOption ==
                    RELOCK_AFTER_SCREEN_OFF
                ) {

                    if (
                        AppLockServiceHolder
                            .currentUnlockedApp !=
                        null
                    ) {

                        scheduleRelock(
                            reason =
                                "SCREEN_OFF"
                        )
                    }
                }
            }
        }


    // =========================================================
    // FOREGROUND NOTIFICATION
    // =========================================================

    private fun startForegroundServiceNotification() {

        try {

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
                    Notification.Builder(this)
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
                "FOREGROUND NOTIFICATION STARTED"
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
    // CLEAR PROTECTION STATE
    // =========================================================

    private fun clearProtectionState() {

        handler.removeCallbacks(
            resetUnlockedAppRunnable
        )

        AppLockServiceHolder
            .currentUnlockedApp =
            null

        AppLockServiceHolder
            .lastUnlockTime =
            0L

        AppLockServiceHolder
            .isLockScreenOpen =
            false

        checkingPackage =
            null

        lastPackageName =
            null
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
        // FOREGROUND NOTIFICATION
        // =====================================================

        startForegroundServiceNotification()


        // =====================================================
        // SCREEN OFF RECEIVER
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
        // OBSERVE APP PROTECTION
        // =====================================================

        serviceScope.launch {

            try {

                DataStoreManager(
                    this@AppLockService
                )
                    .getAppProtectionEnabled()
                    .collectLatest { enabled ->

                        appProtectionEnabled =
                            enabled

                        Log.d(
                            TAG,
                            "APP PROTECTION = $enabled"
                        )

                        if (!enabled) {

                            clearProtectionState()

                            Log.d(
                                TAG,
                                "APP PROTECTION DISABLED"
                            )
                        }
                    }

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "APP PROTECTION OBSERVER ERROR",
                    e
                )
            }
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
                    .collectLatest { option ->

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
                    .collectLatest { delay ->

                        relockDelay =
                            when (delay) {

                                "never" ->
                                    "never"

                                "five_seconds",
                                "5_seconds" ->
                                    "five_seconds"

                                "ten_seconds",
                                "10_seconds" ->
                                    "ten_seconds"

                                "thirty_seconds",
                                "30_seconds" ->
                                    "thirty_seconds"

                                "one_minute",
                                "1_minute" ->
                                    "one_minute"

                                "two_minutes",
                                "2_minutes" ->
                                    "two_minutes"

                                "five_minutes",
                                "5_minutes" ->
                                    "five_minutes"

                                else ->
                                    "never"
                            }

                        Log.d(
                            TAG,
                            "RELOCK DELAY = $relockDelay"
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
            "ACCESSIBILITY SERVICE CONNECTED"
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
            "SERVICE READY"
        )
    }


    // =========================================================
    // GET RELOCK DELAY
    // =========================================================

    private fun getRelockDelayMillis(): Long {

        return when (relockDelay) {

            "never" ->
                0L

            "five_seconds",
            "5_seconds" ->
                5_000L

            "ten_seconds",
            "10_seconds" ->
                10_000L

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

        if (!appProtectionEnabled) {
            return
        }


        handler.removeCallbacks(
            resetUnlockedAppRunnable
        )


        val delayMillis =
            getRelockDelayMillis()


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
            "DELAY = $relockDelay"
        )

        Log.d(
            TAG,
            "DELAY MS = $delayMillis"
        )


        if (delayMillis <= 0L) {

            resetUnlockedAppRunnable.run()

            return
        }


        handler.postDelayed(
            resetUnlockedAppRunnable,
            delayMillis
        )
    }


    // =========================================================
    // ACCESSIBILITY EVENT
    // =========================================================

    override fun onAccessibilityEvent(
        event: AccessibilityEvent?
    ) {

        if (!appProtectionEnabled) {
            return
        }


        if (!serviceConnected) {
            return
        }


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
        // IGNORE SYSTEM UI
        // =====================================================

        if (
            packageName ==
            "com.android.systemui"
        ) {
            return
        }


        // =====================================================
        // LAUNCHER
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
                    .currentUnlockedApp !=
                null
            ) {

                if (
                    relockOption ==
                    RELOCK_AFTER_QUITTING
                ) {

                    scheduleRelock(
                        reason =
                            "QUITTING"
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
        // IGNORE APPLOCK ITSELF
        // =====================================================

        if (
            packageName ==
            applicationContext.packageName
        ) {
            return
        }


        // =====================================================
        // IGNORE WHILE LOCK SCREEN IS OPEN
        // =====================================================

        if (
            AppLockServiceHolder
                .isLockScreenOpen
        ) {
            return
        }


        // =====================================================
        // CURRENTLY UNLOCKED APP
        // =====================================================

        if (
            AppLockServiceHolder
                .currentUnlockedApp ==
            packageName
        ) {

            lastPackageName =
                packageName

            return
        }


        // =====================================================
        // ALREADY CHECKING
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

                if (!appProtectionEnabled) {

                    checkingPackage =
                        null

                    return@launch
                }


                val dataStore =
                    DataStoreManager(
                        this@AppLockService
                    )


                val lockedApps =
                    dataStore
                        .lockedAppsFlow
                        .first()


                // =================================================
                // APP IS LOCKED
                // =================================================

                if (
                    packageName in lockedApps
                ) {

                    if (!appProtectionEnabled) {

                        checkingPackage =
                            null

                        return@launch
                    }


                    handler.removeCallbacks(
                        resetUnlockedAppRunnable
                    )


                    AppLockServiceHolder
                        .isLockScreenOpen =
                        true


                    // =================================================
                    // OPEN LOCK SCREEN
                    // =================================================

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
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                            )
                        }


                    try {

                        if (!appProtectionEnabled) {

                            AppLockServiceHolder
                                .isLockScreenOpen =
                                false

                            checkingPackage =
                                null

                            return@launch
                        }


                        startActivity(
                            intent
                        )


                        checkingPackage =
                            null

                    } catch (e: Exception) {

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
    // INTERRUPT
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

        super.onRebind(intent)

        Log.d(
            TAG,
            "SERVICE REBOUND"
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

        startForegroundServiceNotification()
    }


    // =========================================================
    // DESTROY
    // =========================================================

    override fun onDestroy() {

        Log.d(
            TAG,
            "SERVICE DESTROYED"
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

        } catch (e: Exception) {

            Log.e(
                TAG,
                "SCREEN OFF RECEIVER UNREGISTER ERROR",
                e
            )
        }


        serviceScope.cancel()


        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.N
        ) {

            stopForeground(
                STOP_FOREGROUND_REMOVE
            )

        } else {

            @Suppress("DEPRECATION")
            stopForeground(true)
        }


        super.onDestroy()
    }
}