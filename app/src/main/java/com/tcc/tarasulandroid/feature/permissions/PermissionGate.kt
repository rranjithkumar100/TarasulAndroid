package com.tcc.tarasulandroid.feature.permissions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun PermissionGate(
    permissions: List<String>,
    rationale: @Composable (request: () -> Unit) -> Unit,
    content: @Composable (isPartialAccess: Boolean) -> Unit
) {
    val context = LocalContext.current

    val getPermissionsState = {
        val allGranted = permissions.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
        if (allGranted) {
            "granted"
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            (permissions.contains(Manifest.permission.READ_MEDIA_IMAGES) || permissions.contains(Manifest.permission.READ_MEDIA_VIDEO)) &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED) {
            "partial"
        } else {
            "denied"
        }
    }

    var permissionState by remember { mutableStateOf(getPermissionsState()) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {
            permissionState = getPermissionsState()
        }
    )

    when (permissionState) {
        "granted" -> content(false)
        "partial" -> content(true)
        else -> {
            rationale {
                launcher.launch(permissions.toTypedArray())
            }
        }
    }
}
