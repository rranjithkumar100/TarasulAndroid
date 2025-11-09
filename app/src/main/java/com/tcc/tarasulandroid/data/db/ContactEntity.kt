package com.tcc.tarasulandroid.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val phoneNumber: String,
    val photoUri: String?,
    val normalizedNumber: String,
    val lastModified: Long, // Timestamp from device
    val syncedAt: Long = System.currentTimeMillis() // When we synced this contact
)
