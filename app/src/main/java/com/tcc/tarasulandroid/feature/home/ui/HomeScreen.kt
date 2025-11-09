package com.tcc.tarasulandroid.feature.home.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tcc.tarasulandroid.R
import com.tcc.tarasulandroid.feature.chat.ChatListScreen
import com.tcc.tarasulandroid.feature.home.ui.profile.ProfileScreen

@Composable
fun HomeScreen(
    navController: NavController
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    val tabs = listOf(
        stringResource(R.string.chats),
        stringResource(R.string.profile)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                painter = painterResource(
                                    id = when (index) {
                                        0 -> if (selectedTab == index) R.drawable.ic_chat_filled else R.drawable.ic_chat
                                        else -> if (selectedTab == index) R.drawable.ic_person else R.drawable.ic_person_outline
                                    }
                                ),
                                contentDescription = title
                            )
                        },
                        label = { Text(text = title) },
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
        Box(
            modifier = Modifier.padding(padding)
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
