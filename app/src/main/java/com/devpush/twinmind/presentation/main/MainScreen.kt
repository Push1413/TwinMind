package com.devpush.twinmind.presentation.main

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.devpush.twinmind.presentation.navigation.BottomNavGraph
import com.devpush.twinmind.presentation.navigation.BottomNavItem

@OptIn(ExperimentalMaterial3Api::class) // For Scaffold
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter") // Common for basic Scaffold examples
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    // Assuming BottomNavItem.values() is not available for sealed classes directly in this manner,
    // define the list explicitly.
    val bottomNavItems = listOf(
        BottomNavItem.Memories,
        BottomNavItem.Calendar,
        BottomNavItem.Questions
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = stringResource(id = screen.title),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        label = { Text(stringResource(id = screen.title)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
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
        // Pass the innerPadding to BottomNavGraph and apply it to the NavHost's modifier
        BottomNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
