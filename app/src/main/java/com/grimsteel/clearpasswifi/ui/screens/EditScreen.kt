package com.grimsteel.clearpasswifi.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiEnterpriseConfig
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grimsteel.clearpasswifi.R
import com.grimsteel.clearpasswifi.data.Network
import com.grimsteel.clearpasswifi.data.WpaMethod
import com.grimsteel.clearpasswifi.ui.MainViewModelProvider
import java.security.KeyStore
import java.security.KeyStore.PrivateKeyEntry
import java.text.DateFormat
import java.text.SimpleDateFormat
import javax.security.auth.x500.X500Principal

@Composable
fun EditScreen(vm: EditViewModel = viewModel(factory = MainViewModelProvider.Factory)) {
    val dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM)

    val network by vm.network.collectAsState()
    val uiState by vm.uiState.collectAsState()
    val context = LocalContext.current

    // edit display name modal
    if (uiState.showEditDisplayNameModal) {
        Dialog(onDismissRequest = { vm.closeDisplayNameModal(false) }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    //.height(200.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.edit_display_name),
                        modifier = Modifier
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    OutlinedTextField(value = uiState.newDisplayName, onValueChange = { vm.updateNewDisplayName(it) })
                    // action buttons
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { vm.closeDisplayNameModal(true) }) {
                            Text(
                                text = stringResource(R.string.ok)
                            )
                        }
                        TextButton(onClick = { vm.closeDisplayNameModal(false) }) {
                            Text(
                                text = stringResource(R.string.cancel)
                            )
                        }
                    }
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
        // network name + edit button
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = network?.displayName ?: "",
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(onClick = { vm.showEditDisplayNameModal() }) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = stringResource(R.string.edit)
                )
            }
        }

        // organization card
        network?.organization?.let {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp, 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.round_organization_24),
                        contentDescription = stringResource(R.string.organization)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.managed_by),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = it.name
                        )
                    }
                    if (it.landingPage != null) {
                        // open the landing page when they click the button
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.landingPage))
                        IconButton(
                            onClick = { context.startActivity(intent) },
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.round_open_in_new_24),
                                contentDescription = stringResource(R.string.open_landing_page)
                            )
                        }
                    }
                }
            }
        }

        network?.createdAt?.let {
            Text(
                text = stringResource(R.string.created_on, ""),
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = dateFormat.format(it)
            )
            HorizontalDivider()
        }

        network?.wpaMethod?.let {
            // WPA method
            Text(
                text = stringResource(R.string.wpa_method),
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = when (it) {
                    WpaMethod.EapTls -> stringResource(R.string.eap_tls)
                }
            )
            HorizontalDivider()
        }

        // Identity
        network?.identity?.let {
            Text(
                text = stringResource(R.string.eap_identity),
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = it
            )
            HorizontalDivider()
        }

        // CA cert
        network?.caCertificate?.let {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.ca_certificate),
                        style = MaterialTheme.typography.labelMedium
                    )
                    val cn = it.subjectX500Principal.name
                        .split(",")
                        .map { it.split("=") }
                        .find { it[0] == "CN" };

                    Text(
                        text = cn?.get(1) ?: stringResource(R.string.unknown_cn),
                        modifier = Modifier.wrapContentHeight()
                    )
                }
                IconButton(
                    onClick = { /* TODO: download certificate */ }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.round_download_24),
                        contentDescription = stringResource(R.string.download)
                    )
                }
            }
            HorizontalDivider()
        }

        // Client cert
        network?.clientCertificate?.let {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.client_certificate),
                        style = MaterialTheme.typography.labelMedium
                    )

                    val cn = it.subjectX500Principal.name
                        .split(",")
                        .map { it.split("=") }
                        .find { it[0] == "CN" }

                    Text(
                        text = cn?.get(1) ?: stringResource(R.string.unknown_cn)
                    )
                }
                IconButton(
                    onClick = { /* TODO: download certificate */ }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.round_download_24),
                        contentDescription = stringResource(R.string.download)
                    )
                }
            }
            HorizontalDivider()

            // private key
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.private_key),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = stringResource(R.string.secure_device_storage),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                IconButton(
                    onClick = { /* TODO: download certificate */ }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.round_download_24),
                        contentDescription = stringResource(R.string.download)
                    )
                }
            }
            HorizontalDivider()
        }

        // button to add network
        Button(onClick = {
            network?.let {
                val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
                    load(null)
                }
                val pkEntry = keyStore.getEntry(
                    Network.EAP_TLS_PK_ALIAS.format(it.id),
                    // TODO: use actual protection
                    null
                ) as? PrivateKeyEntry

                val pk = pkEntry?.privateKey

                if (pk != null && it.clientCertificate != null) {
                    val eapConfig = WifiEnterpriseConfig().apply {
                        identity = it.identity
                        domainSuffixMatch = it.domainSuffixMatch
                        caCertificate = it.caCertificate
                        eapMethod = when (it.wpaMethod) {
                            WpaMethod.EapTls -> WifiEnterpriseConfig.Eap.TLS
                        }
                        setClientKeyEntry(pk, it.clientCertificate)
                    }
                    val suggestion = WifiNetworkSuggestion.Builder()
                        .setSsid(it.ssid)
                        .setMacRandomizationSetting(WifiNetworkSuggestion.RANDOMIZATION_PERSISTENT)
                        .setWpa2EnterpriseConfig(eapConfig)
                        .build()

                    val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                    wifiManager.addNetworkSuggestions(listOf(suggestion))
                }
            }
        }) {
            Text(text = "Add Wi-Fi network suggestion")
        }
        FilledTonalButton(onClick = { /*TODO*/ }) {
            Text(text = "Add to your Wi-Fi networks")
        }
    }
}
