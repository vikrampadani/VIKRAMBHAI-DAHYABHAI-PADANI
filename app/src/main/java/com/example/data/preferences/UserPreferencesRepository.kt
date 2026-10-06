package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

enum class AppThemeMode {
    SYSTEM, LIGHT, DARK
}

data class UserPreferences(
    val language: String = "gu",
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val hapticEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val keepScreenAwake: Boolean = true,
    val defaultTarget: Int = 108,
    val onboardingCompleted: Boolean = false,
    val reminderEnabled: Boolean = false,
    val reminderTime: String = "06:00"
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("app_language")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val KEEP_SCREEN_AWAKE = booleanPreferencesKey("keep_screen_awake")
        val DEFAULT_TARGET = intPreferencesKey("default_target")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_TIME = stringPreferencesKey("reminder_time")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        val lang = preferences[Keys.LANGUAGE] ?: "gu"
        val themeStr = preferences[Keys.THEME_MODE] ?: AppThemeMode.SYSTEM.name
        val theme = runCatching { AppThemeMode.valueOf(themeStr) }.getOrDefault(AppThemeMode.SYSTEM)
        val haptic = preferences[Keys.HAPTIC_ENABLED] ?: true
        val sound = preferences[Keys.SOUND_ENABLED] ?: true
        val keepAwake = preferences[Keys.KEEP_SCREEN_AWAKE] ?: true
        val target = preferences[Keys.DEFAULT_TARGET] ?: 108
        val onboarding = preferences[Keys.ONBOARDING_COMPLETED] ?: false
        val reminder = preferences[Keys.REMINDER_ENABLED] ?: false
        val reminderTime = preferences[Keys.REMINDER_TIME] ?: "06:00"

        UserPreferences(
            language = lang,
            themeMode = theme,
            hapticEnabled = haptic,
            soundEnabled = sound,
            keepScreenAwake = keepAwake,
            defaultTarget = target,
            onboardingCompleted = onboarding,
            reminderEnabled = reminder,
            reminderTime = reminderTime
        )
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = lang }
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HAPTIC_ENABLED] = enabled }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun setKeepScreenAwake(enabled: Boolean) {
        context.dataStore.edit { it[Keys.KEEP_SCREEN_AWAKE] = enabled }
    }

    suspend fun setDefaultTarget(target: Int) {
        context.dataStore.edit { it[Keys.DEFAULT_TARGET] = target }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.REMINDER_ENABLED] = enabled }
    }

    suspend fun setReminderTime(time: String) {
        context.dataStore.edit { it[Keys.REMINDER_TIME] = time }
    }

    suspend fun resetAllPreferences() {
        context.dataStore.edit { it.clear() }
    }
}
