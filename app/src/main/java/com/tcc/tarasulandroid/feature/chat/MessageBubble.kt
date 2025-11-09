package com.tcc.tarasulandroid.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tcc.tarasulandroid.R
import com.tcc.tarasulandroid.data.MessageWithMedia
import com.tcc.tarasulandroid.data.db.DownloadStatus
import com.tcc.tarasulandroid.data.db.MessageType
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessageBubble(
    messageWithMedia: MessageWithMedia,
    modifier: Modifier = Modifier,
    onDownloadClick: (String) -> Unit = {}
) {
    val message = messageWithMedia.message
    val media = messageWithMedia.media
    val isOutgoing = message.isMine
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isOutgoing) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                // Render media content based on type
                when (message.type) {
                    MessageType.IMAGE -> {
                        ImageMessageContent(media, onDownloadClick)
                    }
                    MessageType.VIDEO -> {
                        VideoMessageContent(media, onDownloadClick)
                    }
                    MessageType.FILE -> {
                        FileMessageContent(media, onDownloadClick)
                    }
                    MessageType.CONTACT -> {
                        ContactMessageContent(message)
                    }
                    MessageType.AUDIO -> {
                        AudioMessageContent(media, onDownloadClick)
                    }
                    else -> {}
                }
                
                // Text content or caption
                if (message.content.isNotBlank()) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isOutgoing) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                
                // Timestamp
                Text(
                    text = formatTimestamp(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isOutgoing) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    },
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun ImageMessageContent(
    media: com.tcc.tarasulandroid.data.db.MediaEntity?,
    onDownloadClick: (String) -> Unit
) {
    if (media == null) return
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        when (media.downloadStatus) {
            DownloadStatus.DONE -> {
                if (media.localPath != null && File(media.localPath).exists()) {
                    AsyncImage(
                        model = File(media.localPath),
                        contentDescription = "Image message",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            DownloadStatus.DOWNLOADING -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Text(
                            text = "${media.downloadProgress}%",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            DownloadStatus.FAILED -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Download failed", style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = { onDownloadClick(media.mediaId) }) {
                            Text("Retry")
                        }
                    }
                }
            }
            else -> {
                // NOT_STARTED or PENDING
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(onClick = { onDownloadClick(media.mediaId) }) {
                        Text("Download")
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoMessageContent(
    media: com.tcc.tarasulandroid.data.db.MediaEntity?,
    onDownloadClick: (String) -> Unit
) {
    if (media == null) return
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        // Show thumbnail or video placeholder
        if (media.thumbnailPath != null && File(media.thumbnailPath).exists()) {
            AsyncImage(
                model = File(media.thumbnailPath),
                contentDescription = "Video thumbnail",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        // Show download button or play icon
        if (media.downloadStatus != DownloadStatus.DONE) {
            TextButton(onClick = { onDownloadClick(media.mediaId) }) {
                Text("Download Video")
            }
        }
    }
}

@Composable
private fun FileMessageContent(
    media: com.tcc.tarasulandroid.data.db.MediaEntity?,
    onDownloadClick: (String) -> Unit
) {
    if (media == null) return
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_file),
            contentDescription = "File",
            modifier = Modifier.size(40.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = media.fileName ?: "File",
                style = MaterialTheme.typography.bodyMedium
            )
            if (media.fileSize != null) {
                Text(
                    text = formatFileSize(media.fileSize),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        if (media.downloadStatus != DownloadStatus.DONE) {
            TextButton(onClick = { onDownloadClick(media.mediaId) }) {
                Text("Download")
            }
        }
    }
}

@Composable
private fun ContactMessageContent(message: com.tcc.tarasulandroid.data.db.MessageEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_person),
            contentDescription = "Contact",
            modifier = Modifier.size(40.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "Contact: ${message.content}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun AudioMessageContent(
    media: com.tcc.tarasulandroid.data.db.MediaEntity?,
    onDownloadClick: (String) -> Unit
) {
    if (media == null) return
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_audio),
            contentDescription = "Audio",
            modifier = Modifier.size(40.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = media.fileName ?: "Audio",
                style = MaterialTheme.typography.bodyMedium
            )
            if (media.durationMs != null) {
                Text(
                    text = formatDuration(media.durationMs),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        if (media.downloadStatus != DownloadStatus.DONE) {
            TextButton(onClick = { onDownloadClick(media.mediaId) }) {
                Text("Download")
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }
}

private fun formatDuration(durationMs: Long): String {
    val seconds = durationMs / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}
