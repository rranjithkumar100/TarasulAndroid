package com.tcc.tarasulandroid.core

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Composable to handle runtime permissions
 */
@Composable
fun rememberPermissionState(
    permission: String,
    onPermissionResult: (Boolean) -> Unit = {}
): PermissionState {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        onPermissionResult(isGranted)
    }
    
    return remember(permission) {
        PermissionState(
            permission = permission,
            hasPermission = hasPermission,
            requestPermission = { launcher.launch(permission) }
        )
    }
}

/**
 * Composable to handle multiple runtime permissions
 */
@Composable
fun rememberMultiplePermissionsState(
    permissions: List<String>,
    onPermissionsResult: (Map<String, Boolean>) -> Unit = {}
): MultiplePermissionsState {
    val context = LocalContext.current
    var permissionsStatus by remember {
        mutableStateOf(
            permissions.associateWith { permission ->
                ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            }
        )
    }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        // Update the permission status with actual results from the system
        val updatedStatus = permissions.associateWith { permission ->
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
        permissionsStatus = updatedStatus
        onPermissionsResult(updatedStatus)
    }
    
    // Check permissions on every recomposition to catch external grants
    LaunchedEffect(Unit) {
        val currentStatus = permissions.associateWith { permission ->
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
        if (currentStatus != permissionsStatus) {
            permissionsStatus = currentStatus
        }
    }
    
    return remember(permissions, permissionsStatus) {
        MultiplePermissionsState(
            permissions = permissions,
            permissionsStatus = permissionsStatus,
            allPermissionsGranted = permissionsStatus.values.all { it },
            requestPermissions = { launcher.launch(permissions.toTypedArray()) }
        )
    }
}

data class PermissionState(
    val permission: String,
    val hasPermission: Boolean,
    val requestPermission: () -> Unit
)

data class MultiplePermissionsState(
    val permissions: List<String>,
    val permissionsStatus: Map<String, Boolean>,
    val allPermissionsGranted: Boolean,
    val requestPermissions: () -> Unit
)

object MediaPermissions {
    /**
     * Get required permissions for camera
     */
    fun getCameraPermissions(): List<String> {
        return listOf(Manifest.permission.CAMERA)
    }
    
    /**
     * Get required permissions for reading images/videos
     */
    fun getMediaPermissions(): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
    
    /**
     * Get required permissions for reading contacts
     */
    fun getContactsPermissions(): List<String> {
        return listOf(Manifest.permission.READ_CONTACTS)
    }
    
    /**
     * Get required permissions for recording audio
     */
    fun getAudioPermissions(): List<String> {
        return listOf(Manifest.permission.RECORD_AUDIO)
    }
    
    /**
     * Check if camera permission is granted
     */
    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Check if media permissions are granted
     */
    fun hasMediaPermissions(context: Context): Boolean {
        return getMediaPermissions().all { permission ->
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Check if contacts permission is granted
     */
    fun hasContactsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
