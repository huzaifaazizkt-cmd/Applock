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

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "✅ Service Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        Log.d(TAG, "📱 App: $packageName")

        // ❌ ignore own app
        if (packageName == this.packageName) return

        // ❌ if lock screen already open
        if (AppLockServiceHolder.isLockScreenOpen) return

        scope.launch {

            val lockedApps = DataStoreManager(this@AppLockService)
                .lockedAppsFlow.first()

            // ✅ LOCK CONDITION
            if (lockedApps.contains(packageName)
                && AppLockServiceHolder.currentUnlockedApp != packageName
            ) {

                Log.d(TAG, "🚫 LOCKING: $packageName")

                AppLockServiceHolder.isLockScreenOpen = true

                val intent = Intent(this@AppLockService, LockScreenActivity::class.java)
                intent.putExtra("packageName", packageName)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "❌ Interrupted")
    }
}