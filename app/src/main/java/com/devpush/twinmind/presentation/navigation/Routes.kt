package com.devpush.twinmind.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.QuestionAnswer

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Settings : Screen("settings")
    object Memories : Screen("memories")
    object Calendar : Screen("calendar")
    object Question : Screen("question")
    object Main : Screen("main")
}