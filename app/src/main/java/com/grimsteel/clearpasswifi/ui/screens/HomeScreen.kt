package com.grimsteel.clearpasswifi.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grimsteel.clearpasswifi.R
import com.grimsteel.clearpasswifi.ui.MainViewModelProvider
import com.grimsteel.clearpasswifi.ui.theme.AppTheme
import java.text.DateFormat
import java.text.SimpleDateFormat

@Composable
fun HomeScreen(navigateToEdit: (id: String) -> Unit, vm: HomeViewModel = viewModel(factory = MainViewModelProvider.Factory)) {
    val networks by vm.networks.collectAsState()
    val dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)

    if (networks.isNotEmpty()) {
        // list of networks
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(16.dp)
        ) {
            item {
                Text(
                    style = MaterialTheme.typography.titleLarge,
                    text = stringResource(R.string.saved_networks)
                )
            }
            items(items = networks, key = { it.id }) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        navigateToEdit(it.id)
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp, 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // display name + created date
                        Text(text = it.displayName, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = stringResource(
                                R.string.created_on,
                                dateFormat.format(it.createdAt)
                            ), style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    } else {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(8.dp)
        ) {
            Text(
                style = MaterialTheme.typography.titleLarge,
                text = stringResource(R.string.no_networks_configured)
            )
            Text(
                style = MaterialTheme.typography.bodyLarge,
                text = stringResource(R.string.click_plus_add)
            )
        }
    }
}