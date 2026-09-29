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
import kotlinx.coroutines.withContext


class AppLockService : AccessibilityService() {

    companion object {

        private const val TAG = "AppLockService"

        private const val RELOCK_AFTER_QUITTING =
            "relock_after_quitting"

        private const val RELOCK_AFTER_SCREEN_OFF =
            "relock_after_screen_off"

        private const val NOTIFICATION_CHANNEL_ID =
            "applock_service_channel"

        private const val NOTIFICATION_ID =
            1001
    }


    private val serviceScope =
        CoroutineScope(
            SupervisorJob() +
                    Dispatchers.IO
        )


    private val handler =
        Handler(
            Looper.getMainLooper()
        )


    private var lastPackageName: String? =
        null

    private var checkingPackage: String? =
        null


    @Volatile
    private var appProtectionEnabled =
        false


    @Volatile
    private var relockOption =
        RELOCK_AFTER_QUITTING


    @Volatile
    private var relockDelay =
        "never"


    @Volatile
    private var serviceConnected =
        false


    @Volatile
    private var lockedAppsCache: Set<String> =
        emptySet()


    @Volatile
    private var lockedAppsCacheReady =
        false


    private var homePackageName: String? =
        null


    private val resetUnlockedAppRunnable =
        Runnable {

            Log.d(
                TAG,
                "RELOCK TIMER FINISHED"
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


    private fun resolveHomePackage() {

        try {

            val homeIntent =
                Intent(
                    Intent.ACTION_MAIN
                ).apply {
                    addCategory(
                        Intent.CATEGORY_HOME
                    )
                }

            val resolveInfo =
                packageManager.resolveActivity(
                    homeIntent,
                    0
                )

            homePackageName =
                resolveInfo
                    ?.activityInfo
                    ?.packageName

            Log.d(
                TAG,
                "HOME PACKAGE = $homePackageName"
            )

        } catch (e: Exception) {

            Log.e(
                TAG,
                "HOME PACKAGE ERROR",
                e
            )
        }
    }


    private fun clearProtectionState() {

        handler.removeCallbacks(
            resetUnlockedAppRunnable
        )

        AppLockServiceHolder.currentUnlockedApp =
            null

        AppLockServiceHolder.lastUnlockTime =
            0L

        AppLockServiceHolder.isLockScreenOpen =
            false

        AppLockServiceHolder.clearSuppressedPackage()

        checkingPackage =
            null

        lastPackageName =
            null
    }


    override fun onCreate() {

        super.onCreate()

        Log.d(
            TAG,
            "SERVICE CREATED"
        )


        startForegroundServiceNotification()


        resolveHomePackage()


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


        serviceScope.launch {

            try {

                DataStoreManager(
                    this@AppLockService
                )
                    .lockedAppsFlow
                    .collectLatest { apps ->

                        lockedAppsCache =
                            apps.toSet()

                        lockedAppsCacheReady =
                            true

                        Log.d(
                            TAG,
                            "LOCKED APPS CACHE = $lockedAppsCache"
                        )
                    }

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "LOCKED APPS OBSERVER ERROR",
                    e
                )
            }
        }


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
                    50

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

        AppLockServiceHolder.isLockScreenOpen =
            false

        checkingPackage =
            null

        lastPackageName =
            null

        resolveHomePackage()


        Log.d(
            TAG,
            "SERVICE READY"
        )
    }


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

            Log.d(
                TAG,
                "RELOCK DISABLED - KEEP APP UNLOCKED"
            )

            return
        }


        handler.postDelayed(
            resetUnlockedAppRunnable,
            delayMillis
        )
    }


    private fun isLauncherPackage(
        packageName: String
    ): Boolean {

        if (
            homePackageName != null &&
            packageName ==
            homePackageName
        ) {
            return true
        }

        return packageName.contains(
            "launcher",
            ignoreCase = true
        )
    }


    private fun openLockScreen(
        packageName: String
    ) {

        if (!appProtectionEnabled) {

            checkingPackage =
                null

            return
        }


        if (
            AppLockServiceHolder
                .isPackageSuppressed(packageName)
        ) {

            Log.d(
                TAG,
                "PACKAGE SUPPRESSED AFTER BACK = $packageName"
            )

            checkingPackage =
                null

            return
        }


        if (
            AppLockServiceHolder.isLockScreenOpen
        ) {

            checkingPackage =
                null

            return
        }


        Log.d(
            TAG,
            "================================"
        )

        Log.d(
            TAG,
            "OPENING LOCK SCREEN FOR = $packageName"
        )

        Log.d(
            TAG,
            "================================"
        )


        handler.post {

            try {

                if (!appProtectionEnabled) {

                    checkingPackage =
                        null

                    return@post
                }


                if (
                    AppLockServiceHolder
                        .isPackageSuppressed(
                            packageName
                        )
                ) {

                    Log.d(
                        TAG,
                        "SUPPRESSED BEFORE LAUNCH = $packageName"
                    )

                    checkingPackage =
                        null

                    return@post
                }


                if (
                    AppLockServiceHolder
                        .isLockScreenOpen
                ) {

                    checkingPackage =
                        null

                    return@post
                }


                AppLockServiceHolder
                    .isLockScreenOpen = true


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
                                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                        )
                    }


                startActivity(
                    lockIntent
                )


                Log.d(
                    TAG,
                    "LOCK SCREEN STARTED = $packageName"
                )

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "FAILED TO OPEN LOCK SCREEN",
                    e
                )

                AppLockServiceHolder
                    .isLockScreenOpen = false

            } finally {

                checkingPackage =
                    null
            }
        }
    }


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


        Log.d(
            TAG,
            "CURRENT PACKAGE = $packageName"
        )


        if (
            packageName ==
            "com.android.systemui"
        ) {
            return
        }


        if (
            isLauncherPackage(
                packageName
            )
        ) {

            Log.d(
                TAG,
                "LAUNCHER DETECTED = $packageName"
            )


            if (
                lastPackageName != null &&
                lastPackageName != packageName &&
                AppLockServiceHolder
                    .currentUnlockedApp != null
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
                .clearSuppressedPackage()

            AppLockServiceHolder
                .isLockScreenOpen = false

            checkingPackage =
                null

            lastPackageName =
                packageName

            Log.d(
                TAG,
                "LAUNCHER DETECTED -> SUPPRESSION CLEARED"
            )

            return
        }


        if (
            packageName ==
            applicationContext.packageName
        ) {

            Log.d(
                TAG,
                "APPLOCK ITSELF -> IGNORE"
            )

            lastPackageName =
                packageName

            return
        }


        if (
            AppLockServiceHolder
                .isLockScreenOpen
        ) {

            return
        }


        if (
            AppLockServiceHolder
                .currentUnlockedApp ==
            packageName
        ) {

            lastPackageName =
                packageName

            Log.d(
                TAG,
                "APP ALREADY UNLOCKED = $packageName"
            )

            return
        }


        if (
            AppLockServiceHolder
                .isPackageSuppressed(
                    packageName
                )
        ) {

            lastPackageName =
                packageName

            Log.d(
                TAG,
                "PACKAGE SUPPRESSED AFTER BACK = $packageName"
            )

            return
        }


        if (
            checkingPackage ==
            packageName
        ) {

            return
        }


        checkingPackage =
            packageName


        lastPackageName =
            packageName


        if (lockedAppsCacheReady) {

            if (
                lockedAppsCache.contains(
                    packageName
                )
            ) {

                openLockScreen(
                    packageName
                )

            } else {

                checkingPackage =
                    null
            }

            return
        }


        serviceScope.launch {

            try {

                val lockedApps =
                    DataStoreManager(
                        this@AppLockService
                    )
                        .lockedAppsFlow
                        .first()


                lockedAppsCache =
                    lockedApps.toSet()

                lockedAppsCacheReady =
                    true


                if (
                    lockedApps.contains(
                        packageName
                    )
                ) {

                    withContext(
                        Dispatchers.Main
                    ) {

                        openLockScreen(
                            packageName
                        )
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
    }


    override fun onInterrupt() {

        Log.d(
            TAG,
            "SERVICE INTERRUPTED"
        )
    }


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

        serviceConnected =
            true

        checkingPackage =
            null

        lastPackageName =
            null

        lockedAppsCacheReady =
            false

        AppLockServiceHolder
            .isLockScreenOpen = false

        resolveHomePackage()

        startForegroundServiceNotification()
    }


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

        lockedAppsCache =
            emptySet()

        lockedAppsCacheReady =
            false

        AppLockServiceHolder
            .isLockScreenOpen = false


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