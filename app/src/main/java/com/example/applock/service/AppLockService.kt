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

    // =========================================================
    // COROUTINE
    // =========================================================

    private val serviceJob =
        SupervisorJob()

    private val scope =
        CoroutineScope(
            Dispatchers.IO + serviceJob
        )


    // =========================================================
    // MAIN HANDLER
    // =========================================================

    private val handler =
        Handler(
            Looper.getMainLooper()
        )


    // =========================================================
    // LAST PACKAGE
    // =========================================================

    @Volatile
    private var lastPackageName: String? = null


    // =========================================================
    // RESET UNLOCKED APP
    // =========================================================

    private val resetUnlockedAppRunnable =
        Runnable {

            Log.d(
                TAG,
                "RESET UNLOCKED APP"
            )

            AppLockServiceHolder.currentUnlockedApp =
                null

            AppLockServiceHolder.lastUnlockTime =
                0L
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


        // =====================================================
        // RESET TEMP STATE
        // =====================================================

        lastPackageName =
            null

        AppLockServiceHolder.isLockScreenOpen =
            false


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

        // =====================================================
        // NULL CHECK
        // =====================================================

        if (event == null) {
            return
        }


        // =====================================================
        // ONLY WINDOW STATE CHANGED
        // =====================================================

        if (
            event.eventType !=
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        ) {
            return
        }


        // =====================================================
        // PACKAGE
        // =====================================================

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
        // LOCK SCREEN ALREADY OPEN
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
        // SAVE LAST PACKAGE
        // =====================================================

        lastPackageName =
            packageName


        // =====================================================
        // CHECK LOCKED APPS
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


                // =================================================
                // APP NOT LOCKED
                // =================================================

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


                // =================================================
                // ALREADY UNLOCKED
                // =================================================

                if (
                    AppLockServiceHolder
                        .currentUnlockedApp ==
                    packageName
                ) {

                    Log.d(
                        TAG,
                        "APP ALREADY UNLOCKED = $packageName"
                    )

                    return@launch
                }


                // =================================================
                // CANCEL OLD RESET
                // =================================================

                handler.removeCallbacks(
                    resetUnlockedAppRunnable
                )


                // =================================================
                // EXTRA PROTECTION
                // =================================================

                if (
                    AppLockServiceHolder.isLockScreenOpen
                ) {

                    Log.d(
                        TAG,
                        "LOCK SCREEN ALREADY STARTING"
                    )

                    return@launch
                }


                // =================================================
                // MARK LOCK SCREEN OPEN
                // =================================================

                AppLockServiceHolder.isLockScreenOpen =
                    true


                // =================================================
                // OPEN LOCK SCREEN
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

                AppLockServiceHolder.isLockScreenOpen =
                    false
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

        return super.onUnbind(
            intent
        )
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
            "ACCESSIBILITY SERVICE REBOUND"
        )
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


        AppLockServiceHolder.clear()


        scope.cancel()


        super.onDestroy()
    }
}