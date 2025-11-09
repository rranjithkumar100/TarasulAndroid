package com.tcc.tarasulandroid.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey
    val id: String, // Conversation identifier
    val contactId: String, // The other user's ID
    val contactName: String,
    val contactPhoneNumber: String,
    val lastMessage: String, // Last message content (decrypted for display)
    val lastMessageTime: Long,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val isEncryptionEnabled: Boolean = false // E2E encryption status for this conversation
)
