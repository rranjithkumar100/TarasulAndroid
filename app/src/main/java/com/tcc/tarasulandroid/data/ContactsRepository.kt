package com.tcc.tarasulandroid.data

import android.annotation.SuppressLint
import android.content.Context
import android.provider.ContactsContract
import com.tcc.tarasulandroid.feature.contacts.model.DeviceContact
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    @SuppressLint("Range")
    suspend fun loadContacts(): List<DeviceContact> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<DeviceContact>()
        val seenIds = mutableSetOf<String>()
        
        try {
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.PHOTO_URI
                ),
                null,
                null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
                
                while (cursor.moveToNext()) {
                    val id = cursor.getString(idColumn)
                    val name = cursor.getString(nameColumn) ?: "Unknown"
                    val number = cursor.getString(numberColumn) ?: ""
                    val photoUri = cursor.getString(photoColumn)
                    
                    // Avoid duplicates (same contact with multiple numbers)
                    val contactKey = "$id-$number"
                    if (contactKey !in seenIds && number.isNotBlank()) {
                        seenIds.add(contactKey)
                        contacts.add(
                            DeviceContact(
                                id = id,
                                name = name,
                                phoneNumber = number,
                                photoUri = photoUri
                            )
                        )
                    }
                }
            }
        } catch (e: SecurityException) {
            // Permission not granted
            return@withContext emptyList()
        } catch (e: Exception) {
            // Other errors
            e.printStackTrace()
            return@withContext emptyList()
        }
        
        contacts.distinctBy { it.id + it.phoneNumber }
    }
}
