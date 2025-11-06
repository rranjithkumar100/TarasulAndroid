package com.tcc.tarasulandroid.feature.home.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector) {
    object Chat : BottomNavItem("home/chat", Icons.Default.Home)
    object Profile : BottomNavItem("home/profile", Icons.Default.Person)
}

val bottomNavItems = listOf(
    BottomNavItem.Chat,
    BottomNavItem.Profile,
)
