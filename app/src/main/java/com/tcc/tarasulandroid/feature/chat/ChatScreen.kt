package com.tcc.tarasulandroid.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tcc.tarasulandroid.R
import com.tcc.tarasulandroid.feature.home.model.Contact
import com.tcc.tarasulandroid.feature.home.model.Message
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    contact: Contact,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var messageText by remember { mutableStateOf("") }
    
    // Get MessagesRepository
    val messagesRepository = remember {
        val app = context.applicationContext as com.tcc.tarasulandroid.TarasulApplication
        dagger.hilt.android.EntryPointAccessors.fromApplication(
            app,
            com.tcc.tarasulandroid.di.MessagesRepositoryEntryPoint::class.java
        ).messagesRepository()
    }
    
    // Load messages from database
    val conversationId = remember { contact.id }
    val messagesFromDb by messagesRepository.getMessagesForConversation(conversationId)
        .collectAsState(initial = emptyList())
    
    // Convert DB messages to UI messages
    val messages = remember(messagesFromDb) {
        messagesFromDb.map { msg ->
            Message(
                id = msg.id,
                from = if (msg.isMine) "Me" else contact.name,
                to = if (msg.isMine) contact.name else "Me",
                text = msg.content,
                time = msg.timestamp
            )
        }.toMutableStateList()
    }
    
    // Dummy messages for initial demo (only if DB is empty)
    LaunchedEffect(messagesFromDb.isEmpty()) {
        if (messagesFromDb.isEmpty()) {
            // Add some dummy messages only on first load
            val dummyMessages = listOf(
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
            ))
            // Don't add dummy messages to state, just for demo
        }
    }

    val listState = rememberLazyListState()
    
    // Auto-scroll to bottom when new message is added or keyboard appears
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = contact.name.first().toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (contact.isOnline) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                        .align(Alignment.BottomEnd)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = contact.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (contact.isOnline) 
                                    stringResource(R.string.online) 
                                else 
                                    stringResource(R.string.offline),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.navigationBarsPadding() // Add navigation bar padding
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(stringResource(R.string.type_message)) },
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                // Send message via repository
                                kotlinx.coroutines.GlobalScope.launch {
                                    try {
                                        messagesRepository.sendMessage(
                                            conversationId = conversationId,
                                            content = messageText,
                                            recipientId = contact.id
                                        )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                messageText = ""
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(R.string.send),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .imePadding(), // Add IME padding to messages list only
            state = listState,
            reverseLayout = false,
            contentPadding = PaddingValues(bottom = 8.dp) // Add bottom padding for last message
        ) {
            items(messages) { message ->
                MessageBubble(message = message)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun MessageBubble(message: Message) {
    val isMe = message.from == "Me"
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 16.dp
            ),
            color = if (isMe) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isMe) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.time)),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
