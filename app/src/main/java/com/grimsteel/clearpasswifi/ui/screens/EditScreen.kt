package com.grimsteel.clearpasswifi.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grimsteel.clearpasswifi.R
import com.grimsteel.clearpasswifi.ui.MainViewModelProvider

@Composable
fun EditScreen(vm: EditViewModel = viewModel(factory = MainViewModelProvider.Factory)) {
    val network by vm.network.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = network?.displayName ?: "",
            style = MaterialTheme.typography.titleLarge
        )

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
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
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
                        IconButton(onClick = { context.startActivity(intent) }) {
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