package com.tcc.tarasulandroid.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["conversationId", "timestamp"]),
        Index(value = ["status"]),
        Index(value = ["mediaId"])
    ]
)
data class MessageEntity(
    @PrimaryKey
    val id: String,
    
    val conversationId: String, // Chat identifier
    val senderId: String, // User who sent the message
    val recipientId: String, // User who receives the message
    
    // Message type and content
    val type: MessageType = MessageType.TEXT,
    val content: String, // Text content or caption (encrypted if E2E enabled)
    val mediaId: String? = null, // Reference to MediaEntity for non-text messages
    
    // Encryption
    val isEncrypted: Boolean = false, // Whether content is encrypted
    
    // Timestamps
    val timestamp: Long = System.currentTimeMillis(),
    val deliveredAt: Long? = null,
    val readAt: Long? = null,
    
    // Status tracking
    val status: MessageStatus = MessageStatus.PENDING,
    val direction: MessageDirection, // OUTGOING or INCOMING
    
    // Legacy fields (deprecated, keep for migration)
    @Deprecated("Use status instead")
    val isSent: Boolean = false,
    @Deprecated("Use status instead")
    val isDelivered: Boolean = false,
    @Deprecated("Use status instead")
    val isRead: Boolean = false,
    @Deprecated("Use direction instead")
    val isMine: Boolean = direction == MessageDirection.OUTGOING
)
