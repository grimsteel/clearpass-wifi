package com.grimsteel.clearpasswifi.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.grimsteel.clearpasswifi.MainApplication
import com.grimsteel.clearpasswifi.ui.screens.EditViewModel
import com.grimsteel.clearpasswifi.ui.screens.HomeViewModel
import com.grimsteel.clearpasswifi.ui.screens.ImportViewModel

object MainViewModelProvider {
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
                mainApplication().networkDao
            )
        }
    }
}

fun CreationExtras.mainApplication(): MainApplication = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MainApplication)