package com.tcc.tarasulandroid

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tcc.tarasulandroid.feature.chat.ChatScreen
import com.tcc.tarasulandroid.feature.chat.ProfileInfoScreen
import com.tcc.tarasulandroid.feature.contacts.ContactListScreen
import com.tcc.tarasulandroid.feature.home.model.Contact
import com.tcc.tarasulandroid.feature.home.ui.HomeScreen
import com.tcc.tarasulandroid.feature.image.ImagePreviewScreen
import com.tcc.tarasulandroid.feature.login.LoginScreen

@Composable
fun NavGraph(
    startDestination: String = "login",
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable("login") {
            LoginScreen(navController = navController)
        }
        
        composable("home") {
            HomeScreen(navController = navController)
        }
        
        composable(
            route = "chat/{contactId}/{contactName}/{isOnline}",
            arguments = listOf(
                navArgument("contactId") { type = NavType.StringType },
                navArgument("contactName") { type = NavType.StringType },
                navArgument("isOnline") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId") ?: ""
            val contactName = backStackEntry.arguments?.getString("contactName") ?: ""
            val isOnline = backStackEntry.arguments?.getBoolean("isOnline") ?: false
            
            val contact = Contact(
                id = contactId,
                name = contactName,
                isOnline = isOnline
            )
            
            ChatScreen(
                contact = contact,
                onBackClick = { navController.popBackStack() },
                onProfileClick = {
                    navController.navigate("profile_info/${contactId}/${contactName}/${isOnline}")
                },
                onImageClick = { imagePath ->
                    val encodedPath = Uri.encode(imagePath)
                    navController.navigate("image_preview/$encodedPath")
                }
            )
        }
        
        composable(
            route = "profile_info/{contactId}/{contactName}/{isOnline}",
            arguments = listOf(
                navArgument("contactId") { type = NavType.StringType },
                navArgument("contactName") { type = NavType.StringType },
                navArgument("isOnline") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId") ?: ""
            val contactName = backStackEntry.arguments?.getString("contactName") ?: ""
            val isOnline = backStackEntry.arguments?.getBoolean("isOnline") ?: false
            
            val contact = Contact(
                id = contactId,
                name = contactName,
                isOnline = isOnline
            )
            
            ProfileInfoScreen(
                contact = contact,
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable("contacts") {
            ContactListScreen(navController = navController)
        }
        
        // Image Preview with zoom and gestures
        composable(
            route = "image_preview/{imagePath}",
            arguments = listOf(
                navArgument("imagePath") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedPath = backStackEntry.arguments?.getString("imagePath") ?: ""
            val imagePath = Uri.decode(encodedPath)
            
            ImagePreviewScreen(
                imagePath = imagePath,
                onDismiss = { navController.popBackStack() }
            )
        }
    }
}
