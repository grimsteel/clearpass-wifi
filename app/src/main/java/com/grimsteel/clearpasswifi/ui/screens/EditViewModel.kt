package com.grimsteel.clearpasswifi.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grimsteel.clearpasswifi.data.NetworkDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditScreenState(
    val newDisplayName: String = "",
    val showEditDisplayNameModal: Boolean = false
)

class EditViewModel(savedStateHandle: SavedStateHandle, private val networkDao: NetworkDao) : ViewModel() {
    private var _uiState = MutableStateFlow(EditScreenState())
    val uiState = _uiState.asStateFlow()

    private val networkId: String = checkNotNull(savedStateHandle.get<String>("id"))

    val network = networkDao.getNetwork(networkId)
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateNewDisplayName(name: String) {
        _uiState.update { it.copy(newDisplayName = name) }
    }

    fun showEditDisplayNameModal() {
        _uiState.update {
            it.copy(
                newDisplayName = network.value?.displayName ?: "",
                showEditDisplayNameModal = true
            )
        }
    }

    fun closeDisplayNameModal(save: Boolean = true) {
        _uiState.update {
            it.copy(
                showEditDisplayNameModal = false
            )
        }
        if (save) {
            // save the edited network to the database
            network.value?.let {
                viewModelScope.launch {
                    networkDao.updateNetwork(it.copy(displayName = _uiState.value.newDisplayName))
                }
            }
        }
    }
}