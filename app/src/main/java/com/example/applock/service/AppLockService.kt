package com.example.applock.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.applock.Design.screens.LockScreenActivity
import com.example.applock.data.DataStoreManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class AppLockService : AccessibilityService() {

    private val scope = CoroutineScope(Dispatchers.IO)

    private var lastApp = ""

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        if (packageName == this.packageName) return

        if (packageName == lastApp) return
        lastApp = packageName

        // 🔥 LOOP FIX
        if (AppLockServiceHolder.isLockScreenOpen) return

        scope.launch {

            val lockedApps = DataStoreManager(this@AppLockService)
                .lockedAppsFlow.first()

            if (lockedApps.contains(packageName)) {

                AppLockServiceHolder.isLockScreenOpen = true

                val intent = Intent(this@AppLockService, LockScreenActivity::class.java)
                intent.putExtra("packageName", packageName)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {}
}