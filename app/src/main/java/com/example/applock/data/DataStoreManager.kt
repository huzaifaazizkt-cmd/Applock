package com.example.applock.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "app_lock")

class DataStoreManager(private val context: Context) {

    companion object {
        val PIN_KEY = stringPreferencesKey("pin_key")
        val LOCKED_APPS = stringSetPreferencesKey("locked_apps")
    }

    // 🔐 Save PIN
    suspend fun savePin(pin: String) {
        context.dataStore.edit {
            it[PIN_KEY] = pin
        }
    }

    fun getPin(): Flow<String?> {
        return context.dataStore.data.map {
            it[PIN_KEY]
        }
    }

    // 🔒 Save Locked App
    suspend fun saveLockedApp(packageName: String) {
        context.dataStore.edit {
            val current = it[LOCKED_APPS] ?: emptySet()
            it[LOCKED_APPS] = current + packageName
        }
    }

    // 🔓 Remove Locked App
    suspend fun removeLockedApp(packageName: String) {
        context.dataStore.edit {
            val current = it[LOCKED_APPS] ?: emptySet()
            it[LOCKED_APPS] = current - packageName
        }
    }

    // 📦 Get Locked Apps
    val lockedAppsFlow: Flow<Set<String>> =
        context.dataStore.data.map {
            it[LOCKED_APPS] ?: emptySet()
        }
}