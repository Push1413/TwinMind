package com.devpush.twinmind.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.devpush.twinmind.presentation.calendar.CalendarScreen
import com.devpush.twinmind.presentation.memories.MemoriesScreen
import com.devpush.twinmind.presentation.questions.QuestionsScreen

@Composable
fun BottomNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Calendar.route,
        modifier = modifier
    ) {
        composable(BottomNavItem.Memories.route) {
            MemoriesScreen()
        }
        composable(BottomNavItem.Calendar.route) {
            CalendarScreen(/*navController = navController*/) // Changed to CalendarScreen. Pass navController if CalendarScreen needs it.
        }
        composable(BottomNavItem.Questions.route) {
            QuestionsScreen()
        }
    }
}
