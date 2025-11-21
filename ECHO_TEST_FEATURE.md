# Echo Test Feature for Incoming Messages

## Overview
Added a testing feature that automatically echoes sent messages as incoming messages from the contact. This is useful for testing the chat UI, message display, pagination, and animations without needing another device or account.

## Changes Made

### 1. ChatScreen.kt
Modified the `onSendClick` handler to automatically create an incoming echo message after sending:

```kotlin
onSendClick = {
    if (messageText.isNotBlank() && conversationId != null) {
        coroutineScope.launch {
            try {
                // Send the message normally
                messagesRepository.sendMessage(...)
                
                // TEST: Echo incoming message (remove later)
                kotlinx.coroutines.delay(500)  // 500ms delay for realistic feel
                try {
                    messagesRepository.receiveTestMessage(
                        conversationId = conversationId!!,
                        senderId = contact.id,
                        content = messageText
                    )
                } catch (e: Exception) {
                    android.util.Log.e("ChatScreen", "Error creating test incoming message", e)
                }
                
                // Reload messages to display the echo
                reloadMessages(...)
            } catch (e: Exception) {
                android.util.Log.e("ChatScreen", "Error sending message", e)
            }
        }
    }
}
```

### 2. MessagesRepository.kt
Added new `receiveTestMessage()` suspend function:

```kotlin
suspend fun receiveTestMessage(
    conversationId: String,
    senderId: String,
    content: String
) = withContext(Dispatchers.IO) {
    // Creates an incoming MessageEntity as if from the contact
    // Properties set:
    // - direction = MessageDirection.INCOMING
    // - isMine = false
    // - status = MessageStatus.DELIVERED
    // - isDelivered = true
    // 
    // The message is saved to the database with:
    // - Automatic encryption (if enabled for conversation)
    // - Proper timestamp
    // - Database persistence
}
```

## How It Works

1. **User sends a message**: They type and press send
2. **Message is sent**: The message is saved to the database as OUTGOING
3. **500ms delay**: Wait a bit to make it feel more realistic
4. **Echo message created**: The same message is created as INCOMING from the contact
5. **Database saved**: The incoming message is persisted in the database
6. **Messages reload**: The UI refreshes and displays both sent and received messages
7. **Auto-scroll**: The chat automatically scrolls to show the new incoming message

## Features

- ✅ Messages are **saved to database** - they persist across app restarts
- ✅ **Encryption support** - respects conversation encryption settings
- ✅ **Realistic delay** - 500ms delay before echo makes it feel natural
- ✅ **Proper message properties** - Direction, status, and delivery flags are correct
- ✅ **Automatic UI update** - Messages appear in real-time via message reload
- ✅ **Logging** - Debug logs track the flow for troubleshooting

## Testing Scenarios

This feature allows you to test:
- ✅ Message display (both sent and received)
- ✅ Chat UI layout and spacing
- ✅ Message pagination (older messages load)
- ✅ Auto-scroll behavior
- ✅ Reply-to functionality
- ✅ Media message handling
- ✅ Message timestamps
- ✅ Encryption/decryption
- ✅ Conversation last message updates
- ✅ Message animations

## Removing This Feature

When ready for production, remove the testing code block from ChatScreen.kt:

```kotlin
// Remove this entire block:
// TEST: Echo incoming message (remove later)
kotlinx.coroutines.delay(500)
try {
    messagesRepository.receiveTestMessage(
        conversationId = conversationId!!,
        senderId = contact.id,
        content = messageText
    )
} catch (e: Exception) {
    android.util.Log.e("ChatScreen", "Error creating test incoming message", e)
}
```

You can leave the `receiveTestMessage()` function in MessagesRepository for future testing needs, or remove it entirely if not needed.

## Database Impact

The test messages are **real database entries** with:
- Unique message IDs
- Proper conversation references
- Correct sender/recipient info
- Full encryption support
- Updateable message status

They will appear in your message history and conversation lists until manually deleted from the database.

## Notes

- The 500ms delay is configurable - adjust as needed
- Echo messages appear from the contact with their ID
- The feature respects all message encryption settings
- Logging helps debug any issues with message persistence

