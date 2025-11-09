package com.tcc.tarasulandroid.data

import com.tcc.tarasulandroid.feature.home.model.Contact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing contacts.
 * This provides a single source of truth for contact data and can be easily
 * extended to support remote data sources, caching, etc.
 */
@Singleton
class ContactRepository @Inject constructor() {
    
    /**
     * Get all contacts as a Flow.
     * In a real app, this would fetch from a database or API.
     */
    fun getContacts(): Flow<List<Contact>> = flow {
        emit(getDummyContacts())
    }
    
    /**
     * Get a contact by ID.
     * In a real app, this would fetch from a database or API.
     */
    suspend fun getContactById(id: String): Contact? {
        return getDummyContacts().find { it.id == id }
    }
    
    /**
     * Dummy contacts for demo purposes.
     * In a real app, this would come from a database or API.
     */
    private fun getDummyContacts() = listOf(
        Contact(
            id = "1",
            name = "Alice Johnson",
            lastMessage = "Hey! How are you doing?",
            lastMessageTime = System.currentTimeMillis() - 300000,
            unreadCount = 2,
            isOnline = true
        ),
        Contact(
            id = "2",
            name = "Bob Smith",
            lastMessage = "Did you see the latest update?",
            lastMessageTime = System.currentTimeMillis() - 3600000,
            unreadCount = 0,
            isOnline = true
        ),
        Contact(
            id = "3",
            name = "Charlie Brown",
            lastMessage = "Thanks for your help!",
            lastMessageTime = System.currentTimeMillis() - 7200000,
            unreadCount = 1,
            isOnline = false
        ),
        Contact(
            id = "4",
            name = "Diana Prince",
            lastMessage = "Let's meet tomorrow at 3 PM",
            lastMessageTime = System.currentTimeMillis() - 86400000,
            unreadCount = 0,
            isOnline = false
        ),
        Contact(
            id = "5",
            name = "Edward Norton",
            lastMessage = "Great work on the project!",
            lastMessageTime = System.currentTimeMillis() - 172800000,
            unreadCount = 5,
            isOnline = true
        )
    )
}
