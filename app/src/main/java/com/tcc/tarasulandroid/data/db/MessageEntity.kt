package com.tcc.tarasulandroid.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val conversationId: String, // Chat identifier (could be contactId or groupId)
    val senderId: String, // User who sent the message
    val recipientId: String, // User who receives the message
    val content: String, // Message content (encrypted if E2E enabled)
    val isEncrypted: Boolean = false, // Whether content is encrypted
    val timestamp: Long = System.currentTimeMillis(),
    val isSent: Boolean = false, // Message sent to server
    val isDelivered: Boolean = false, // Message delivered to recipient
    val isRead: Boolean = false, // Message read by recipient
    val isMine: Boolean // Whether this message was sent by current user
)
