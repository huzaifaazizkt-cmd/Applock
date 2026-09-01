
package com.example.applock.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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
            stringPreferencesKey(
                "pin_key"
            )


        // =========================
        // PATTERN
        // =========================

        val PATTERN_KEY =
            stringPreferencesKey(
                "pattern_key"
            )


        // =========================
        // AUTH TYPE
        // =========================

        val AUTH_TYPE_KEY =
            stringPreferencesKey(
                "auth_type"
            )


        // =========================
        // LOCKED APPS
        // =========================

        val LOCKED_APPS =
            stringSetPreferencesKey(
                "locked_apps"
            )


        // =========================
        // FINGERPRINT
        // =========================

        val FINGERPRINT_ENABLED =
            booleanPreferencesKey(
                "fingerprint_enabled"
            )


        // =========================
        // INTRUDER
        // =========================

        val INTRUDER_ENABLED =
            booleanPreferencesKey(
                "intruder_enabled"
            )


        val INTRUDER_WRONG_ATTEMPTS =
            intPreferencesKey(
                "intruder_wrong_attempts"
            )


        // =========================
        // INTRUDER OBSERVATION TIME
        // =========================

        val INTRUDER_OBSERVATION_TIME =
            stringPreferencesKey(
                "intruder_observation_time"
            )


        // =========================
        // INTRUDER PHOTOS
        // =========================

        val INTRUDER_PHOTOS =
            stringSetPreferencesKey(
                "intruder_photos"
            )


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


        // =========================
        // HIDE FROM RECENTS
        // =========================

        val HIDE_FROM_RECENTS =
            booleanPreferencesKey(
                "hide_from_recents"
            )
    }


    // =========================================================
    // PIN
    // =========================================================

    suspend fun savePin(
        pin: String
    ) {

        context.dataStore.edit {

            it[PIN_KEY] =
                pin
        }
    }


    fun getPin(): Flow<String?> {

        return context.dataStore.data.map {

            it[PIN_KEY]
        }
    }


    // =========================================================
    // PATTERN
    // =========================================================

    suspend fun savePattern(
        pattern: String
    ) {

        context.dataStore.edit {

            it[PATTERN_KEY] =
                pattern
        }
    }


    fun getPattern(): Flow<String?> {

        return context.dataStore.data.map {

            it[PATTERN_KEY]
        }
    }


    // =========================================================
    // AUTH TYPE
    // =========================================================

    suspend fun saveAuthType(
        type: String
    ) {

        context.dataStore.edit {

            it[AUTH_TYPE_KEY] =
                type
        }
    }


    fun getAuthType(): Flow<String> {

        return context.dataStore.data.map {

            it[AUTH_TYPE_KEY]
                ?: "pin"
        }
    }


    // =========================================================
    // FINGERPRINT
    // =========================================================

    suspend fun saveFingerprintEnabled(
        enabled: Boolean
    ) {

        context.dataStore.edit {

            it[FINGERPRINT_ENABLED] =
                enabled
        }
    }


    fun getFingerprintEnabled(): Flow<Boolean> {

        return context.dataStore.data.map {

            it[FINGERPRINT_ENABLED]
                ?: false
        }
    }


    // =========================================================
    // INTRUDER ENABLED
    // =========================================================

    suspend fun saveIntruderEnabled(
        enabled: Boolean
    ) {

        context.dataStore.edit {

            it[INTRUDER_ENABLED] =
                enabled
        }
    }


    fun getIntruderEnabled(): Flow<Boolean> {

        return context.dataStore.data.map {

            it[INTRUDER_ENABLED]
                ?: false
        }
    }


    // =========================================================
    // INTRUDER WRONG ATTEMPTS
    // =========================================================

    suspend fun saveIntruderWrongAttempts(
        attempts: Int
    ) {

        context.dataStore.edit {

            it[INTRUDER_WRONG_ATTEMPTS] =
                attempts
        }
    }


    fun getIntruderWrongAttempts(): Flow<Int> {

        return context.dataStore.data.map {

            it[INTRUDER_WRONG_ATTEMPTS]
                ?: 0
        }
    }


    suspend fun resetIntruderWrongAttempts() {

        context.dataStore.edit {

            it[INTRUDER_WRONG_ATTEMPTS] =
                0
        }
    }


    // =========================================================
    // OBSERVATION TIME
    // =========================================================

    suspend fun saveIntruderObservationTime(
        time: String
    ) {

        context.dataStore.edit {

            it[INTRUDER_OBSERVATION_TIME] =
                time
        }
    }


    fun getIntruderObservationTime(): Flow<String> {

        return context.dataStore.data.map {

            it[
                INTRUDER_OBSERVATION_TIME
            ]
                ?: "Immediately"
        }
    }


    // =========================================================
    // INTRUDER PHOTOS
    // =========================================================

    suspend fun saveIntruderPhoto(
        uri: String
    ) {

        context.dataStore.edit {

            val current =
                it[
                    INTRUDER_PHOTOS
                ]
                    ?: emptySet()

            it[
                INTRUDER_PHOTOS
            ] =
                current + uri
        }
    }


    fun getIntruderPhotos(): Flow<Set<String>> {

        return context.dataStore.data.map {

            it[
                INTRUDER_PHOTOS
            ]
                ?: emptySet()
        }
    }


    suspend fun removeIntruderPhoto(
        uri: String
    ) {

        context.dataStore.edit {

            val current =
                it[
                    INTRUDER_PHOTOS
                ]
                    ?: emptySet()

            it[
                INTRUDER_PHOTOS
            ] =
                current - uri
        }
    }


    // =========================================================
    // LOCKED APPS
    // =========================================================

    suspend fun saveLockedApp(
        packageName: String
    ) {

        context.dataStore.edit {

            val current =
                it[LOCKED_APPS]
                    ?: emptySet()

            it[LOCKED_APPS] =
                current + packageName
        }
    }


    suspend fun removeLockedApp(
        packageName: String
    ) {

        context.dataStore.edit {

            val current =
                it[LOCKED_APPS]
                    ?: emptySet()

            it[LOCKED_APPS] =
                current - packageName
        }
    }


    val lockedAppsFlow: Flow<Set<String>> =
        context.dataStore.data.map {

            it[LOCKED_APPS]
                ?: emptySet()
        }


    // =========================================================
    // SECURITY QUESTION
    // =========================================================

    suspend fun saveSecurityQuestion(
        question: String
    ) {

        context.dataStore.edit { preferences ->

            preferences[
                SECURITY_QUESTION
            ] =
                question
        }
    }


    suspend fun saveSecurityAnswer(
        answer: String
    ) {

        context.dataStore.edit { preferences ->

            preferences[
                SECURITY_ANSWER
            ] =
                answer
        }
    }


    fun getSecurityQuestion(): Flow<String?> {

        return context.dataStore.data.map {

            it[SECURITY_QUESTION]
        }
    }


    fun getSecurityAnswer(): Flow<String?> {

        return context.dataStore.data.map {

            it[SECURITY_ANSWER]
        }
    }


    // =========================================================
    // HIDE FROM RECENTS
    // =========================================================

    suspend fun saveHideFromRecents(
        enabled: Boolean
    ) {

        context.dataStore.edit {

            it[HIDE_FROM_RECENTS] =
                enabled
        }
    }


    fun getHideFromRecents(): Flow<Boolean> {

        return context.dataStore.data.map {

            it[HIDE_FROM_RECENTS]
                ?: false
        }
    }
}

