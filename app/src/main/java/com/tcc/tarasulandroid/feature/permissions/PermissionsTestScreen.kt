package com.tcc.tarasulandroid.feature.permissions

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PermissionsTestScreen(modifier: Modifier = Modifier) {
    var permissionsToRequest by remember { mutableStateOf<List<String>?>(null) }
    var permissionResult by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.padding(16.dp)) {
        if (permissionResult != null) {
            Text(permissionResult!!)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(onClick = { permissionsToRequest = listOf(Manifest.permission.CAMERA) }) {
            Text("Request Camera")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { permissionsToRequest = listOf(Manifest.permission.RECORD_AUDIO) }) {
            Text("Request Audio")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { permissionsToRequest = listOf(Manifest.permission.READ_CONTACTS) }) {
            Text("Request Contacts")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                val permissions = mutableListOf<String>()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                    permissions.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                } else {
                    permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                permissionsToRequest = permissions
            }
        ) {
            Text("Request Read Images")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                val permissions = mutableListOf<String>()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
                    permissions.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
                } else {
                    permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                permissionsToRequest = permissions
            }
        ) {
            Text("Request Read Video")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionsToRequest = listOf(Manifest.permission.READ_MEDIA_AUDIO)
                } else {
                    permissionsToRequest = listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        ) {
            Text("Request Read Audio")
        }

        permissionsToRequest?.let { permissions ->
            PermissionGate(
                permissions = permissions,
                rationale = { request ->
                    Column {
                        Text("The requested permissions are needed for this feature.")
                        Button(onClick = {
                            request()
                            permissionsToRequest = null
                        }) {
                            Text("Request")
                        }
                    }
                }
            ) { isPartialAccess ->
                LaunchedEffect(permissions, isPartialAccess) {
                    permissionResult = if (isPartialAccess) {
                        "Partial access granted for ${permissions.joinToString()}"
                    } else {
                        "Full access granted for ${permissions.joinToString()}"
                    }
                    permissionsToRequest = null
                }
            }
        }
    }
}
