package ru.faustyu.tasktrackers.viewmodel

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object SettingsKeys {
    val LOCALE = stringPreferencesKey("locale")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val HAS_SEEN_ONBOARDING = androidx.datastore.preferences.core.booleanPreferencesKey("has_seen_onboarding")
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.dataStore

    val currentLocale: StateFlow<String> = dataStore.data
        .map { prefs -> prefs[SettingsKeys.LOCALE] ?: "system" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val currentTheme: StateFlow<String> = dataStore.data
        .map { prefs -> prefs[SettingsKeys.THEME_MODE] ?: "system" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val hasSeenOnboarding: StateFlow<Boolean?> = dataStore.data
        .map { prefs -> prefs[SettingsKeys.HAS_SEEN_ONBOARDING] ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setLocale(locale: String) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[SettingsKeys.LOCALE] = locale
            }
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[SettingsKeys.THEME_MODE] = theme
            }
        }
    }

    fun setOnboardingSeen() {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[SettingsKeys.HAS_SEEN_ONBOARDING] = true
            }
        }
    }
}
