package com.tcc.tarasulandroid.data

import androidx.room.Embedded
import androidx.room.Relation
import com.tcc.tarasulandroid.data.db.MediaEntity
import com.tcc.tarasulandroid.data.db.MessageEntity

/**
 * Data class combining Message with its Media for convenient querying
 */
data class MessageWithMedia(
    @Embedded val message: MessageEntity,
    
    @Relation(
        parentColumn = "mediaId",
        entityColumn = "mediaId"
    )
    val media: MediaEntity? = null
)
