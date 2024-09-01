package com.grimsteel.clearpasswifi.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grimsteel.clearpasswifi.data.NetworkDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn

class EditViewModel(savedStateHandle: SavedStateHandle, private val networkDao: NetworkDao) : ViewModel() {
    private val networkId: String = checkNotNull(savedStateHandle.get<String>("id"))

    val network = networkDao.getNetwork(networkId)
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}