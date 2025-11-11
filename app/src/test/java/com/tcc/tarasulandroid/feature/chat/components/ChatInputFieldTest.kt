package com.tcc.tarasulandroid.feature.chat.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.tcc.tarasulandroid.data.db.MessageType
import com.tcc.tarasulandroid.feature.chat.models.ReplyMessage
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for ChatInputField component.
 */
class ChatInputFieldTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun chatInputField_displaysTypedText() {
        // Given
        var messageText = ""
        
        // When
        composeTestRule.setContent {
            ChatInputField(
                messageText = messageText,
                onMessageTextChange = { messageText = it },
                onSendClick = {},
                onAttachClick = {},
                replyToMessage = null,
                onCancelReply = {}
            )
        }
        
        composeTestRule.onNodeWithText("Type a message...").performTextInput("Hello")
        
        // Then
        assert(messageText == "Hello")
    }
    
    @Test
    fun chatInputField_sendButton_isDisabled_whenTextIsEmpty() {
        // Given/When
        composeTestRule.setContent {
            ChatInputField(
                messageText = "",
                onMessageTextChange = {},
                onSendClick = {},
                onAttachClick = {},
                replyToMessage = null,
                onCancelReply = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithContentDescription("Send").assertIsNotEnabled()
    }
    
    @Test
    fun chatInputField_sendButton_isEnabled_whenTextIsNotEmpty() {
        // Given/When
        composeTestRule.setContent {
            ChatInputField(
                messageText = "Hello World",
                onMessageTextChange = {},
                onSendClick = {},
                onAttachClick = {},
                replyToMessage = null,
                onCancelReply = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithContentDescription("Send").assertIsEnabled()
    }
    
    @Test
    fun chatInputField_sendButton_triggersSendClick() {
        // Given
        var sendClicked = false
        
        // When
        composeTestRule.setContent {
            ChatInputField(
                messageText = "Test message",
                onMessageTextChange = {},
                onSendClick = { sendClicked = true },
                onAttachClick = {},
                replyToMessage = null,
                onCancelReply = {}
            )
        }
        
        composeTestRule.onNodeWithContentDescription("Send").performClick()
        
        // Then
        assert(sendClicked)
    }
    
    @Test
    fun chatInputField_attachButton_triggersAttachClick() {
        // Given
        var attachClicked = false
        
        // When
        composeTestRule.setContent {
            ChatInputField(
                messageText = "",
                onMessageTextChange = {},
                onSendClick = {},
                onAttachClick = { attachClicked = true },
                replyToMessage = null,
                onCancelReply = {}
            )
        }
        
        composeTestRule.onNodeWithContentDescription("Attach").performClick()
        
        // Then
        assert(attachClicked)
    }
    
    @Test
    fun chatInputField_showsReplyPreview_whenReplyingToMessage() {
        // Given
        val replyMessage = ReplyMessage(
            messageId = "msg-1",
            senderName = "John",
            content = "Original message",
            messageType = MessageType.TEXT
        )
        
        // When
        composeTestRule.setContent {
            ChatInputField(
                messageText = "",
                onMessageTextChange = {},
                onSendClick = {},
                onAttachClick = {},
                replyToMessage = replyMessage,
                onCancelReply = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithText("John").assertIsDisplayed()
        composeTestRule.onNodeWithText("Original message").assertIsDisplayed()
    }
    
    @Test
    fun chatInputField_cancelReply_triggersOnCancelReply() {
        // Given
        var replyCancel = false
        val replyMessage = ReplyMessage(
            messageId = "msg-1",
            senderName = "Jane",
            content = "Test",
            messageType = MessageType.TEXT
        )
        
        // When
        composeTestRule.setContent {
            ChatInputField(
                messageText = "",
                onMessageTextChange = {},
                onSendClick = {},
                onAttachClick = {},
                replyToMessage = replyMessage,
                onCancelReply = { replyCancelled = true }
            )
        }
        
        // Find and click cancel button in reply preview
        composeTestRule.onNodeWithContentDescription("Cancel reply").performClick()
        
        // Then
        assert(replyCancelled)
    }
}
