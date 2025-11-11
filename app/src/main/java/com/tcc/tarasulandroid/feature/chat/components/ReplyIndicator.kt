package com.tcc.tarasulandroid.feature.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import com.tcc.tarasulandroid.R
import com.tcc.tarasulandroid.data.db.MessageType

/**
 * Small reply indicator shown inside message bubble
 */
@Composable
fun ReplyIndicator(
    senderName: String,
    content: String,
    messageType: MessageType,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        // Reply line
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(2.dp)
                )
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = senderName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Row {
                // Icon for media types
                when (messageType) {
                    MessageType.IMAGE -> Icon(
                        painter = painterResource(R.drawable.ic_gallery),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MessageType.VIDEO -> Icon(
                        painter = painterResource(R.drawable.ic_video),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MessageType.FILE -> Icon(
                        painter = painterResource(R.drawable.ic_file),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    else -> {}
                }
                
                if (messageType != MessageType.TEXT) {
                    Spacer(modifier = Modifier.width(4.dp))
                }
                
                Text(
                    text = getReplyContentText(content, messageType),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun getReplyContentText(content: String, messageType: MessageType): String {
    return when (messageType) {
        MessageType.IMAGE -> stringResource(R.string.image_message)
        MessageType.VIDEO -> stringResource(R.string.video)
        MessageType.FILE -> stringResource(R.string.file)
        MessageType.CONTACT -> stringResource(R.string.contact)
        MessageType.AUDIO -> stringResource(R.string.audio)
        MessageType.TEXT -> content
        else -> content
    }
}
