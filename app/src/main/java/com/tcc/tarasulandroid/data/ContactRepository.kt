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
     * Empty list - conversations will come from message database
     */
    fun getContacts(): Flow<List<Contact>> = flow {
        emit(emptyList())
    }
}
