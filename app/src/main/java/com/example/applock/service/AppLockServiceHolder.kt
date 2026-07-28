package com.example.applock.service

object AppLockServiceHolder {

    var isLockScreenOpen = false

    // ❌ REMOVE old logic (no permanent unlock)
    // val unlockedApps = mutableSetOf<String>()

    // ✅ TEMP unlock only while screen active
    var currentUnlockedApp: String? = null
}