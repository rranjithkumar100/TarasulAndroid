package com.tcc.tarasulandroid.feature.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcc.tarasulandroid.data.ContactsRepository
import com.tcc.tarasulandroid.feature.contacts.model.DeviceContact
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactsRepository: ContactsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ContactsUiState())
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    init {
        // Observe contacts from database
        viewModelScope.launch {
            contactsRepository.getContactsFlow()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { contacts ->
                    _uiState.update { it.copy(contacts = contacts) }
                }
        }
        
        // Observe search query and filter contacts
        viewModelScope.launch {
            _searchQuery
                .debounce(300) // Debounce search input
                .distinctUntilChanged()
                .collect { query ->
                    searchContacts(query)
                }
        }
    }
    
    fun onPermissionGranted() {
        _uiState.update { it.copy(hasPermission = true, permissionDenied = false) }
        syncContacts()
    }
    
    fun onPermissionDenied() {
        _uiState.update { it.copy(hasPermission = false, permissionDenied = true) }
    }
    
    fun syncContacts(forceFullSync: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, error = null) }
            
            try {
                val result = contactsRepository.syncContacts(forceFullSync)
                
                if (result.success) {
                    val message = if (result.isFullSync) {
                        "Synced ${result.totalContacts} contacts"
                    } else if (result.newContacts > 0) {
                        "Synced ${result.newContacts} new contacts (${result.totalContacts} total)"
                    } else {
                        "All contacts are up to date (${result.totalContacts} total)"
                    }
                    
                    _uiState.update { 
                        it.copy(
                            isSyncing = false,
                            lastSyncMessage = message
                        ) 
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isSyncing = false,
                            error = result.error ?: "Sync failed"
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isSyncing = false,
                        error = e.message ?: "Failed to sync contacts"
                    ) 
                }
            }
        }
    }
    
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
    
    private fun searchContacts(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            
            try {
                val results = contactsRepository.searchContacts(query)
                _uiState.update { 
                    it.copy(
                        filteredContacts = results,
                        isSearching = false
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isSearching = false,
                        error = e.message
                    ) 
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun clearSyncMessage() {
        _uiState.update { it.copy(lastSyncMessage = null) }
    }
}

data class ContactsUiState(
    val contacts: List<DeviceContact> = emptyList(),
    val filteredContacts: List<DeviceContact>? = null, // null means no search active
    val hasPermission: Boolean = false,
    val permissionDenied: Boolean = false,
    val isSyncing: Boolean = false,
    val isSearching: Boolean = false,
    val error: String? = null,
    val lastSyncMessage: String? = null
) {
    val displayContacts: List<DeviceContact>
        get() = filteredContacts ?: contacts
}
