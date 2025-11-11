package com.tcc.tarasulandroid.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "media",
    indices = [
        Index(value = ["messageId"]),
        Index(value = ["downloadStatus"])
    ]
)
data class MediaEntity(
    @PrimaryKey
    val mediaId: String,
    
    val messageId: String, // Foreign key to MessageEntity
    
    // Server information
    val serverUrl: String? = null, // URL to download from (for incoming media)
    
    // Local storage
    val localPath: String? = null, // Absolute path to file in internal storage
    val thumbnailPath: String? = null, // Thumbnail for images/videos
    
    // File metadata
    val fileName: String? = null,
    val mimeType: String? = null,
    val fileSize: Long? = null, // Size in bytes
    val checksum: String? = null, // MD5/SHA256 for integrity
    
    // Media-specific metadata
    val width: Int? = null, // For images/videos
    val height: Int? = null, // For images/videos
    val durationMs: Long? = null, // For audio/video in milliseconds
    
    // Storage flags
    val storedInAppFiles: Boolean = true, // True if stored in app internal storage
    
    // Download management
    val downloadStatus: DownloadStatus = DownloadStatus.NOT_STARTED,
    val downloadProgress: Int = 0, // 0-100
    val downloadedAt: Long? = null // Timestamp when download completed
)
