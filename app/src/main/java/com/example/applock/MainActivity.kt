package com.example.applock

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.applock.navigation.NavGraph

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val openResetPassword = intent.getBooleanExtra("openResetPassword", false)

        setContent {
            NavGraph(
                context = this,
                startDestination = if (openResetPassword) {
                    "resetCreate"
                } else {
                    "startScreen"
                }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        val openResetPassword = intent.getBooleanExtra("openResetPassword", false)

        setContent {
            NavGraph(
                context = this,
                startDestination = if (openResetPassword) {
                    "resetCreate"
                } else {
                    "unlockScreen"
                }
            )
        }
    }
}