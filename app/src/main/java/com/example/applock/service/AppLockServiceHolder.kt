package com.example.applock.service

object AppLockServiceHolder {

    // LockScreenActivity currently open hai
    var isLockScreenOpen = false

    // Last unlocked app
    var currentUnlockedApp: String? = null

    // App ko unlock kiye hue last time
    var lastUnlockTime: Long = 0L
}
