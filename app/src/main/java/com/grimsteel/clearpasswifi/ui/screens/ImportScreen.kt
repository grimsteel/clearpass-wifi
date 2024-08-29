package com.grimsteel.clearpasswifi.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grimsteel.clearpasswifi.R
import com.grimsteel.clearpasswifi.onboard.OnboardError
import kotlinx.coroutines.launch

@Composable
fun ImportScreen(snackbar: SnackbarHostState, vm: ImportConfigureViewModel = viewModel()) {
    val uiState by vm.importScreenState.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // closure to load credentials and show any errors
    val loadCredentials = {
        coroutineScope.launch {
            try {
                vm.loadCredentials(context)
            } catch (e: OnboardError) {
                // generally a network error
                val result = snackbar.showSnackbar(
                    context.getString(R.string.network_error),
                    context.getString(R.string.details),
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    // show dialog
                    vm.updateDialogErrorMessage(e.message ?: "No message")
                }
            }
        }
    }

    val xmlCredFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) {
            vm.useXmlCredsFile(context, uri)
        }
    }
    val quick1xConfigFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            vm.useQuick1xFile(context, uri)
            loadCredentials()
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // file import
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.import_file),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp)
            )

            Button(
                onClick = { quick1xConfigFilePicker.launch("*/*") },
                modifier = Modifier.padding(16.dp, 8.dp),
                enabled = !uiState.loading
            ) {
                Text(
                    text = stringResource(R.string.select_onboard_file)
                )
            }

            FilledTonalButton(
                onClick = { xmlCredFilePicker.launch("*/*") },
                modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 16.dp),
                enabled = !uiState.loading
            ) {
                Text(
                    text = stringResource(R.string.select_xml_file)
                )
            }
        }

        // manual add
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.specify_manually),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )

            // prompt for network URL and OTP
            OutlinedTextField(
                value = uiState.networkUrl,
                onValueChange = { vm.updateNetworkUrl(it) },
                label = { Text(text = stringResource(R.string.network_url)) },
                singleLine = true,
                modifier = Modifier.padding(16.dp, 4.dp)
            )

            OutlinedTextField(
                value = uiState.networkOtp,
                onValueChange = { vm.updateNetworkOtp(it) },
                label = { Text(text = stringResource(R.string.network_otp)) },
                singleLine = true,
                modifier = Modifier.padding(16.dp, 4.dp)
            )

            Button(
                onClick = { loadCredentials() },
                modifier = Modifier.padding(16.dp),
                enabled = !uiState.loading
            ) {
                Text(
                    text = stringResource(R.string.add)
                )
            }
        }

        // if the network request failed, show a dialog with more info
        if (uiState.dialogErrorMessage.isNotEmpty()) {
            AlertDialog(
                onDismissRequest = { vm.updateDialogErrorMessage("") },
                confirmButton = {
                    TextButton(onClick = { vm.updateDialogErrorMessage("") }) {
                        Text(text = stringResource(R.string.ok))
                    }
                },
                icon = {
                    Icon(Icons.Rounded.Warning, contentDescription = stringResource(R.string.error))
                },
                title = { Text(text = stringResource(R.string.network_error)) },
                text = { Text(text = uiState.dialogErrorMessage) }
            )
        }
    }
}
