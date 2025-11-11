package com.tcc.tarasulandroid.data.db

import androidx.room.TypeConverter

class Converters {
    
    @TypeConverter
    fun fromMessageType(value: MessageType): String {
        return value.name
    }
    
    @TypeConverter
    fun toMessageType(value: String): MessageType {
        return MessageType.valueOf(value)
    }
    
    @TypeConverter
    fun fromMessageStatus(value: MessageStatus): String {
        return value.name
    }
    
    @TypeConverter
    fun toMessageStatus(value: String): MessageStatus {
        return MessageStatus.valueOf(value)
    }
    
    @TypeConverter
    fun fromMessageDirection(value: MessageDirection): String {
        return value.name
    }
    
    @TypeConverter
    fun toMessageDirection(value: String): MessageDirection {
        return MessageDirection.valueOf(value)
    }
    
    @TypeConverter
    fun fromDownloadStatus(value: DownloadStatus): String {
        return value.name
    }
    
    @TypeConverter
    fun toDownloadStatus(value: String): DownloadStatus {
        return DownloadStatus.valueOf(value)
    }
}
