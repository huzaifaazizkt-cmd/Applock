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


// =============================================================
// DATASTORE
// =============================================================

private val Context.dataStore by preferencesDataStore(
    name = "app_lock"
)


// =============================================================
// DATA STORE MANAGER
// =============================================================

class DataStoreManager(
    private val context: Context
) {

    companion object {

        // =====================================================
        // PIN
        // =====================================================

        val PIN_KEY =
            stringPreferencesKey(
                "pin_key"
            )


        // =====================================================
        // PATTERN
        // =====================================================

        val PATTERN_KEY =
            stringPreferencesKey(
                "pattern_key"
            )


        // =====================================================
        // AUTH TYPE
        // =====================================================

        val AUTH_TYPE_KEY =
            stringPreferencesKey(
                "auth_type"
            )


        // =====================================================
        // LOCKED APPS
        // =====================================================

        val LOCKED_APPS =
            stringSetPreferencesKey(
                "locked_apps"
            )


        // =====================================================
        // FINGERPRINT
        // =====================================================

        val FINGERPRINT_ENABLED =
            booleanPreferencesKey(
                "fingerprint_enabled"
            )


        // =====================================================
        // VIBRATION
        // =====================================================

        val VIBRATION_ENABLED =
            booleanPreferencesKey(
                "vibration_enabled"
            )


        // =====================================================
        // HIDE TRACK
        // =====================================================

        val HIDE_TRACK_ENABLED =
            booleanPreferencesKey(
                "hide_track_enabled"
            )


        // =====================================================
        // INTRUDER
        // =====================================================

        val INTRUDER_ENABLED =
            booleanPreferencesKey(
                "intruder_enabled"
            )


        val INTRUDER_WRONG_ATTEMPTS =
            intPreferencesKey(
                "intruder_wrong_attempts"
            )


        val INTRUDER_OBSERVATION_TIME =
            intPreferencesKey(
                "intruder_observation_time"
            )


        // =====================================================
        // INTRUDER PHOTOS ENABLE / DISABLE
        // =====================================================

        val INTRUDER_PHOTOS =
            booleanPreferencesKey(
                "intruder_photos"
            )


        // =====================================================
        // INTRUDER PHOTO URI LIST
        // =====================================================

        val INTRUDER_PHOTO_URIS =
            stringSetPreferencesKey(
                "intruder_photo_uris"
            )


        // =====================================================
        // SECURITY QUESTION
        // =====================================================

        val SECURITY_QUESTION =
            stringPreferencesKey(
                "security_question"
            )


        val SECURITY_ANSWER =
            stringPreferencesKey(
                "security_answer"
            )


        // =====================================================
        // HIDE FROM RECENTS
        // =====================================================

        val HIDE_FROM_RECENTS =
            booleanPreferencesKey(
                "hide_from_recents"
            )


        // =====================================================
        // LANGUAGE
        // =====================================================

        val LANGUAGE_KEY =
            stringPreferencesKey(
                "language_key"
            )


        // =====================================================
        // RELOCK OPTION
        // =====================================================

        val RELOCK_OPTION_KEY =
            stringPreferencesKey(
                "relock_option"
            )


        // =====================================================
        // RELOCK DELAY
        // =====================================================

        val RELOCK_DELAY_KEY =
            stringPreferencesKey(
                "relock_delay"
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


    fun getAuthType(): Flow<String?> {

        return context.dataStore.data.map {

            it[AUTH_TYPE_KEY]
                ?: "pin"
        }
    }


    // =========================================================
    // LOCKED APPS
    // =========================================================

    suspend fun saveLockedApps(
        apps: Set<String>
    ) {

        context.dataStore.edit {

            it[LOCKED_APPS] =
                apps
        }
    }


    // =========================================================
    // ADD ONE LOCKED APP
    // =========================================================

    suspend fun saveLockedApp(
        packageName: String
    ) {

        context.dataStore.edit {

            val currentApps =
                it[LOCKED_APPS]
                    ?: emptySet()

            it[LOCKED_APPS] =
                currentApps + packageName
        }
    }


    // =========================================================
    // REMOVE ONE LOCKED APP
    // =========================================================

    suspend fun removeLockedApp(
        packageName: String
    ) {

        context.dataStore.edit {

            val currentApps =
                it[LOCKED_APPS]
                    ?: emptySet()

            it[LOCKED_APPS] =
                currentApps - packageName
        }
    }


    // =========================================================
    // GET LOCKED APPS
    // =========================================================

    fun getLockedApps(): Flow<Set<String>> {

        return context.dataStore.data.map {

            it[LOCKED_APPS]
                ?: emptySet()
        }
    }


    // =========================================================
    // LOCKED APPS FLOW
    // =========================================================

    val lockedAppsFlow: Flow<Set<String>>
        get() =
            getLockedApps()


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
    // VIBRATION
    // =========================================================

    suspend fun saveVibrationEnabled(
        enabled: Boolean
    ) {

        context.dataStore.edit {

            it[VIBRATION_ENABLED] =
                enabled
        }
    }


    fun getVibrationEnabled(): Flow<Boolean> {

        return context.dataStore.data.map {

            it[VIBRATION_ENABLED]
                ?: false
        }
    }


    // =========================================================
    // HIDE TRACK
    // =========================================================

    suspend fun saveHideTrackEnabled(
        enabled: Boolean
    ) {

        context.dataStore.edit {

            it[HIDE_TRACK_ENABLED] =
                enabled
        }
    }


    fun getHideTrackEnabled(): Flow<Boolean> {

        return context.dataStore.data.map {

            it[HIDE_TRACK_ENABLED]
                ?: false
        }
    }


    // =========================================================
    // INTRUDER
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


    // =========================================================
    // RESET INTRUDER WRONG ATTEMPTS
    // =========================================================

    suspend fun resetIntruderWrongAttempts() {

        context.dataStore.edit {

            it[INTRUDER_WRONG_ATTEMPTS] =
                0
        }
    }


    // =========================================================
    // INTRUDER OBSERVATION TIME
    // =========================================================

    suspend fun saveIntruderObservationTime(
        seconds: Int
    ) {

        context.dataStore.edit {

            it[INTRUDER_OBSERVATION_TIME] =
                seconds
        }
    }


    fun getIntruderObservationTime(): Flow<Int> {

        return context.dataStore.data.map {

            it[INTRUDER_OBSERVATION_TIME]
                ?: 10
        }
    }


    // =========================================================
    // INTRUDER PHOTOS ENABLE / DISABLE
    // =========================================================

    suspend fun saveIntruderPhotos(
        enabled: Boolean
    ) {

        context.dataStore.edit {

            it[INTRUDER_PHOTOS] =
                enabled
        }
    }


    fun getIntruderPhotos(): Flow<Boolean> {

        return context.dataStore.data.map {

            it[INTRUDER_PHOTOS]
                ?: false
        }
    }


    // =========================================================
    // SAVE INTRUDER PHOTO URI
    // =========================================================

    suspend fun saveIntruderPhoto(
        photoUri: String
    ) {

        context.dataStore.edit {

            val currentPhotos =
                it[INTRUDER_PHOTO_URIS]
                    ?: emptySet()

            it[INTRUDER_PHOTO_URIS] =
                currentPhotos + photoUri
        }
    }


    // =========================================================
    // GET INTRUDER PHOTO URIS
    // =========================================================

    fun getIntruderPhotosUris(): Flow<Set<String>> {

        return context.dataStore.data.map {

            it[INTRUDER_PHOTO_URIS]
                ?: emptySet()
        }
    }


    // =========================================================
    // REMOVE INTRUDER PHOTO URI
    // =========================================================

    suspend fun removeIntruderPhoto(
        photoUri: String
    ) {

        context.dataStore.edit {

            val currentPhotos =
                it[INTRUDER_PHOTO_URIS]
                    ?: emptySet()

            it[INTRUDER_PHOTO_URIS] =
                currentPhotos - photoUri
        }
    }


    // =========================================================
    // CLEAR ALL INTRUDER PHOTO URIS
    // =========================================================

    suspend fun clearIntruderPhotos() {

        context.dataStore.edit {

            it[INTRUDER_PHOTO_URIS] =
                emptySet()
        }
    }


    // =========================================================
    // SECURITY QUESTION
    // =========================================================

    suspend fun saveSecurityQuestion(
        question: String
    ) {

        context.dataStore.edit {

            it[SECURITY_QUESTION] =
                question
        }
    }


    fun getSecurityQuestion(): Flow<String?> {

        return context.dataStore.data.map {

            it[SECURITY_QUESTION]
        }
    }


    // =========================================================
    // SECURITY ANSWER
    // =========================================================

    suspend fun saveSecurityAnswer(
        answer: String
    ) {

        context.dataStore.edit {

            it[SECURITY_ANSWER] =
                answer
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


    // =========================================================
    // LANGUAGE
    // =========================================================

    suspend fun saveLanguage(
        language: String
    ) {

        context.dataStore.edit {

            it[LANGUAGE_KEY] =
                language
        }
    }


    fun getLanguage(): Flow<String> {

        return context.dataStore.data.map {

            it[LANGUAGE_KEY]
                ?: "en"
        }
    }


    // =========================================================
    // RELOCK OPTION
    // =========================================================

    suspend fun saveRelockOption(
        option: String
    ) {

        context.dataStore.edit {

            it[RELOCK_OPTION_KEY] =
                option
        }
    }


    fun getRelockOption(): Flow<String> {

        return context.dataStore.data.map {

            it[RELOCK_OPTION_KEY]
                ?: "relock_after_quitting"
        }
    }


    // =========================================================
    // RELOCK DELAY
    // =========================================================

    suspend fun saveRelockDelay(
        delay: String
    ) {

        context.dataStore.edit {

            it[RELOCK_DELAY_KEY] =
                delay
        }
    }


    fun getRelockDelay(): Flow<String> {

        return context.dataStore.data.map {

            it[RELOCK_DELAY_KEY]
                ?: "never"
        }
    }
}