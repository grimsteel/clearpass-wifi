package com.grimsteel.clearpasswifi.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
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
import com.grimsteel.clearpasswifi.ui.MainViewModelProvider

@Composable
fun EditScreen(vm: EditViewModel = viewModel(factory = MainViewModelProvider.Factory)) {
    val network by vm.network.collectAsState()
    val uiState by vm.uiState.collectAsState()
    val context = LocalContext.current

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
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // network name + edit button
        Row(
            modifier = Modifier
                .padding(0.dp, 0.dp, 0.dp, 8.dp)
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
    }
}