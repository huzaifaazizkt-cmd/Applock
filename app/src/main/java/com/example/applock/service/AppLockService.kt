package com.example.applock.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.applock.Design.screens.LockScreenActivity
import com.example.applock.data.DataStoreManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class AppLockService : AccessibilityService() {

    private val TAG = "APPLOCK_DEBUG"
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        Log.d(TAG, "App: $packageName")

        if (packageName == this.packageName) return
        if (packageName.contains("launcher")) return
        if (packageName == "com.android.systemui") return

        if (AppLockServiceHolder.isLockScreenOpen) return

        scope.launch {

            val lockedApps = DataStoreManager(this@AppLockService)
                .lockedAppsFlow.first()

            if (lockedApps.contains(packageName)
                && AppLockServiceHolder.currentUnlockedApp != packageName
            ) {

                Log.d(TAG, "LOCKING: $packageName")

                AppLockServiceHolder.isLockScreenOpen = true

                // 🔥 IMPORTANT → MAIN THREAD PE LAUNCH
                withContext(Dispatchers.Main) {

                    try {
                        val intent = Intent(this@AppLockService, LockScreenActivity::class.java)
                        intent.putExtra("packageName", packageName)

                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

                        startActivity(intent)

                    } catch (e: Exception) {
                        AppLockServiceHolder.isLockScreenOpen = false
                    }
                }
            }
        }
    }

    override fun onInterrupt() {}
}