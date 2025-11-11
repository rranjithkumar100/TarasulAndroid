package com.tcc.tarasulandroid.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MessagesDao {
    
    // ==================== Messages ====================
    
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>>
    
    @androidx.room.Transaction
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesWithMediaForConversation(conversationId: String): Flow<List<com.tcc.tarasulandroid.data.MessageWithMedia>>
    
    @androidx.room.Transaction
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesWithReplyForConversation(conversationId: String): Flow<List<com.tcc.tarasulandroid.data.MessageWithReply>>
    
    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): MessageEntity?
    
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getMessagesForConversationSync(conversationId: String): List<MessageEntity>
    
    @androidx.room.Transaction
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getMessagesWithMediaPaginated(conversationId: String, limit: Int, offset: Int): List<com.tcc.tarasulandroid.data.MessageWithMedia>
    
    @androidx.room.Transaction
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getMessagesWithMediaAndReplyPaginated(conversationId: String, limit: Int, offset: Int): List<com.tcc.tarasulandroid.data.MessageWithMediaAndReply>
    
    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :conversationId")
    suspend fun getMessageCount(conversationId: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)
    
    @Update
    suspend fun updateMessage(message: MessageEntity)
    
    @Query("UPDATE messages SET isSent = :isSent WHERE id = :messageId")
    suspend fun updateMessageSentStatus(messageId: String, isSent: Boolean)
    
    @Query("UPDATE messages SET isDelivered = :isDelivered WHERE id = :messageId")
    suspend fun updateMessageDeliveredStatus(messageId: String, isDelivered: Boolean)
    
    @Query("UPDATE messages SET isRead = :isRead WHERE id = :messageId")
    suspend fun updateMessageReadStatus(messageId: String, isRead: Boolean)
    
    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)
    
    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteAllMessagesInConversation(conversationId: String)
    
    // ==================== Conversations ====================
    
    @Query("SELECT * FROM conversations ORDER BY lastMessageTime DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>
    
    @Query("SELECT * FROM conversations ORDER BY lastMessageTime DESC")
    suspend fun getAllConversationsSync(): List<ConversationEntity>
    
    @Query("SELECT * FROM conversations WHERE id = :conversationId")
    suspend fun getConversationById(conversationId: String): ConversationEntity?
    
    @Query("SELECT * FROM conversations WHERE contactId = :contactId")
    suspend fun getConversationByContactId(contactId: String): ConversationEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)
    
    @Update
    suspend fun updateConversation(conversation: ConversationEntity)
    
    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :conversationId")
    suspend fun markConversationAsRead(conversationId: String)
    
    @Query("UPDATE conversations SET lastMessage = :lastMessage, lastMessageTime = :lastMessageTime WHERE id = :conversationId")
    suspend fun updateConversationLastMessage(conversationId: String, lastMessage: String, lastMessageTime: Long)
    
    @Query("UPDATE conversations SET isEncryptionEnabled = :isEnabled WHERE id = :conversationId")
    suspend fun updateConversationEncryptionStatus(conversationId: String, isEnabled: Boolean)
    
    @Delete
    suspend fun deleteConversation(conversation: ConversationEntity)
    
    @Query("DELETE FROM conversations WHERE id = :conversationId")
    suspend fun deleteConversationById(conversationId: String)
}
