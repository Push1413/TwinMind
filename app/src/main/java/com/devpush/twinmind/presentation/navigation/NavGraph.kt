package com.devpush.twinmind.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.devpush.twinmind.presentation.auth.LoginScreen
import com.devpush.twinmind.presentation.setting.SettingsScreen

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    isUserLoggedIn: Boolean
) {
    val startDestination = if (isUserLoggedIn) Screen.Settings.route else Screen.Login.route
    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {

        composable(Screen.Login.route) {
            LoginScreen(navController)
        }

//        composable(Screen.Calendar.route) {
//            CalendarScreen(navController)
//        }
//
//        composable(Screen.Transcription.route) {
//            TranscriptionScreen(navController)
//        }
//
//        composable(Screen.Chat.route) {
//            ChatScreen(navController)
//        }
//
//        composable(Screen.Summary.route) {
//            SummaryScreen(navController)
//        }

        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }
    }
}