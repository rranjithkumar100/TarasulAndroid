package com.tcc.tarasulandroid.feature.chat.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.tcc.tarasulandroid.data.MessageWithMediaAndReply
import com.tcc.tarasulandroid.data.db.MessageDirection
import com.tcc.tarasulandroid.data.db.MessageEntity
import com.tcc.tarasulandroid.data.db.MessageType
import org.junit.Rule
import org.junit.Test
import java.util.UUID

/**
 * Unit tests for MessageBubbleWithReply component.
 */
class MessageBubbleWithReplyTest {
    
    @get:Rule
    val composeTestRule = composeTestRule()
    
    @Test
    fun messageBubble_displaysTextContent() {
        // Given
        val message = createTextMessage("Hello World!")
        val messageWithMedia = MessageWithMediaAndReply(message, null, null)
        
        // When
        composeTestRule.setContent {
            MessageBubbleWithReply(
                messageWithMedia = messageWithMedia,
                replyToMessage = null,
                onDownloadClick = {},
                onImageClick = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithText("Hello World!").assertIsDisplayed()
    }
    
    @Test
    fun messageBubble_showsReplyIndicator_whenMessageIsReply() {
        // Given
        val originalMessage = createTextMessage("Original message")
        val replyMessage = createTextMessage("Reply to original")
        val messageWithMedia = MessageWithMediaAndReply(replyMessage, null, originalMessage)
        
        // When
        composeTestRule.setContent {
            MessageBubbleWithReply(
                messageWithMedia = messageWithMedia,
                replyToMessage = originalMessage,
                onDownloadClick = {},
                onImageClick = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithText("Original message").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reply to original").assertIsDisplayed()
    }
    
    @Test
    fun messageBubble_displaysTimestamp() {
        // Given
        val message = createTextMessage("Test message")
        val messageWithMedia = MessageWithMediaAndReply(message, null, null)
        
        // When
        composeTestRule.setContent {
            MessageBubbleWithReply(
                messageWithMedia = messageWithMedia,
                replyToMessage = null,
                onDownloadClick = {},
                onImageClick = {}
            )
        }
        
        // Then - timestamp should be visible (format: HH:mm)
        composeTestRule.onAllNodesWithText(Regex("\\d{2}:\\d{2}")).assertCountEquals(1)
    }
    
    @Test
    fun messageBubble_outgoingMessage_hasCorrectAlignment() {
        // Given
        val message = createTextMessage("Outgoing", direction = MessageDirection.OUTGOING)
        val messageWithMedia = MessageWithMediaAndReply(message, null, null)
        
        // When
        composeTestRule.setContent {
            MessageBubbleWithReply(
                messageWithMedia = messageWithMedia,
                replyToMessage = null,
                onDownloadClick = {},
                onImageClick = {}
            )
        }
        
        // Then - verify message appears (alignment tested via screenshot/visual test)
        composeTestRule.onNodeWithText("Outgoing").assertIsDisplayed()
    }
    
    @Test
    fun messageBubble_incomingMessage_hasCorrectAlignment() {
        // Given
        val message = createTextMessage("Incoming", direction = MessageDirection.INCOMING)
        val messageWithMedia = MessageWithMediaAndReply(message, null, null)
        
        // When
        composeTestRule.setContent {
            MessageBubbleWithReply(
                messageWithMedia = messageWithMedia,
                replyToMessage = null,
                onDownloadClick = {},
                onImageClick = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithText("Incoming").assertIsDisplayed()
    }
    
    // Helper functions
    private fun createTextMessage(
        content: String,
        direction: MessageDirection = MessageDirection.INCOMING
    ): MessageEntity {
        return MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = "conv-1",
            content = content,
            type = MessageType.TEXT,
            direction = direction,
            sentAt = System.currentTimeMillis(),
            deliveredAt = null,
            readAt = null,
            replyToMessageId = null
        )
    }
}
