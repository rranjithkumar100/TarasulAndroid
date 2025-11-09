package com.tcc.tarasulandroid.data

import kotlinx.serialization.Serializable

/**
 * Contact information for sharing
 */
@Serializable
data class ContactInfo(
    val name: String,
    val phoneNumbers: List<String> = emptyList(),
    val photoUri: String? = null
) {
    fun toJsonString(): String {
        return kotlinx.serialization.json.Json.encodeToString(serializer(), this)
    }
    
    companion object {
        fun fromJsonString(json: String): ContactInfo? {
            return try {
                kotlinx.serialization.json.Json.decodeFromString(serializer(), json)
            } catch (e: Exception) {
                null
            }
        }
    }
}
