package com.tcc.tarasulandroid.feature.auth.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.tcc.tarasulandroid.core.designsystem.components.AppButton

@Composable
fun LoginScreen(
    navController: NavController
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Login Screen")
        AppButton(onClick = {
            navController.navigate("home") {
                popUpTo("login") {
                    inclusive = true
                }
            }
        }) {
            Text("Go to Home")
        }
    }
}