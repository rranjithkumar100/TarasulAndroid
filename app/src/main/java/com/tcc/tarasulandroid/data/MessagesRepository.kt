package com.tcc.tarasulandroid.data

import android.net.Uri
import com.tcc.tarasulandroid.data.db.*
import com.tcc.tarasulandroid.data.encryption.MessageEncryption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.*
import javax.crypto.SecretKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessagesRepository @Inject constructor(
    private val messagesDao: MessagesDao,
    private val securePreferencesManager: SecurePreferencesManager,
    private val mediaRepository: MediaRepository
) {
    
    // In-memory cache for encryption keys (per conversation)
    // In production, store in Android KeyStore
    private val encryptionKeys = mutableMapOf<String, SecretKey>()
    
    /**
     * Get all conversations with decrypted last message
     */
    fun getAllConversations(): Flow<List<ConversationEntity>> {
        return messagesDao.getAllConversations().map { conversations ->
            conversations.map { conversation ->
                if (conversation.isEncryptionEnabled && conversation.lastMessage.isNotEmpty()) {
                    try {
                        val key = getEncryptionKey(conversation.id)
                        val decryptedMessage = MessageEncryption.decrypt(conversation.lastMessage, key)
                        conversation.copy(lastMessage = decryptedMessage)
                    } catch (e: Exception) {
                        // If decryption fails, show encrypted indicator
                        conversation.copy(lastMessage = "🔒 Encrypted message")
                    }
                } else {
                    conversation
                }
            }
        }
    }
    
    /**
     * Get messages for a conversation with decryption
     */
    fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>> {
        return messagesDao.getMessagesForConversation(conversationId).map { messages ->
            messages.map { message ->
                if (message.isEncrypted) {
                    try {
                        val key = getEncryptionKey(conversationId)
                        val decryptedContent = MessageEncryption.decrypt(message.content, key)
                        message.copy(content = decryptedContent)
                    } catch (e: Exception) {
                        message.copy(content = "🔒 Decryption failed")
                    }
                } else {
                    message
                }
            }
        }
    }
    
    /**
     * Get messages with media for a conversation
     */
    fun getMessagesWithMediaForConversation(conversationId: String): Flow<List<MessageWithMedia>> {
        return messagesDao.getMessagesWithMediaForConversation(conversationId).map { messagesWithMedia ->
            messagesWithMedia.map { messageWithMedia ->
                val message = messageWithMedia.message
                if (message.isEncrypted && message.content.isNotEmpty()) {
                    try {
                        val key = getEncryptionKey(conversationId)
                        val decryptedContent = MessageEncryption.decrypt(message.content, key)
                        messageWithMedia.copy(
                            message = message.copy(content = decryptedContent)
                        )
                    } catch (e: Exception) {
                        messageWithMedia.copy(
                            message = message.copy(content = "🔒 Decryption failed")
                        )
                    }
                } else {
                    messageWithMedia
                }
            }
        }
    }
    
    /**
     * Send a new message (with optional encryption)
     */
    suspend fun sendMessage(
        conversationId: String,
        content: String,
        recipientId: String
    ) = withContext(Dispatchers.IO) {
        android.util.Log.d("MessagesRepository", "sendMessage called - conversationId: $conversationId, content: $content, recipientId: $recipientId")
        
        val currentUserId = securePreferencesManager.getUserEmail() ?: "me"
        android.util.Log.d("MessagesRepository", "currentUserId: $currentUserId")
        
        // Check if encryption is enabled for this conversation
        val conversation = messagesDao.getConversationById(conversationId)
        android.util.Log.d("MessagesRepository", "conversation found: ${conversation != null}, id: ${conversation?.id}")
        
        val isEncrypted = conversation?.isEncryptionEnabled ?: false
        
        val messageContent = if (isEncrypted) {
            val key = getEncryptionKey(conversationId)
            MessageEncryption.encrypt(content, key)
        } else {
            content
        }
        
        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            senderId = currentUserId,
            recipientId = recipientId,
            content = messageContent,
            isEncrypted = isEncrypted,
            timestamp = System.currentTimeMillis(),
            isSent = true,
            isDelivered = false,
            isRead = false,
            isMine = true,
            direction = MessageDirection.OUTGOING,
            status = MessageStatus.PENDING
        )
        
        android.util.Log.d("MessagesRepository", "Inserting message: ${message.id}, content: ${message.content}")
        messagesDao.insertMessage(message)
        android.util.Log.d("MessagesRepository", "Message inserted successfully")
        
        // Update conversation with last message
        messagesDao.updateConversationLastMessage(
            conversationId = conversationId,
            lastMessage = if (isEncrypted) messageContent else content,
            lastMessageTime = message.timestamp
        )
        android.util.Log.d("MessagesRepository", "Conversation updated with last message")
    }
    
    /**
     * Create or get a conversation with a contact
     */
    suspend fun getOrCreateConversation(
        contactId: String,
        contactName: String,
        contactPhoneNumber: String
    ): ConversationEntity = withContext(Dispatchers.IO) {
        android.util.Log.d("MessagesRepository", "getOrCreateConversation - contactId: $contactId, contactName: $contactName")
        
        // Check if conversation already exists
        val existing = messagesDao.getConversationByContactId(contactId)
        if (existing != null) {
            android.util.Log.d("MessagesRepository", "Found existing conversation: ${existing.id}")
            return@withContext existing
        }
        
        // Create new conversation
        val conversation = ConversationEntity(
            id = UUID.randomUUID().toString(),
            contactId = contactId,
            contactName = contactName,
            contactPhoneNumber = contactPhoneNumber,
            lastMessage = "",
            lastMessageTime = System.currentTimeMillis(),
            unreadCount = 0,
            isOnline = false,
            isEncryptionEnabled = false
        )
        
        android.util.Log.d("MessagesRepository", "Creating new conversation: ${conversation.id}")
        messagesDao.insertConversation(conversation)
        android.util.Log.d("MessagesRepository", "Conversation created successfully")
        
        return@withContext conversation
    }
    
    /**
     * Enable/disable E2E encryption for a conversation
     */
    suspend fun setEncryptionEnabled(conversationId: String, enabled: Boolean) = withContext(Dispatchers.IO) {
        if (enabled) {
            // Generate and store encryption key
            val key = MessageEncryption.generateKey()
            encryptionKeys[conversationId] = key
            
            // In production, securely store key string
            val keyString = MessageEncryption.keyToString(key)
            // TODO: Store keyString in Android KeyStore
        } else {
            // Remove encryption key
            encryptionKeys.remove(conversationId)
        }
        
        messagesDao.updateConversationEncryptionStatus(conversationId, enabled)
    }
    
    /**
     * Mark conversation as read
     */
    suspend fun markConversationAsRead(conversationId: String) = withContext(Dispatchers.IO) {
        messagesDao.markConversationAsRead(conversationId)
    }
    
    /**
     * Delete a conversation and all its messages
     */
    suspend fun deleteConversation(conversationId: String) = withContext(Dispatchers.IO) {
        messagesDao.deleteAllMessagesInConversation(conversationId)
        messagesDao.deleteConversationById(conversationId)
        encryptionKeys.remove(conversationId)
    }
    
    /**
     * Send a media message (image/video/file/contact)
     */
    suspend fun sendMediaMessage(
        conversationId: String,
        recipientId: String,
        mediaType: MessageType,
        mediaUri: Uri,
        caption: String = "",
        mimeType: String? = null,
        fileName: String? = null
    ) = withContext(Dispatchers.IO) {
        android.util.Log.d("MessagesRepository", "sendMediaMessage - type: $mediaType, uri: $mediaUri")
        
        val currentUserId = securePreferencesManager.getUserEmail() ?: "me"
        
        // Check if encryption is enabled for this conversation
        val conversation = messagesDao.getConversationById(conversationId)
        val isEncrypted = conversation?.isEncryptionEnabled ?: false
        
        // Encrypt caption if needed
        val messageContent = if (isEncrypted && caption.isNotEmpty()) {
            val key = getEncryptionKey(conversationId)
            MessageEncryption.encrypt(caption, key)
        } else {
            caption
        }
        
        // Create message ID
        val messageId = UUID.randomUUID().toString()
        
        // Save media to internal storage
        val media = mediaRepository.saveOutgoingMedia(
            uri = mediaUri,
            messageId = messageId,
            mimeType = mimeType,
            fileName = fileName
        )
        
        // Create message entity
        val message = MessageEntity(
            id = messageId,
            conversationId = conversationId,
            senderId = currentUserId,
            recipientId = recipientId,
            type = mediaType,
            content = messageContent,
            mediaId = media.mediaId,
            isEncrypted = isEncrypted,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT,
            direction = MessageDirection.OUTGOING,
            isSent = true,
            isDelivered = false,
            isRead = false,
            isMine = true
        )
        
        android.util.Log.d("MessagesRepository", "Inserting media message: ${message.id}")
        messagesDao.insertMessage(message)
        
        // Update conversation with last message
        val lastMessageText = when (mediaType) {
            MessageType.IMAGE -> "📷 Image"
            MessageType.VIDEO -> "🎥 Video"
            MessageType.FILE -> "📎 File"
            MessageType.CONTACT -> "👤 Contact"
            MessageType.AUDIO -> "🎵 Audio"
            else -> caption
        }
        
        messagesDao.updateConversationLastMessage(
            conversationId = conversationId,
            lastMessage = lastMessageText,
            lastMessageTime = message.timestamp
        )
        
        android.util.Log.d("MessagesRepository", "Media message sent successfully")
    }
    
    /**
     * Download media for a message
     */
    suspend fun downloadMedia(mediaId: String): Result<String> {
        return mediaRepository.downloadMedia(mediaId)
    }
    
    /**
     * Get or generate encryption key for a conversation
     */
    private fun getEncryptionKey(conversationId: String): SecretKey {
        return encryptionKeys.getOrPut(conversationId) {
            // In production, retrieve from Android KeyStore
            // For now, generate a new key
            MessageEncryption.generateKey()
        }
    }
}
