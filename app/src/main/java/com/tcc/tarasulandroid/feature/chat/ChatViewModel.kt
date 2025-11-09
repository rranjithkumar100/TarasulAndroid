package com.tcc.tarasulandroid.feature.chat

import androidx.lifecycle.ViewModel
import com.tcc.tarasulandroid.feature.home.model.Contact
import com.tcc.tarasulandroid.feature.home.model.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor() : ViewModel() {
    
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    
    fun loadMessages(contact: Contact) {
        // Load dummy messages for demo
        _messages.value = listOf(
            Message(
                id = "1",
                from = contact.name,
                to = "Me",
                text = "Hey! How are you doing?",
                time = System.currentTimeMillis() - 3600000
            ),
            Message(
                id = "2",
                from = "Me",
                to = contact.name,
                text = "Hi! I'm doing great, thanks for asking!",
                time = System.currentTimeMillis() - 3000000
            ),
            Message(
                id = "3",
                from = contact.name,
                to = "Me",
                text = "That's awesome! Want to catch up later?",
                time = System.currentTimeMillis() - 2400000
            ),
            Message(
                id = "4",
                from = "Me",
                to = contact.name,
                text = "Sure! What time works for you?",
                time = System.currentTimeMillis() - 1800000
            ),
            Message(
                id = "5",
                from = contact.name,
                to = "Me",
                text = "How about 5 PM?",
                time = System.currentTimeMillis() - 600000
            )
        )
    }
    
    fun sendMessage(contact: Contact, text: String) {
        val newMessage = Message(
            id = java.util.UUID.randomUUID().toString(),
            from = "Me",
            to = contact.name,
            text = text,
            time = System.currentTimeMillis()
        )
        _messages.value = _messages.value + newMessage
    }
}
