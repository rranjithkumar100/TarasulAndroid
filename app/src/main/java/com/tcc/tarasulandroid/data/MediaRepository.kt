package com.tcc.tarasulandroid.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.tcc.tarasulandroid.data.db.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mediaDao: MediaDao,
    private val okHttpClient: OkHttpClient
) {
    
    companion object {
        private const val TAG = "MediaRepository"
        private const val MEDIA_DIR = "media"
        private const val THUMBNAIL_DIR = "thumbnails"
    }
    
    private val mediaDir: File by lazy {
        File(context.filesDir, MEDIA_DIR).apply { mkdirs() }
    }
    
    private val thumbnailDir: File by lazy {
        File(context.filesDir, THUMBNAIL_DIR).apply { mkdirs() }
    }
    
    /**
     * Save a file from URI to internal storage for outgoing messages
     */
    suspend fun saveOutgoingMedia(
        uri: Uri,
        messageId: String,
        mimeType: String?,
        fileName: String?
    ): MediaEntity = withContext(Dispatchers.IO) {
        Log.d(TAG, "saveOutgoingMedia - uri: $uri, mimeType: $mimeType, fileName: $fileName")
        val mediaId = UUID.randomUUID().toString()
        
        // Copy file to internal storage
        val extension = fileName?.substringAfterLast('.', "") ?: getExtensionFromMimeType(mimeType)
        val targetFile = File(mediaDir, "$mediaId.$extension")
        
        Log.d(TAG, "Copying file to: ${targetFile.absolutePath}")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(targetFile).use { output ->
                input.copyTo(output)
            }
        }
        
        Log.d(TAG, "File copied successfully, size: ${targetFile.length()} bytes")
        
        // Calculate file metadata
        val fileSize = targetFile.length()
        val checksum = calculateChecksum(targetFile)
        
        // Extract additional metadata based on type
        val (width, height, durationMs) = extractMediaMetadata(uri, mimeType)
        
        val media = MediaEntity(
            mediaId = mediaId,
            messageId = messageId,
            localPath = targetFile.absolutePath,
            fileName = fileName ?: targetFile.name,
            mimeType = mimeType,
            fileSize = fileSize,
            checksum = checksum,
            width = width,
            height = height,
            durationMs = durationMs,
            storedInAppFiles = true,
            downloadStatus = DownloadStatus.DONE,
            downloadedAt = System.currentTimeMillis()
        )
        
        Log.d(TAG, "Inserting media into database: $mediaId")
        mediaDao.insertMedia(media)
        Log.d(TAG, "Saved outgoing media: $mediaId at ${targetFile.absolutePath}")
        
        media
    }
    
    /**
     * Download media from server URL for incoming messages
     */
    suspend fun downloadMedia(mediaId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val media = mediaDao.getMediaById(mediaId)
                ?: return@withContext Result.failure(Exception("Media not found: $mediaId"))
            
            if (media.localPath != null && File(media.localPath).exists()) {
                return@withContext Result.success(media.localPath)
            }
            
            val serverUrl = media.serverUrl
                ?: return@withContext Result.failure(Exception("No server URL for media: $mediaId"))
            
            // Update status to downloading
            mediaDao.updateDownloadStatus(mediaId, DownloadStatus.DOWNLOADING, 0)
            
            // Download file
            val request = Request.Builder().url(serverUrl).build()
            val response = okHttpClient.newCall(request).execute()
            
            if (!response.isSuccessful) {
                mediaDao.updateDownloadStatus(mediaId, DownloadStatus.FAILED, 0)
                return@withContext Result.failure(Exception("Download failed: ${response.code}"))
            }
            
            // Save to internal storage
            val extension = media.fileName?.substringAfterLast('.', "") 
                ?: getExtensionFromMimeType(media.mimeType)
            val targetFile = File(mediaDir, "$mediaId.$extension")
            
            response.body?.byteStream()?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalBytesRead = 0L
                    val totalBytes = response.body?.contentLength() ?: 0L
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        
                        // Update progress
                        if (totalBytes > 0) {
                            val progress = ((totalBytesRead * 100) / totalBytes).toInt()
                            mediaDao.updateDownloadStatus(mediaId, DownloadStatus.DOWNLOADING, progress)
                        }
                    }
                }
            }
            
            // Update media with local path
            mediaDao.updateLocalPath(
                mediaId = mediaId,
                localPath = targetFile.absolutePath,
                status = DownloadStatus.DONE,
                downloadedAt = System.currentTimeMillis()
            )
            
            Log.d(TAG, "Downloaded media: $mediaId to ${targetFile.absolutePath}")
            Result.success(targetFile.absolutePath)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading media: $mediaId", e)
            mediaDao.updateDownloadStatus(mediaId, DownloadStatus.FAILED, 0)
            Result.failure(e)
        }
    }
    
    /**
     * Get media by ID
     */
    suspend fun getMediaById(mediaId: String): MediaEntity? {
        return mediaDao.getMediaById(mediaId)
    }
    
    /**
     * Get media for a message
     */
    suspend fun getMediaByMessageId(messageId: String): MediaEntity? {
        return mediaDao.getMediaByMessageId(messageId)
    }
    
    /**
     * Get pending downloads
     */
    fun getPendingDownloads(): Flow<List<MediaEntity>> {
        return mediaDao.getMediaByDownloadStatus(DownloadStatus.PENDING)
    }
    
    /**
     * Delete media files and database entry
     */
    suspend fun deleteMedia(mediaId: String) = withContext(Dispatchers.IO) {
        val media = mediaDao.getMediaById(mediaId)
        media?.let {
            // Delete local files
            it.localPath?.let { path ->
                File(path).delete()
            }
            it.thumbnailPath?.let { path ->
                File(path).delete()
            }
            
            // Delete database entry
            mediaDao.deleteMedia(mediaId)
            Log.d(TAG, "Deleted media: $mediaId")
        }
    }
    
    /**
     * Cleanup orphaned media files
     */
    suspend fun cleanupOrphanedMedia() = withContext(Dispatchers.IO) {
        // Get all media IDs from database
        val allMedia = mediaDao.getMediaByDownloadStatuses(
            listOf(DownloadStatus.DONE, DownloadStatus.NOT_STARTED, DownloadStatus.PENDING, DownloadStatus.FAILED, DownloadStatus.DOWNLOADING)
        )
        val validPaths = allMedia.mapNotNull { it.localPath }.toSet()
        
        // Delete files not in database
        mediaDir.listFiles()?.forEach { file ->
            if (file.absolutePath !in validPaths) {
                file.delete()
                Log.d(TAG, "Cleaned up orphaned file: ${file.name}")
            }
        }
    }
    
    private fun calculateChecksum(file: File): String {
        val digest = MessageDigest.getInstance("MD5")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
    
    private fun getExtensionFromMimeType(mimeType: String?): String {
        return when (mimeType?.substringBefore('/')) {
            "image" -> when (mimeType.substringAfter('/')) {
                "jpeg", "jpg" -> "jpg"
                "png" -> "png"
                "gif" -> "gif"
                "webp" -> "webp"
                else -> "jpg"
            }
            "video" -> when (mimeType.substringAfter('/')) {
                "mp4" -> "mp4"
                "quicktime" -> "mov"
                "x-matroska" -> "mkv"
                else -> "mp4"
            }
            "audio" -> when (mimeType.substringAfter('/')) {
                "mpeg" -> "mp3"
                "mp4" -> "m4a"
                "ogg" -> "ogg"
                else -> "mp3"
            }
            else -> "bin"
        }
    }
    
    private fun extractMediaMetadata(uri: Uri, mimeType: String?): Triple<Int?, Int?, Long?> {
        // TODO: Use MediaMetadataRetriever for video/audio
        // TODO: Use BitmapFactory for images
        // For now, return nulls
        return Triple(null, null, null)
    }
}
