package com.tcc.tarasulandroid.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    
    @Query("SELECT * FROM media WHERE mediaId = :mediaId")
    suspend fun getMediaById(mediaId: String): MediaEntity?
    
    @Query("SELECT * FROM media WHERE messageId = :messageId")
    suspend fun getMediaByMessageId(messageId: String): MediaEntity?
    
    @Query("SELECT * FROM media WHERE downloadStatus = :status")
    fun getMediaByDownloadStatus(status: DownloadStatus): Flow<List<MediaEntity>>
    
    @Query("SELECT * FROM media WHERE downloadStatus IN (:statuses)")
    suspend fun getMediaByDownloadStatuses(statuses: List<DownloadStatus>): List<MediaEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: MediaEntity)
    
    @Update
    suspend fun updateMedia(media: MediaEntity)
    
    @Query("UPDATE media SET downloadStatus = :status, downloadProgress = :progress WHERE mediaId = :mediaId")
    suspend fun updateDownloadStatus(mediaId: String, status: DownloadStatus, progress: Int)
    
    @Query("UPDATE media SET localPath = :localPath, downloadStatus = :status, downloadedAt = :downloadedAt WHERE mediaId = :mediaId")
    suspend fun updateLocalPath(mediaId: String, localPath: String, status: DownloadStatus, downloadedAt: Long)
    
    @Query("DELETE FROM media WHERE mediaId = :mediaId")
    suspend fun deleteMedia(mediaId: String)
    
    @Query("DELETE FROM media WHERE messageId = :messageId")
    suspend fun deleteMediaByMessageId(messageId: String)
}
