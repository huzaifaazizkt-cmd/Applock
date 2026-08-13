package com.example.applock.service

object AppLockServiceHolder {

    // LockScreenActivity currently open hai
    var isLockScreenOpen = false

    // Last unlocked app
    var currentUnlockedApp: String? = null


    var lastUnlockTime: Long = 0L
}
