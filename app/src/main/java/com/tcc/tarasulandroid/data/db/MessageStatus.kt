package com.tcc.tarasulandroid.data.db

enum class MessageStatus {
    PENDING,    // Message created, not yet sent
    SENT,       // Message sent to server
    DELIVERED,  // Message delivered to recipient
    READ,       // Message read by recipient
    FAILED      // Message failed to send
}
