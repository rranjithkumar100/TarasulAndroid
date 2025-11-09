package com.tcc.tarasulandroid.feature.home.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tcc.tarasulandroid.feature.chat.ChatListScreen
import com.tcc.tarasulandroid.feature.home.ui.profile.ProfileScreen

@Composable
fun HomeScreen(
    navController: NavController
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    val tabs = listOf(
        BottomNavItem("Chats", Icons.Filled.Call, Icons.Filled.Call),
        BottomNavItem("Profile", Icons.Outlined.Person, Icons.Filled.Person)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                tabs.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == index) item.selectedIcon else item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(text = item.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { padding ->
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier.padding(padding)
        ) {
            when (selectedTab) {
                0 -> ChatListScreen(
                    onContactClick = { contact ->
                        navController.navigate("chat/${contact.id}/${contact.name}/${contact.isOnline}")
                    }
                )
                1 -> ProfileScreen()
            }
        }
    }
}

data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)