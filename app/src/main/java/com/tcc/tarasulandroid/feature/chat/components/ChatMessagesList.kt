package com.tcc.tarasulandroid.feature.chat.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tcc.tarasulandroid.data.MessageWithMediaAndReply

/**
 * Scrollable list of chat messages with loading indicators.
 * FIXED: Removed reverseLayout and .reversed() to fix scroll jump issues with media
 */
@Composable
fun ChatMessagesList(
    messages: List<MessageWithMediaAndReply>,
    listState: LazyListState,
    isLoadingMore: Boolean,
    onReply: (MessageWithMediaAndReply) -> Unit,
    onDownloadClick: (String) -> Unit,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        reverseLayout = false, // CHANGED: Normal layout
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        // Loading indicator at TOP for older messages
        if (isLoadingMore) {
            item(key = "loading_indicator") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        }

        // Empty state
        if (messages.isEmpty() && !isLoadingMore) {
            item(key = "empty_state") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No messages yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Messages in NORMAL order (oldest to newest)
        items(
            items = messages, // CHANGED: No .reversed()
            key = { it.message.id }
        ) { messageWithMedia ->
            SwipeableMessageItem(
                messageWithMedia = messageWithMedia,
                onReply = { onReply(messageWithMedia) },
                onDownloadClick = onDownloadClick,
                onImageClick = onImageClick
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}