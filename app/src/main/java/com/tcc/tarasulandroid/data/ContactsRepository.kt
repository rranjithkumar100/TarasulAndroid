package com.tcc.tarasulandroid.data

import android.annotation.SuppressLint
import android.content.Context
import android.provider.ContactsContract
import com.tcc.tarasulandroid.data.db.ContactEntity
import com.tcc.tarasulandroid.data.db.ContactsDao
import com.tcc.tarasulandroid.feature.contacts.model.DeviceContact
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contactsDao: ContactsDao
) {
    
    /**
     * Get contacts from database as Flow (reactive updates)
     */
    fun getContactsFlow(): Flow<List<DeviceContact>> {
        return contactsDao.getAllContactsFlow().map { entities ->
            entities.map { it.toDeviceContact() }
        }
    }
    
    /**
     * Get cached contacts from database
     */
    suspend fun getCachedContacts(): List<DeviceContact> = withContext(Dispatchers.IO) {
        contactsDao.getAllContacts().map { it.toDeviceContact() }
    }
    
    /**
     * Search contacts in database
     */
    suspend fun searchContacts(query: String): List<DeviceContact> = withContext(Dispatchers.IO) {
        if (query.isBlank()) {
            contactsDao.getAllContacts().map { it.toDeviceContact() }
        } else {
            contactsDao.searchContacts(query).map { it.toDeviceContact() }
        }
    }
    
    /**
     * Get contact count from database
     */
    suspend fun getContactCount(): Int {
        return contactsDao.getContactCount()
    }
    
    /**
     * Sync contacts - intelligently syncs only new/modified contacts
     * Returns number of contacts synced
     */
    suspend fun syncContacts(forceFullSync: Boolean = false): SyncResult = withContext(Dispatchers.IO) {
        try {
            val lastSyncTime = if (forceFullSync) 0L else (contactsDao.getLastSyncTime() ?: 0L)
            val currentTime = System.currentTimeMillis()
            
            // Load contacts from device that were modified after last sync
            val newOrModifiedContacts = loadContactsFromDevice(lastSyncTime)
            
            // Save to database
            if (newOrModifiedContacts.isNotEmpty()) {
                contactsDao.insertContacts(newOrModifiedContacts)
            }
            
            // Get total count
            val totalCount = contactsDao.getContactCount()
            
            SyncResult(
                success = true,
                newContacts = newOrModifiedContacts.size,
                totalContacts = totalCount,
                isFullSync = forceFullSync || lastSyncTime == 0L
            )
        } catch (e: SecurityException) {
            SyncResult(success = false, error = "Permission denied")
        } catch (e: Exception) {
            e.printStackTrace()
            SyncResult(success = false, error = e.message)
        }
    }
    
    /**
     * Load contacts from device ContentResolver
     * Only loads contacts modified after the given timestamp (incremental sync)
     */
    @SuppressLint("Range")
    private suspend fun loadContactsFromDevice(modifiedSince: Long): List<ContactEntity> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<ContactEntity>()
        val seenIds = mutableSetOf<String>()
        val currentTime = System.currentTimeMillis()
        
        try {
            // Build selection clause for incremental sync
            val selection = if (modifiedSince > 0) {
                "${ContactsContract.CommonDataKinds.Phone.CONTACT_LAST_UPDATED_TIMESTAMP} > ?"
            } else null
            
            val selectionArgs = if (modifiedSince > 0) {
                arrayOf(modifiedSince.toString())
            } else null
            
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_LAST_UPDATED_TIMESTAMP
                ),
                selection,
                selectionArgs,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
                val modifiedColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_LAST_UPDATED_TIMESTAMP)
                
                while (cursor.moveToNext()) {
                    val id = cursor.getString(idColumn)
                    val name = cursor.getString(nameColumn) ?: "Unknown"
                    val number = cursor.getString(numberColumn) ?: ""
                    val photoUri = cursor.getString(photoColumn)
                    val lastModified = cursor.getLong(modifiedColumn)
                    
                    // Avoid duplicates (same contact with multiple numbers)
                    val contactKey = "$id-$number"
                    if (contactKey !in seenIds && number.isNotBlank()) {
                        seenIds.add(contactKey)
                        contacts.add(
                            ContactEntity(
                                id = "$id-${number.hashCode()}", // Unique ID combining contact ID and number
                                name = name,
                                phoneNumber = number,
                                photoUri = photoUri,
                                normalizedNumber = number.replace(Regex("[^0-9+]"), ""),
                                lastModified = lastModified,
                                syncedAt = currentTime
                            )
                        )
                    }
                }
            }
        } catch (e: SecurityException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        contacts
    }
    
    /**
     * Clear all cached contacts (useful for logout or force refresh)
     */
    suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            contactsDao.deleteAllContacts()
        }
    }
    
    // Extension function to convert entity to domain model
    private fun ContactEntity.toDeviceContact() = DeviceContact(
        id = id,
        name = name,
        phoneNumber = phoneNumber,
        photoUri = photoUri,
        normalizedNumber = normalizedNumber
    )
}

/**
 * Result of sync operation
 */
data class SyncResult(
    val success: Boolean,
    val newContacts: Int = 0,
    val totalContacts: Int = 0,
    val isFullSync: Boolean = false,
    val error: String? = null
)
