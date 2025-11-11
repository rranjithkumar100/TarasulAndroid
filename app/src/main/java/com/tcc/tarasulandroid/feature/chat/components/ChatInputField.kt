package com.tcc.tarasulandroid.feature.chat.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tcc.tarasulandroid.R
import com.tcc.tarasulandroid.feature.chat.models.ReplyMessage

/**
 * Chat input field with send button and media attachment button.
 *
 * Features:
 * - Text input field
 * - Send button (only enabled when text is not empty)
 * - Attach media button
 * - Reply preview (when replying to a message)
 *
 * @param messageText Current text input
 * @param onMessageTextChange Callback when text changes
 * @param onSendClick Callback when send button is clicked
 * @param onAttachClick Callback when attach button is clicked
 * @param replyToMessage Current reply context (null if not replying)
 * @param onCancelReply Callback to cancel reply
 */
@Composable
fun ChatInputField(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onAttachClick: () -> Unit,
    replyToMessage: ReplyMessage?,
    onCancelReply: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Reply preview
        if (replyToMessage != null) {
            ReplyPreview(
                replyMessage = replyToMessage,
                onCancelReply = onCancelReply,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        
        // Input row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Attach button
            IconButton(
                onClick = onAttachClick,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_attach),
                    contentDescription = "Attach",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            // Text field
            TextField(
                value = messageText,
                onValueChange = onMessageTextChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = if (replyToMessage != null) {
                            "Replying to ${replyToMessage.senderName}"
                        } else {
                            "Type a message..."
                        }
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                ),
                shape = CircleShape,
                maxLines = 5
            )
            
            // Send button
            IconButton(
                onClick = onSendClick,
                enabled = messageText.isNotBlank(),
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (messageText.isNotBlank()) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )
            }
        }
    }
}
