package com.grimsteel.clearpasswifi.ui.screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grimsteel.clearpasswifi.R
import com.grimsteel.clearpasswifi.onboard.CredentialParseError
import com.grimsteel.clearpasswifi.onboard.OnboardError
import com.grimsteel.clearpasswifi.ui.MainViewModelProvider
import kotlinx.coroutines.launch

@Composable
fun ImportScreen(
    snackbar: SnackbarHostState,
    navigateToEdit: (id: String) -> Unit,
    vm: ImportViewModel = viewModel(factory = MainViewModelProvider.Factory)
) {
    val uiState by vm.importScreenState.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val showSnackbar: suspend (e: Exception, message: String) -> Unit = { e, message ->
        val result = snackbar.showSnackbar(
            message,
            context.getString(R.string.details),
            duration = SnackbarDuration.Long
        )
        if (result == SnackbarResult.ActionPerformed) {
            // show dialog
            vm.updateDialogError(e)
        }
    }

    // closure to load credentials and show any errors
    val loadCredentials = {
        coroutineScope.launch {
            try {
                vm.loadCredentials(context)?.let {
                    navigateToEdit(it)
                }
            } catch (e: OnboardError) {
                showSnackbar(e, context.getString(R.string.network_error))
            } catch (e: CredentialParseError) {
                showSnackbar(e, context.getString(R.string.parse_error))
            }
        }
    }

    // file pickers and savers
    val xmlCredFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    vm.useXmlCredentialsFile(context, uri)?.let {
                        navigateToEdit(it)
                    }
                } catch (e: CredentialParseError) {
                    showSnackbar(e, context.getString(R.string.parse_error))
                }
            }
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
    val errorFileSaver = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            // save file
            coroutineScope.launch {
                try {
                    vm.saveErrorMessage(context, uri)
                    vm.updateDialogError(null)
                    snackbar.showSnackbar(
                        "Error message saved successfully",
                    )
                } catch (e: Exception) {
                    Log.e("ImportScreen", "error message save error", e)
                    snackbar.showSnackbar(
                        "An error occurred while saving the error message",
                    )
                }
            }
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
        if (uiState.dialogError != null) {
            Dialog (
                onDismissRequest = { vm.updateDialogError(null) }
            ) {
                Card (
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            Icons.Rounded.Warning,
                            contentDescription = stringResource(R.string.error),
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        Text(
                            text = uiState.dialogError?.message ?: "No message",
                            modifier = Modifier.padding(24.dp, 16.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            TextButton(
                                onClick = {  errorFileSaver.launch(context.getString(R.string.error_message_file)) },
                                modifier = Modifier.padding(8.dp),
                            ) {
                                Text("Download Error")
                            }
                            TextButton(
                                onClick = { vm.updateDialogError(null) },
                                modifier = Modifier.padding(8.dp),
                            ) {
                                Text("OK")
                            }
                        }
                    }
                }
            }
        }
    }
}
