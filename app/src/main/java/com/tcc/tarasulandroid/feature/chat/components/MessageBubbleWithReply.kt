package com.tcc.tarasulandroid.feature.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tcc.tarasulandroid.data.MessageWithMedia
import com.tcc.tarasulandroid.data.MessageWithMediaAndReply
import com.tcc.tarasulandroid.data.db.MessageEntity
import com.tcc.tarasulandroid.data.db.MessageType
import com.tcc.tarasulandroid.feature.chat.MessageBubble

/**
 * Message bubble that shows the reply indicator when message is a reply.
 *
 * @param messageWithMedia The message data including media
 * @param replyToMessage The original message being replied to (null if not a reply)
 * @param onDownloadClick Callback for media download
 * @param onImageClick Callback when image is clicked
 */
@Composable
fun MessageBubbleWithReply(
    messageWithMedia: MessageWithMediaAndReply,
    replyToMessage: MessageEntity?,
    onDownloadClick: (String) -> Unit,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // If no reply, show regular bubble
    if (replyToMessage == null) {
        MessageBubble(
            messageWithMedia = MessageWithMedia(messageWithMedia.message, messageWithMedia.media),
            onDownloadClick = onDownloadClick,
            onImageClick = onImageClick,
            modifier = modifier
        )
        return
    }
    
    // Message with reply indicator
    val message = messageWithMedia.message
    val media = messageWithMedia.media
    val isOutgoing = message.direction == com.tcc.tarasulandroid.data.db.MessageDirection.OUTGOING
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isOutgoing) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
                .padding(8.dp)
        ) {
            // Reply indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    .padding(8.dp)
                    .clickable { /* Optional: scroll to replied message */ }
            ) {
                Column {
                    Text(
                        text = if (replyToMessage.direction == com.tcc.tarasulandroid.data.db.MessageDirection.OUTGOING) {
                            "You"
                        } else {
                            "Friend" // Could pass contact name
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = when (replyToMessage.type) {
                            MessageType.TEXT -> replyToMessage.content
                            MessageType.IMAGE -> "📷 Photo"
                            MessageType.VIDEO -> "🎥 Video"
                            MessageType.FILE -> "📎 File"
                            MessageType.AUDIO -> "🎵 Audio"
                            MessageType.CONTACT -> "👤 Contact"
                            else -> replyToMessage.content
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 2
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Actual message content
            when (message.type) {
                MessageType.TEXT -> {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isOutgoing) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                MessageType.IMAGE, MessageType.VIDEO, MessageType.FILE, MessageType.AUDIO, MessageType.CONTACT -> {
                    // Use MessageBubble for media content
                    MessageBubble(
                        messageWithMedia = MessageWithMedia(message, media),
                        onDownloadClick = onDownloadClick,
                        onImageClick = onImageClick,
                        modifier = Modifier.padding(0.dp)
                    )
                }
                else -> {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            // Timestamp
            Text(
                text = java.text.SimpleDateFormat(
                    "HH:mm",
                    java.util.Locale.getDefault()
                ).format(java.util.Date(message.timestamp)),
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
