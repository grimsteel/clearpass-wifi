package com.grimsteel.clearpasswifi.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grimsteel.clearpasswifi.data.LogManager
import com.grimsteel.clearpasswifi.data.UserPreferences
import com.grimsteel.clearpasswifi.data.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val prefsRepository: UserPreferencesRepository, private val logManager: LogManager) : ViewModel() {
    val prefs = prefsRepository.prefsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),
        UserPreferences(false))

    fun enableDebugLogging(enable: Boolean) {
        viewModelScope.launch {
            prefsRepository.updateEnableDebugLogging(enable)
            logManager.log("SettingsViewModel", "set enableDebugLogging = $enable")
        }
    }
}