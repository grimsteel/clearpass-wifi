package com.grimsteel.clearpasswifi.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.grimsteel.clearpasswifi.R
import com.grimsteel.clearpasswifi.ui.screens.*
import com.grimsteel.clearpasswifi.ui.theme.AppTheme

enum class NavDestination(val id: String) {
    Home("home"),
    Import("import"),
    Configuring("configuring"),
    Edit("edit")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout() {
    val currentScreen: NavDestination by remember { mutableStateOf(NavDestination.Home) }
    val navController = rememberNavController()

    // figure out what t shows up in the app bar
    val title = when (currentScreen) {
        NavDestination.Home -> R.string.app_name
        NavDestination.Import -> R.string.add_config
        NavDestination.Configuring -> R.string.configuring
        else -> R.string.edit
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(title)) },
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            if (currentScreen == NavDestination.Home) {
                FloatingActionButton(onClick = { navController.navigate(NavDestination.Import.id) }) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavDestination.Home.id,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = NavDestination.Home.id) {
                HomeScreen()
            }
            composable(route = NavDestination.Edit.id) {
                EditScreen()
            }
            composable(route = NavDestination.Import.id) {
                ImportScreen()
            }
            composable(route = NavDestination.Configuring.id) {
                ConfiguringScreen()
            }
        }
    }
}

@Composable
@Preview
fun MainLayoutPreview() {
    AppTheme(darkTheme = true) {
        MainLayout()
    }
}