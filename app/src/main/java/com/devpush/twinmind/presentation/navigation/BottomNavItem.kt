package com.devpush.twinmind.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.ui.graphics.vector.ImageVector
import com.devpush.twinmind.R

sealed class BottomNavItem(val route: String, val title: Int, val icon: ImageVector) {

    object Memories : BottomNavItem("memories", R.string.screen_title_memories, Icons.Default.Favorite)
    object Calendar : BottomNavItem("calendar", R.string.screen_title_calendar, Icons.Default.CalendarToday)
    object Questions : BottomNavItem("questions", R.string.screen_title_questions, Icons.Default.HelpOutline)
}
