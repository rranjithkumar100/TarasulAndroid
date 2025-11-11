package com.tcc.tarasulandroid.feature.chat.models

/**
 * Model representing a message being replied to
 */
data class ReplyMessage(
    val messageId: String,
    val senderName: String,
    val content: String,
    val messageType: com.tcc.tarasulandroid.data.db.MessageType
)
