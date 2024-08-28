package com.grimsteel.clearpasswifi.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.grimsteel.clearpasswifi.R
import com.grimsteel.clearpasswifi.ui.screens.*
import com.grimsteel.clearpasswifi.ui.theme.AppTheme

enum class NavDestination(val id: String) {
    Home("home"),
    Import("import"),
    Edit("edit")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout() {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack?.destination
    val currentScreen = NavDestination.entries.find { it.id == currentDestination?.route }
    val snackbarHostState = remember { SnackbarHostState() }

    // figure out what t shows up in the app bar
    val title = when (currentScreen) {
        NavDestination.Home -> R.string.app_name
        NavDestination.Import -> R.string.add_config
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
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentScreen == NavDestination.Home) {
                FloatingActionButton(onClick = { navController.navigate(NavDestination.Import.id) }) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add")
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
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
                ImportScreen(snackbarHostState)
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