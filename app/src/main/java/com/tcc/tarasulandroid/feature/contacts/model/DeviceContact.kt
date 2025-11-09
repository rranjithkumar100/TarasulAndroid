package com.tcc.tarasulandroid.feature.contacts.model

data class DeviceContact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val photoUri: String? = null,
    val normalizedNumber: String = phoneNumber.replace(Regex("[^0-9+]"), "")
)
