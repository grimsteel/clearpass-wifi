package com.grimsteel.clearpasswifi.ui.screens

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grimsteel.clearpasswifi.data.Network
import com.grimsteel.clearpasswifi.data.NetworkDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditScreenState(
    val newDisplayName: String = "",
    val showEditDisplayNameModal: Boolean = false,
    val existingSuggestedNetwork: WifiNetworkSuggestion? = null
)

class EditViewModel(savedStateHandle: SavedStateHandle, private val networkDao: NetworkDao, private val wifiManager: WifiManager) : ViewModel() {
    private var _uiState = MutableStateFlow(EditScreenState())
    val uiState = _uiState.asStateFlow()

    private val networkId: String = checkNotNull(savedStateHandle.get<String>("id"))

    val network = networkDao.getNetwork(networkId)
        .filterNotNull()
        .onEach {
            refreshExistingSuggestedNetwork(it)
        }
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

    /**
     * add or remove the network suggestion depending on its current status
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun toggleNetworkSuggestion() {
        if (_uiState.value.existingSuggestedNetwork == null) {
            network.value
                ?.toWifiSuggestion()
                ?.let {
                    wifiManager.addNetworkSuggestions(listOf(it))
                }
        } else {
            wifiManager.removeNetworkSuggestions(listOf(_uiState.value.existingSuggestedNetwork))
        }
        network.value?.let { refreshExistingSuggestedNetwork(it) }
    }

    fun addSavedNetwork(context: Context) {
        // R and up -- wifi network suggestion
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            network.value?.toWifiSuggestion()?.let {
                val intent = Intent(Settings.ACTION_WIFI_ADD_NETWORKS)
                intent.putExtra(Settings.EXTRA_WIFI_NETWORK_LIST, arrayListOf(it))
                context.startActivity(intent)
            }
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) { // P and below - wifiManager.addNetwork
            // use the deprecated method
            @Suppress("DEPRECATION")
            network.value?.toWifiConfig()?.let { wifiManager.addNetwork(it) }
        } else {
            // idk what happens on Q
            Toast.makeText(context, ":(", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshExistingSuggestedNetwork(network: Network) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            _uiState.update {
                it.copy(
                    existingSuggestedNetwork = network.findExistingWifiSuggestion(wifiManager)
                )
            }
        }
    }

    suspend fun deleteNetwork() {
        network.value?.let {
            // make sure to remove the suggested network
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                wifiManager.removeNetworkSuggestions(listOf(_uiState.value.existingSuggestedNetwork))
            }
            networkDao.delete(it)
        }
    }
}