package com.tcc.tarasulandroid.data

import androidx.room.Embedded
import androidx.room.Relation
import com.tcc.tarasulandroid.data.db.MediaEntity
import com.tcc.tarasulandroid.data.db.MessageEntity

/**
 * Data class combining Message with its Media and Reply information
 */
data class MessageWithMediaAndReply(
    @Embedded val message: MessageEntity,
    
    @Relation(
        parentColumn = "mediaId",
        entityColumn = "mediaId"
    )
    val media: MediaEntity? = null,
    
    @Relation(
        parentColumn = "replyToMessageId",
        entityColumn = "id"
    )
    val replyToMessage: MessageEntity? = null
)
