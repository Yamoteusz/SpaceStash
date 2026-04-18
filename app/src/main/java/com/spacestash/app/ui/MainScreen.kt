package com.spacestash.app.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person // Nowy import dla ikonki społeczności!
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

// Zmienione opcje menu: Dodano Społeczność!
sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    object Stash : BottomNavItem("stash", "Schowek", Icons.Default.Star)
    object Map : BottomNavItem("map", "Mapa", Icons.Default.LocationOn)
    object Community : BottomNavItem("community", "Społeczność", Icons.Default.Person) // NOWE
    object Contact : BottomNavItem("contact", "Kontakt", Icons.Default.Email)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    // Stan szuflady (otwarta/zamknięta)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    // Scope potrzebny do uruchamiania animacji wysuwania szuflady
    val scope = rememberCoroutineScope()

    // Aktualna lista zakładek (dodano Społeczność)
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Stash,
        BottomNavItem.Map,
        BottomNavItem.Community, // NOWE
        BottomNavItem.Contact
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // ModalNavigationDrawer otacza całą naszą aplikację
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // Wygląd samego wysuwanego menu
            ModalDrawerSheet {
                Spacer(Modifier.height(16.dp))
                Text(text = "SpaceStash Menu", modifier = Modifier.padding(16.dp))
                Spacer(Modifier.height(8.dp))

                items.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(text = item.title) },
                        selected = currentRoute == item.route,
                        onClick = {
                            scope.launch { drawerState.close() } // Zamyka menu po kliknięciu
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                // Górny pasek z ikoną do otwierania bocznego menu
                TopAppBar(
                    title = { Text("SpaceStash") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Otwórz menu")
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = BottomNavItem.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                // Uaktualnione i uporządkowane ścieżki
                composable(BottomNavItem.Home.route) { HomeScreen() }
                composable(BottomNavItem.Stash.route) { StashScreen() }
                composable(BottomNavItem.Map.route) { MapScreen() }
                composable(BottomNavItem.Community.route) { CommunityScreen() } // Zmienione z surowego "community"
                composable(BottomNavItem.Contact.route) { ContactScreen() }
            }
        }
    }
}