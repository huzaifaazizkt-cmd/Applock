package com.example.applock.Design.screens

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.applock.service.AppLockServiceHolder

class LockScreenActivity : ComponentActivity() {

    private var targetPackage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔥 FORCE SHOW ON TOP (IMPORTANT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        targetPackage = intent.getStringExtra("packageName")

        AppLockServiceHolder.isLockScreenOpen = true

        setContent {
            UnlockScreen(
                onUnlockSuccess = {

                    targetPackage?.let {
                        AppLockServiceHolder.currentUnlockedApp = it
                    }

                    AppLockServiceHolder.isLockScreenOpen = false

                    openApp()
                }
            )
        }
    }

    private fun openApp() {
        try {
            targetPackage?.let { pkg ->
                val intent = packageManager.getLaunchIntentForPackage(pkg)
                intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        } catch (_: Exception) {}

        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        AppLockServiceHolder.isLockScreenOpen = false
    }
}