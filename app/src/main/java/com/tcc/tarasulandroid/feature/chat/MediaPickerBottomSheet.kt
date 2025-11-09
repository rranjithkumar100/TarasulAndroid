package com.tcc.tarasulandroid.feature.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tcc.tarasulandroid.R

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
                iconRes = R.drawable.ic_camera,
                label = "Camera",
                onClick = {
                    onCameraClick()
                    onDismiss()
                }
            )
            
            MediaOption(
                iconRes = R.drawable.ic_gallery,
                label = "Gallery",
                onClick = {
                    onGalleryClick()
                    onDismiss()
                }
            )
            
            MediaOption(
                iconRes = R.drawable.ic_video,
                label = "Video",
                onClick = {
                    onVideoClick()
                    onDismiss()
                }
            )
            
            MediaOption(
                iconRes = R.drawable.ic_file,
                label = "Document",
                onClick = {
                    onFileClick()
                    onDismiss()
                }
            )
            
            MediaOption(
                iconRes = R.drawable.ic_contact,
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
    iconRes: Int,
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
            painter = painterResource(id = iconRes),
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
