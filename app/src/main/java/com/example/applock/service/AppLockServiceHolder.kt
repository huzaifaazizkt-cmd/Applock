package com.example.applock.service

object AppLockServiceHolder {

    @Volatile
    var isLockScreenOpen: Boolean = false

    @Volatile
    var currentUnlockedApp: String? = null

    @Volatile
    var lastUnlockTime: Long = 0L

    // =========================================================
    // CLEAR
    // =========================================================

    fun clear() {

        isLockScreenOpen =
            false

        currentUnlockedApp =
            null

        lastUnlockTime =
            0L
    }
}