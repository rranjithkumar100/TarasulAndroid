package com.tcc.tarasulandroid.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.FileProvider
import java.io.File

/**
 * Contract for picking images from gallery
 */
class PickImageContract : ActivityResultContract<Unit, Uri?>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
    }
    
    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return intent?.data
    }
}

/**
 * Contract for picking videos from gallery
 */
class PickVideoContract : ActivityResultContract<Unit, Uri?>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent(Intent.ACTION_PICK).apply {
            type = "video/*"
        }
    }
    
    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return intent?.data
    }
}

/**
 * Contract for picking any file
 */
class PickFileContract : ActivityResultContract<String, Uri?>() {
    override fun createIntent(context: Context, input: String): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            type = input // MIME type filter (e.g., "*/*" for all files)
            addCategory(Intent.CATEGORY_OPENABLE)
        }
    }
    
    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return intent?.data
    }
}

/**
 * Contract for taking a photo with camera
 */
class TakePhotoContract : ActivityResultContract<Uri, Uri?>() {
    override fun createIntent(context: Context, input: Uri): Intent {
        return Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, input)
        }
    }
    
    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        // Return the URI passed in createIntent
        return null // Handled externally
    }
}

/**
 * Contract for recording video with camera
 */
class RecordVideoContract : ActivityResultContract<Uri, Uri?>() {
    override fun createIntent(context: Context, input: Uri): Intent {
        return Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, input)
        }
    }
    
    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return null // Handled externally
    }
}

/**
 * Contract for picking a contact
 */
class PickContactContract : ActivityResultContract<Unit, Uri?>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent(Intent.ACTION_PICK).apply {
            type = ContactsContract.Contacts.CONTENT_TYPE
        }
    }
    
    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return intent?.data
    }
}

/**
 * Helper to create temporary file URIs for camera capture
 */
object MediaPickerHelper {
    
    fun createTempImageUri(context: Context): Uri {
        val tempFile = File.createTempFile(
            "camera_image_${System.currentTimeMillis()}",
            ".jpg",
            context.cacheDir
        )
        
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    }
    
    fun createTempVideoUri(context: Context): Uri {
        val tempFile = File.createTempFile(
            "camera_video_${System.currentTimeMillis()}",
            ".mp4",
            context.cacheDir
        )
        
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    }
    
    fun getFileName(context: Context, uri: Uri): String? {
        var fileName: String? = null
        
        // Try to get filename from content resolver
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val displayNameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    fileName = cursor.getString(displayNameIndex)
                }
            }
        }
        
        // Fallback to URI path
        return fileName ?: uri.lastPathSegment
    }
    
    fun getMimeType(context: Context, uri: Uri): String? {
        return context.contentResolver.getType(uri)
    }
}
