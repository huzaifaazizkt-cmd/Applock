package com.example.applock.Design.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.applock.service.AppLockServiceHolder

class LockScreenActivity : ComponentActivity() {

    private var targetPackage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        targetPackage = intent.getStringExtra("packageName")

        setContent {
            UnlockScreen(
                onUnlockSuccess = {

                    // ✅ TEMP unlock
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

    // ❗ IMPORTANT FIX
    override fun onStop() {
        super.onStop()

        // ✅ Reset when app goes background
        AppLockServiceHolder.currentUnlockedApp = null
        AppLockServiceHolder.isLockScreenOpen = false
    }
}