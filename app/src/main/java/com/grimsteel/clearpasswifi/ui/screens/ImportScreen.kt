package com.grimsteel.clearpasswifi.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grimsteel.clearpasswifi.R
import com.grimsteel.clearpasswifi.ui.theme.AppTheme

@Composable
fun ImportScreen() {
    var networkUrl by rememberSaveable { mutableStateOf(("")) }
    var networkOtp by rememberSaveable { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(R.string.specify_manually))

        // prompt for network URL and OTP
        OutlinedTextField(
            value = networkUrl,
            onValueChange = { networkUrl = it },
            label = { Text(text = stringResource(R.string.network_url)) },
            singleLine = true
        )

        OutlinedTextField(
            value = networkOtp,
            onValueChange = { networkOtp = it },
            label = { Text(text = stringResource(R.string.network_otp)) },
            singleLine = true,
        )

        Text(text = stringResource(R.string.import_file))
    }
}

@Preview
@Composable
fun ImportScreenPreview() {
    AppTheme(darkTheme = true) {
        ImportScreen()
    }
}