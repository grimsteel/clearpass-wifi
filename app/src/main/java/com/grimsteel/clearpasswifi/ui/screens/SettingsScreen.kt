package com.grimsteel.clearpasswifi.ui.screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grimsteel.clearpasswifi.ui.MainViewModelProvider
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(snackbar: SnackbarHostState, viewModel: SettingsViewModel = viewModel(factory = MainViewModelProvider.Factory)) {
    val prefs by viewModel.prefs.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val debugLogSaver = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            // save file
            coroutineScope.launch {
                try {
                    viewModel.saveDebugLogs(uri)
                    snackbar.showSnackbar(
                        "Debug logs saved successfully",
                    )
                } catch (e: Exception) {
                    Log.e("SettingsScreen", "debug log download error", e)
                    snackbar.showSnackbar(
                        "An error occurred while saving the debug logs",
                    )
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // debug logging
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Enable debug logging",
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = prefs.enableDebugLogging,
                onCheckedChange = {
                    coroutineScope.launch {
                        viewModel.enableDebugLogging(it)
                    }
                }
            )
        }

        // download logs
        if (prefs.enableDebugLogging)
            FilledTonalButton (onClick = {
                coroutineScope.launch {
                    debugLogSaver.launch("clearpass-wifi-debug-logs.txt")
                }
            }) {
                Text(text = "Save debug logs")
            }
    }
}