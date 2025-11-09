package com.tcc.tarasulandroid.feature.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaPickerBottomSheet(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onVideoClick: () -> Unit,
    onFileClick: () -> Unit,
    onContactClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "Send Media",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            
            MediaOption(
                icon = Icons.Default.CameraAlt,
                label = "Camera",
                onClick = {
                    onCameraClick()
                    onDismiss()
                }
            )
            
            MediaOption(
                icon = Icons.Default.PhotoLibrary,
                label = "Gallery",
                onClick = {
                    onGalleryClick()
                    onDismiss()
                }
            )
            
            MediaOption(
                icon = Icons.Default.Videocam,
                label = "Video",
                onClick = {
                    onVideoClick()
                    onDismiss()
                }
            )
            
            MediaOption(
                icon = Icons.Default.InsertDriveFile,
                label = "Document",
                onClick = {
                    onFileClick()
                    onDismiss()
                }
            )
            
            MediaOption(
                icon = Icons.Default.ContactPage,
                label = "Contact",
                onClick = {
                    onContactClick()
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun MediaOption(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
