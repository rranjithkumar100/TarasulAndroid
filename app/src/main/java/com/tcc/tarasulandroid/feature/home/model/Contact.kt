package com.tcc.tarasulandroid.feature.home.model

data class Contact(
    val id: String,
    val name: String,
    val profilePicture: String? = null,
    val lastMessage: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isOnline: Boolean = false
)
