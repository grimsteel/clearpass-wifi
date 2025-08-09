package com.grimsteel.clearpasswifi.ui

import android.content.Context
import android.net.wifi.WifiManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.grimsteel.clearpasswifi.MainApplication
import com.grimsteel.clearpasswifi.ui.screens.EditViewModel
import com.grimsteel.clearpasswifi.ui.screens.HomeViewModel
import com.grimsteel.clearpasswifi.ui.screens.ImportViewModel
import com.grimsteel.clearpasswifi.ui.screens.SettingsViewModel

object MainViewModelProvider {
    val Factory = viewModelFactory {

        initializer {
            ImportViewModel(
                mainApplication().networkDao,
                mainApplication().logger
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
            SettingsViewModel(mainApplication().prefs, mainApplication().logger)
        }
    }
}

fun CreationExtras.mainApplication(): MainApplication = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MainApplication)