package com.tcc.tarasulandroid.feature.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tcc.tarasulandroid.R
import com.tcc.tarasulandroid.data.db.MessageType
import com.tcc.tarasulandroid.feature.chat.models.ReplyMessage

/**
 * Preview component showing the message being replied to
 */
@Composable
fun ReplyPreview(
    replyMessage: ReplyMessage,
    onCancelReply: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reply indicator line
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Message content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = replyMessage.senderName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon for media type
                    when (replyMessage.messageType) {
                        MessageType.IMAGE -> Icon(
                            painter = painterResource(R.drawable.ic_gallery),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MessageType.VIDEO -> Icon(
                            painter = painterResource(R.drawable.ic_video),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MessageType.FILE -> Icon(
                            painter = painterResource(R.drawable.ic_file),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MessageType.CONTACT -> Icon(
                            painter = painterResource(R.drawable.ic_contact),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        else -> {}
                    }
                    
                    if (replyMessage.messageType != MessageType.TEXT) {
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    
                    Text(
                        text = getReplyPreviewText(replyMessage),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Cancel button
            IconButton(
                onClick = onCancelReply,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.cancel),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Get preview text based on message type
 */
@Composable
private fun getReplyPreviewText(replyMessage: ReplyMessage): String {
    return when (replyMessage.messageType) {
        MessageType.IMAGE -> stringResource(R.string.image_message)
        MessageType.VIDEO -> stringResource(R.string.video)
        MessageType.FILE -> stringResource(R.string.file)
        MessageType.CONTACT -> stringResource(R.string.contact)
        MessageType.AUDIO -> stringResource(R.string.audio)
        MessageType.TEXT -> replyMessage.content
        else -> replyMessage.content
    }
}
