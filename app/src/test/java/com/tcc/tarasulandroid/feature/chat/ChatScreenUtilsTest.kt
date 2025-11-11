package com.tcc.tarasulandroid.feature.chat

import com.tcc.tarasulandroid.data.MessageWithMediaAndReply
import com.tcc.tarasulandroid.data.db.MessageDirection
import com.tcc.tarasulandroid.data.db.MessageEntity
import com.tcc.tarasulandroid.data.db.MessageType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

/**
 * Unit tests for ChatScreen utility functions.
 */
class ChatScreenUtilsTest {
    
    @Test
    fun toReplyMessage_outgoingMessage_returnsSenderAsYou() {
        // Given
        val message = createMessage(direction = MessageDirection.OUTGOING)
        val messageWithMedia = MessageWithMediaAndReply(message, null, null)
        val contactName = "John Doe"
        
        // When
        val replyMessage = messageWithMedia.toReplyMessage(contactName)
        
        // Then
        assertEquals("You", replyMessage.senderName)
    }
    
    @Test
    fun toReplyMessage_incomingMessage_returnsContactName() {
        // Given
        val message = createMessage(direction = MessageDirection.INCOMING)
        val messageWithMedia = MessageWithMediaAndReply(message, null, null)
        val contactName = "Jane Smith"
        
        // When
        val replyMessage = messageWithMedia.toReplyMessage(contactName)
        
        // Then
        assertEquals("Jane Smith", replyMessage.senderName)
    }
    
    @Test
    fun toReplyMessage_copiesMessageContent() {
        // Given
        val content = "Test message content"
        val message = createMessage(content = content)
        val messageWithMedia = MessageWithMediaAndReply(message, null, null)
        
        // When
        val replyMessage = messageWithMedia.toReplyMessage("Contact")
        
        // Then
        assertEquals(content, replyMessage.content)
    }
    
    @Test
    fun toReplyMessage_copiesMessageType() {
        // Given
        val message = createMessage(type = MessageType.IMAGE)
        val messageWithMedia = MessageWithMediaAndReply(message, null, null)
        
        // When
        val replyMessage = messageWithMedia.toReplyMessage("Contact")
        
        // Then
        assertEquals(MessageType.IMAGE, replyMessage.messageType)
    }
    
    @Test
    fun toReplyMessage_copiesMessageId() {
        // Given
        val messageId = UUID.randomUUID().toString()
        val message = createMessage(id = messageId)
        val messageWithMedia = MessageWithMediaAndReply(message, null, null)
        
        // When
        val replyMessage = messageWithMedia.toReplyMessage("Contact")
        
        // Then
        assertEquals(messageId, replyMessage.messageId)
    }
    
    // Helper function
    private fun createMessage(
        id: String = UUID.randomUUID().toString(),
        content: String = "Test message",
        type: MessageType = MessageType.TEXT,
        direction: MessageDirection = MessageDirection.INCOMING
    ): MessageEntity {
        return MessageEntity(
            id = id,
            conversationId = "conv-1",
            content = content,
            type = type,
            direction = direction,
            sentAt = System.currentTimeMillis(),
            deliveredAt = null,
            readAt = null,
            replyToMessageId = null
        )
    }
}
