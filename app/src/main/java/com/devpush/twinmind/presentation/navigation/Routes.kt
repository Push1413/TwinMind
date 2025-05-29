package com.devpush.twinmind.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.QuestionAnswer

sealed class Screen(val route: String, val title: String = "", val icon: ImageVector? = null) {
    object Login : Screen("login")
    object Settings : Screen("settings")
    object Memories : Screen("memories", "Memories", Icons.Default.Photo)
    object Calendar : Screen("calendar", "Calendar", Icons.Default.CalendarToday)
    object Question : Screen("question", "Ask AI", Icons.Default.QuestionAnswer)
}