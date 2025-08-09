package com.grimsteel.clearpasswifi.ui

import android.content.Context
import android.net.wifi.WifiManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.grimsteel.clearpasswifi.MainApplication
import com.grimsteel.clearpasswifi.data.UserPreferencesRepository
import com.grimsteel.clearpasswifi.ui.screens.EditViewModel
import com.grimsteel.clearpasswifi.ui.screens.HomeViewModel
import com.grimsteel.clearpasswifi.ui.screens.ImportViewModel
import com.grimsteel.clearpasswifi.ui.screens.SettingsViewModel

object MainViewModelProvider {
    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")
    val Factory = viewModelFactory {

        initializer {
            ImportViewModel(
                mainApplication().networkDao
            )
        }

        initializer {
            HomeViewModel(
                mainApplication().networkDao
            )
        }

        initializer {
            EditViewModel(
                createSavedStateHandle(),
                mainApplication().networkDao,
                mainApplication().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            )
        }

        initializer {
            SettingsViewModel(
                UserPreferencesRepository(
                    mainApplication().dataStore
                )
            )
        }
    }
}

fun CreationExtras.mainApplication(): MainApplication = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MainApplication)