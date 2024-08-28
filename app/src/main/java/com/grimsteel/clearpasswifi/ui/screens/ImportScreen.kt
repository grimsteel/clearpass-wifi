package com.grimsteel.clearpasswifi.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grimsteel.clearpasswifi.R
import com.grimsteel.clearpasswifi.ui.theme.AppTheme

@Composable
fun ImportScreen(vm: ImportConfigureViewModel = viewModel()) {
    val uiState by vm.importScreenState.collectAsState()

    val xmlCredFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->

    }
    val quick1xConfigFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            vm.useQuick1xFile(uri)
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(16.dp)
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
                modifier = Modifier.padding(16.dp, 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.select_onboard_file)
                )
            }

            FilledTonalButton(
                onClick = { xmlCredFilePicker.launch("*/*") },
                modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 16.dp)
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
                onClick = { /*TODO*/ },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.add)
                )
            }
        }
    }
}

@Preview
@Composable
fun ImportScreenPreview() {
    AppTheme(darkTheme = true) {
        ImportScreen()
    }
}