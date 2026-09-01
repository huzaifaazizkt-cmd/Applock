package com.example.applock.service.com.example.applock.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.applock.Design.screens.LockScreenActivity
import com.example.applock.data.DataStoreManager
import com.example.applock.service.AppLockServiceHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppLockService : AccessibilityService() {

    private val TAG = "APPLOCK_DEBUG"

    private val serviceJob = SupervisorJob()

    private val scope =
        CoroutineScope(
            Dispatchers.IO + serviceJob
        )

    private val handler =
        Handler(Looper.getMainLooper())

    @Volatile
    private var lastPackageName: String? = null

    @Volatile
    private var checkingPackage: String? = null

    // =========================================================
    // RESET UNLOCKED APP
    // =========================================================

    private val resetUnlockedAppRunnable =
        Runnable {

            Log.d(
                TAG,
                "RESET UNLOCKED APP"
            )

            AppLockServiceHolder.currentUnlockedApp = null

            AppLockServiceHolder.lastUnlockTime = 0L
        }

    // =========================================================
    // CREATE
    // =========================================================

    override fun onCreate() {

        super.onCreate()

        Log.d(
            TAG,
            "================================"
        )

        Log.d(
            TAG,
            "ACCESSIBILITY SERVICE CREATED"
        )

        Log.d(
            TAG,
            "================================"
        )
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
            "SERVICE IS RUNNING"
        )

        Log.d(
            TAG,
            "================================"
        )

        try {

            val info =
                serviceInfo

            Log.d(
                TAG,
                "EVENT TYPES = ${info.eventTypes}"
            )

            Log.d(
                TAG,
                "FEEDBACK TYPE = ${info.feedbackType}"
            )

            Log.d(
                TAG,
                "NOTIFICATION TIMEOUT = ${info.notificationTimeout}"
            )

            Log.d(
                TAG,
                "SERVICE FLAGS = ${info.flags}"
            )

        } catch (e: Exception) {

            Log.e(
                TAG,
                "SERVICE INFO ERROR",
                e
            )
        }

        handler.removeCallbacks(
            resetUnlockedAppRunnable
        )

        lastPackageName = null

        checkingPackage = null

        /*
         * Do not call AppLockServiceHolder.clear().
         *
         * Android can reconnect the accessibility service
         * while the application is still running.
         */

        AppLockServiceHolder.isLockScreenOpen = false

        Log.d(
            TAG,
            "SERVICE INITIALIZATION COMPLETE"
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

        // -----------------------------------------------------
        // WINDOW EVENTS ONLY
        // -----------------------------------------------------

        if (
            event.eventType !=
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType !=
            AccessibilityEvent.TYPE_WINDOWS_CHANGED
        ) {
            return
        }

        // -----------------------------------------------------
        // PACKAGE
        // -----------------------------------------------------

        val packageName =
            event.packageName
                ?.toString()
                ?.trim()
                ?: return

        if (packageName.isEmpty()) {
            return
        }

        Log.d(
            TAG,
            "CURRENT PACKAGE = $packageName"
        )

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
                700L
            )

            AppLockServiceHolder.isLockScreenOpen =
                false

            lastPackageName =
                packageName

            checkingPackage =
                null

            return
        }

        // =====================================================
        // APPLOCK ITSELF
        // =====================================================

        if (
            packageName ==
            this.packageName
        ) {

            Log.d(
                TAG,
                "APPLOCK APP DETECTED"
            )

            lastPackageName =
                packageName

            return
        }

        // =====================================================
        // LOCK SCREEN OPEN
        // =====================================================

        if (
            AppLockServiceHolder.isLockScreenOpen
        ) {

            Log.d(
                TAG,
                "LOCK SCREEN ALREADY OPEN"
            )

            return
        }

        // =====================================================
        // ALREADY UNLOCKED
        // =====================================================

        if (
            AppLockServiceHolder.currentUnlockedApp ==
            packageName
        ) {

            Log.d(
                TAG,
                "APP ALREADY UNLOCKED = $packageName"
            )

            return
        }

        // =====================================================
        // DUPLICATE CHECK
        // =====================================================

        if (
            checkingPackage ==
            packageName
        ) {

            Log.d(
                TAG,
                "ALREADY CHECKING = $packageName"
            )

            return
        }

        lastPackageName =
            packageName

        checkingPackage =
            packageName

        Log.d(
            TAG,
            "STARTING LOCK CHECK = $packageName"
        )

        // =====================================================
        // DATASTORE CHECK
        // =====================================================

        scope.launch {

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

                // -------------------------------------------------
                // NOT LOCKED
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

                    checkingPackage =
                        null

                    return@launch
                }

                // -------------------------------------------------
                // ALREADY UNLOCKED
                // -------------------------------------------------

                if (
                    AppLockServiceHolder.currentUnlockedApp ==
                    packageName
                ) {

                    checkingPackage =
                        null

                    return@launch
                }

                // -------------------------------------------------
                // LOCK SCREEN ALREADY OPEN
                // -------------------------------------------------

                if (
                    AppLockServiceHolder.isLockScreenOpen
                ) {

                    checkingPackage =
                        null

                    return@launch
                }

                // -------------------------------------------------
                // CANCEL RESET
                // -------------------------------------------------

                handler.removeCallbacks(
                    resetUnlockedAppRunnable
                )

                // -------------------------------------------------
                // MARK LOCK SCREEN OPEN
                // -------------------------------------------------

                AppLockServiceHolder.isLockScreenOpen =
                    true

                Log.d(
                    TAG,
                    "OPENING LOCK SCREEN FOR = $packageName"
                )

                // =================================================
                // START ACTIVITY
                // =================================================

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
                            "LOCK SCREEN STARTED = $packageName"
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

                    checkingPackage =
                        null
                }

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "LOCK CHECK ERROR",
                    e
                )

                AppLockServiceHolder.isLockScreenOpen =
                    false

                checkingPackage =
                    null
            }
        }
    }

    // =========================================================
    // INTERRUPT
    // =========================================================

    override fun onInterrupt() {

        Log.d(
            TAG,
            "ACCESSIBILITY SERVICE INTERRUPTED"
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
            "ACCESSIBILITY SERVICE UNBOUND"
        )

        handler.removeCallbacks(
            resetUnlockedAppRunnable
        )

        checkingPackage =
            null

        lastPackageName =
            null

        return super.onUnbind(
            intent
        )
    }

    // =========================================================
    // REBIND
    // =========================================================

    override fun onRebind(
        intent: Intent
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
            "ACCESSIBILITY SERVICE REBOUND"
        )

        Log.d(
            TAG,
            "================================"
        )

        checkingPackage =
            null

        lastPackageName =
            null

        AppLockServiceHolder.isLockScreenOpen =
            false
    }

    // =========================================================
    // DESTROY
    // =========================================================

    override fun onDestroy() {

        Log.d(
            TAG,
            "ACCESSIBILITY SERVICE DESTROYED"
        )

        handler.removeCallbacks(
            resetUnlockedAppRunnable
        )

        checkingPackage =
            null

        lastPackageName =
            null

        AppLockServiceHolder.isLockScreenOpen =
            false

        scope.cancel()

        super.onDestroy()
    }
}