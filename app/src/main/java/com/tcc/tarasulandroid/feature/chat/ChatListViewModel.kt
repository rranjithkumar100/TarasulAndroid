package com.tcc.tarasulandroid.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcc.tarasulandroid.data.ContactRepository
import com.tcc.tarasulandroid.feature.home.model.Contact
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val contactRepository: ContactRepository
) : ViewModel() {
    
    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadContacts()
    }
    
    private fun loadContacts() {
        viewModelScope.launch {
            _isLoading.value = true
            contactRepository.getContacts().collect { contactList ->
                _contacts.value = contactList
                _isLoading.value = false
            }
        }
    }
    
    fun refreshContacts() {
        loadContacts()
    }
}
