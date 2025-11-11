package com.tcc.tarasulandroid.feature.chat

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tcc.tarasulandroid.R
import com.tcc.tarasulandroid.data.MessageWithMedia
import com.tcc.tarasulandroid.data.db.DownloadStatus
import com.tcc.tarasulandroid.data.db.MessageType
import com.tcc.tarasulandroid.feature.video.VideoPlayerActivity
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
    val isOutgoing = message.direction == com.tcc.tarasulandroid.data.db.MessageDirection.OUTGOING
    
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
                
                // Text content or caption (but NOT for CONTACT type - it's JSON)
                if (message.content.isNotBlank() && message.type != MessageType.CONTACT) {
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
                        contentDescription = stringResource(R.string.image_message),
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
                            text = stringResource(R.string.download_progress, media.downloadProgress),
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
                        Text(
                            text = stringResource(R.string.download_failed),
                            style = MaterialTheme.typography.bodySmall
                        )
                        TextButton(onClick = { onDownloadClick(media.mediaId) }) {
                            Text(stringResource(R.string.retry))
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
                        Text(stringResource(R.string.download))
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
    
    val context = LocalContext.current
    val videoFile = media.localPath?.let { File(it) }
    val canPlay = media.downloadStatus == DownloadStatus.DONE && 
                  videoFile != null && 
                  videoFile.exists()
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(enabled = canPlay) {
                if (canPlay && videoFile != null) {
                    // Check if file still exists
                    if (!videoFile.exists()) {
                        android.widget.Toast.makeText(
                            context,
                            context.getString(R.string.video_not_found),
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        return@clickable
                    }
                    
                    // Launch video player
                    val intent = Intent(context, VideoPlayerActivity::class.java).apply {
                        putExtra(VideoPlayerActivity.EXTRA_VIDEO_PATH, videoFile.absolutePath)
                        putExtra(VideoPlayerActivity.EXTRA_VIDEO_NAME, media.fileName ?: "Video")
                    }
                    context.startActivity(intent)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Blurred background thumbnail (WhatsApp style)
        if (media.thumbnailPath != null && File(media.thumbnailPath).exists()) {
            // Background: Blurred thumbnail
            AsyncImage(
                model = File(media.thumbnailPath),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(
                        radius = 20.dp,
                        edgeTreatment = BlurredEdgeTreatment.Unbounded
                    ),
                contentScale = ContentScale.Crop
            )
            
            // Foreground: Clear thumbnail with dark overlay
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = File(media.thumbnailPath),
                    contentDescription = stringResource(R.string.video_thumbnail),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                
                // Dark overlay for better icon visibility
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )
            }
        } else {
            // No thumbnail - show placeholder
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
        
        // Download button or play icon overlay
        when {
            media.downloadStatus != DownloadStatus.DONE -> {
                // Show download button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    TextButton(onClick = { onDownloadClick(media.mediaId) }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_file),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.download_video),
                            color = Color.White
                        )
                    }
                }
            }
            canPlay -> {
                // Show play icon (WhatsApp style)
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_video),
                            contentDescription = stringResource(R.string.play_video),
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Show duration badge if available
                if (media.durationMs != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.Black.copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = formatDuration(media.durationMs),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
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
                contentDescription = stringResource(R.string.file),
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = media.fileName ?: stringResource(R.string.file),
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
                Text(stringResource(R.string.download))
            }
        }
    }
}

@Composable
private fun ContactMessageContent(message: com.tcc.tarasulandroid.data.db.MessageEntity) {
    // Try to parse contact info from JSON
    val contactInfo = com.tcc.tarasulandroid.data.ContactInfo.fromJsonString(message.content)
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Contact avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (contactInfo != null) {
                    Text(
                        text = contactInfo.name.firstOrNull()?.toString()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_person),
                        contentDescription = stringResource(R.string.contact),
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Contact info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = contactInfo?.name ?: stringResource(R.string.unknown_contact),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                if (contactInfo != null && contactInfo.phoneNumbers.isNotEmpty()) {
                    Text(
                        text = contactInfo.phoneNumbers.first(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (contactInfo.phoneNumbers.size > 1) {
                        Text(
                            text = stringResource(R.string.more_numbers, contactInfo.phoneNumbers.size - 1),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.contact_information),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Message/Add button
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chat),
                        contentDescription = stringResource(R.string.message_contact),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
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
                contentDescription = stringResource(R.string.audio),
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = media.fileName ?: stringResource(R.string.audio),
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
                Text(stringResource(R.string.download))
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
