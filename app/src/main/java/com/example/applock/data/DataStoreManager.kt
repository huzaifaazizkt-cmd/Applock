package com.example.applock.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(
    name = "app_lock"
)

class DataStoreManager(
    private val context: Context
) {

    companion object {

        // =========================
        // PIN
        // =========================

        val PIN_KEY =
            stringPreferencesKey("pin_key")

        // =========================
        // PATTERN
        // =========================

        val PATTERN_KEY =
            stringPreferencesKey("pattern_key")

        // =========================
        // AUTH TYPE
        // =========================

        val AUTH_TYPE_KEY =
            stringPreferencesKey("auth_type")

        // =========================
        // LOCKED APPS
        // =========================

        val LOCKED_APPS =
            stringSetPreferencesKey("locked_apps")

        // =========================
        // FINGERPRINT
        // =========================

        val FINGERPRINT_ENABLED =
            booleanPreferencesKey("fingerprint_enabled")

        // =========================
        // SECURITY QUESTION
        // =========================

        val SECURITY_QUESTION =
            stringPreferencesKey(
                "security_question"
            )

        // =========================
        // SECURITY ANSWER
        // =========================

        val SECURITY_ANSWER =
            stringPreferencesKey(
                "security_answer"
            )
    }

    // =================================================
    // PIN
    // =================================================

    suspend fun savePin(
        pin: String
    ) {

        context.dataStore.edit {
            it[PIN_KEY] = pin
        }
    }

    fun getPin(): Flow<String?> {

        return context.dataStore.data.map {
            it[PIN_KEY]
        }
    }

    // =================================================
    // PATTERN
    // =================================================

    suspend fun savePattern(
        pattern: String
    ) {

        context.dataStore.edit {
            it[PATTERN_KEY] = pattern
        }
    }

    fun getPattern(): Flow<String?> {

        return context.dataStore.data.map {
            it[PATTERN_KEY]
        }
    }

    // =================================================
    // AUTH TYPE
    // =================================================

    suspend fun saveAuthType(
        type: String
    ) {

        context.dataStore.edit {
            it[AUTH_TYPE_KEY] = type
        }
    }

    fun getAuthType(): Flow<String> {

        return context.dataStore.data.map {
            it[AUTH_TYPE_KEY] ?: "pin"
        }
    }

    // =================================================
    // FINGERPRINT
    // =================================================

    suspend fun saveFingerprintEnabled(
        enabled: Boolean
    ) {

        context.dataStore.edit {
            it[FINGERPRINT_ENABLED] = enabled
        }
    }

    fun getFingerprintEnabled(): Flow<Boolean> {

        return context.dataStore.data.map {
            it[FINGERPRINT_ENABLED] ?: false
        }
    }

    // =================================================
    // LOCKED APPS
    // =================================================

    suspend fun saveLockedApp(
        packageName: String
    ) {

        context.dataStore.edit {

            val current =
                it[LOCKED_APPS] ?: emptySet()

            it[LOCKED_APPS] =
                current + packageName
        }
    }

    suspend fun removeLockedApp(
        packageName: String
    ) {

        context.dataStore.edit {

            val current =
                it[LOCKED_APPS] ?: emptySet()

            it[LOCKED_APPS] =
                current - packageName
        }
    }

    val lockedAppsFlow: Flow<Set<String>> =
        context.dataStore.data.map {

            it[LOCKED_APPS] ?: emptySet()
        }

    // =================================================
    // SECURITY QUESTION
    // =================================================

    suspend fun saveSecurityQuestion(
        question: String
    ) {

        context.dataStore.edit { preferences ->

            preferences[
                SECURITY_QUESTION
            ] = question
        }
    }

    // =================================================
    // SECURITY ANSWER
    // =================================================

    suspend fun saveSecurityAnswer(
        answer: String
    ) {

        context.dataStore.edit { preferences ->

            preferences[
                SECURITY_ANSWER
            ] = answer
        }
    }

    // =================================================
    // GET SECURITY QUESTION
    // =================================================

    fun getSecurityQuestion(): Flow<String?> {

        return context.dataStore.data.map {
            it[SECURITY_QUESTION]
        }
    }

    // =================================================
    // GET SECURITY ANSWER
    // =================================================

    fun getSecurityAnswer(): Flow<String?> {

        return context.dataStore.data.map {
            it[SECURITY_ANSWER]
        }
    }
}