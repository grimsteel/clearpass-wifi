package com.grimsteel.clearpasswifi.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grimsteel.clearpasswifi.data.NetworkDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(networkDao: NetworkDao) : ViewModel() {
    val networks = networkDao.getAllNetworks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf())
}