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
import kotlinx.coroutines.launch

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
    val coroutineScope = rememberCoroutineScope()
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
    
    // Trigger to force re-check
    var recheckTrigger by remember { mutableStateOf(0) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        android.util.Log.d("PermissionHandler", "Permission dialog results: $results")
        
        // Immediately update from dialog results
        permissionsStatus = results
        
        // Also trigger a delayed re-check to ensure we have the actual system state
        coroutineScope.launch {
            kotlinx.coroutines.delay(200) // Delay to let system fully update
            val updatedStatus = permissions.associateWith { permission ->
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
                android.util.Log.d("PermissionHandler", "Re-checked $permission: $granted")
                granted
            }
            if (updatedStatus != permissionsStatus) {
                permissionsStatus = updatedStatus
            }
            onPermissionsResult(updatedStatus)
            recheckTrigger++
        }
    }
    
    // Check permissions on composition and when trigger changes
    LaunchedEffect(permissions, recheckTrigger) {
        val currentStatus = permissions.associateWith { permission ->
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
        if (currentStatus != permissionsStatus) {
            android.util.Log.d("PermissionHandler", "Permission status changed: $currentStatus")
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
