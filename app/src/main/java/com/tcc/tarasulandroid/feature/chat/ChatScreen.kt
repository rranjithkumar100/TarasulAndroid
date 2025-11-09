package com.tcc.tarasulandroid.feature.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import com.tcc.tarasulandroid.R
import com.tcc.tarasulandroid.core.*
import com.tcc.tarasulandroid.data.db.MessageType
import com.tcc.tarasulandroid.feature.home.model.Contact
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    contact: Contact,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var messageText by remember { mutableStateOf("") }
    var showMediaPicker by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // DI
    val messagesRepository = remember {
        val app = context.applicationContext as com.tcc.tarasulandroid.TarasulApplication
        dagger.hilt.android.EntryPointAccessors.fromApplication(
            app,
            com.tcc.tarasulandroid.di.MessagesRepositoryEntryPoint::class.java
        ).messagesRepository()
    }

    var conversationId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(contact.id) {
        try {
            val conversation = messagesRepository.getOrCreateConversation(
                contactId = contact.id,
                contactName = contact.name,
                contactPhoneNumber = ""
            )
            conversationId = conversation.id
        } catch (_: Exception) {}
    }

    // Fetch messages with media data
    val messagesWithMediaFromDb by messagesRepository
        .getMessagesWithMediaForConversation(conversationId ?: "")
        .collectAsState(initial = emptyList())

    val listState = rememberLazyListState()

    LaunchedEffect(messagesWithMediaFromDb.size) {
        if (messagesWithMediaFromDb.isNotEmpty()) {
            listState.animateScrollToItem(messagesWithMediaFromDb.size - 1)
        }
    }
    
    // Permission states
    val cameraPermissionState = rememberMultiplePermissionsState(
        permissions = MediaPermissions.getCameraPermissions()
    )
    
    val mediaPermissionsState = rememberMultiplePermissionsState(
        permissions = MediaPermissions.getMediaPermissions()
    )
    
    val contactsPermissionState = rememberMultiplePermissionsState(
        permissions = MediaPermissions.getContactsPermissions()
    )
    
    // Media picker launchers
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = PickImageContract()
    ) { uri ->
        uri?.let { 
            coroutineScope.launch {
                try {
                    messagesRepository.sendMediaMessage(
                        conversationId = conversationId ?: return@launch,
                        recipientId = contact.id,
                        mediaType = MessageType.IMAGE,
                        mediaUri = it,
                        mimeType = context.contentResolver.getType(it),
                        fileName = MediaPickerHelper.getFileName(context, it)
                    )
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }
    
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = PickVideoContract()
    ) { uri ->
        uri?.let { 
            coroutineScope.launch {
                try {
                    messagesRepository.sendMediaMessage(
                        conversationId = conversationId ?: return@launch,
                        recipientId = contact.id,
                        mediaType = MessageType.VIDEO,
                        mediaUri = it,
                        mimeType = context.contentResolver.getType(it),
                        fileName = MediaPickerHelper.getFileName(context, it)
                    )
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }
    
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = PickFileContract()
    ) { uri ->
        uri?.let { 
            coroutineScope.launch {
                try {
                    messagesRepository.sendMediaMessage(
                        conversationId = conversationId ?: return@launch,
                        recipientId = contact.id,
                        mediaType = MessageType.FILE,
                        mediaUri = it,
                        mimeType = context.contentResolver.getType(it),
                        fileName = MediaPickerHelper.getFileName(context, it)
                    )
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = TakePhotoContract()
    ) { _ ->
        cameraImageUri?.let { uri ->
            coroutineScope.launch {
                try {
                    messagesRepository.sendMediaMessage(
                        conversationId = conversationId ?: return@launch,
                        recipientId = contact.id,
                        mediaType = MessageType.IMAGE,
                        mediaUri = uri,
                        mimeType = "image/jpeg",
                        fileName = "camera_${System.currentTimeMillis()}.jpg"
                    )
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }
    
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = PickContactContract()
    ) { uri ->
        uri?.let {
            // Clear focus from TextField to prevent crash
            coroutineScope.launch {
                try {
                    // Extract full contact info
                    var contactName = "Unknown Contact"
                    var contactId: String? = null
                    val phoneNumbers = mutableListOf<String>()
                    
                    // Query contact basic info
                    context.contentResolver.query(
                        uri,
                        arrayOf(
                            android.provider.ContactsContract.Contacts._ID,
                            android.provider.ContactsContract.Contacts.DISPLAY_NAME,
                            android.provider.ContactsContract.Contacts.HAS_PHONE_NUMBER
                        ),
                        null, null, null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val idIndex = cursor.getColumnIndex(android.provider.ContactsContract.Contacts._ID)
                            val nameIndex = cursor.getColumnIndex(android.provider.ContactsContract.Contacts.DISPLAY_NAME)
                            
                            if (idIndex >= 0) contactId = cursor.getString(idIndex)
                            if (nameIndex >= 0) contactName = cursor.getString(nameIndex)
                        }
                    }
                    
                    // Query phone numbers
                    contactId?.let { id ->
                        context.contentResolver.query(
                            android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            arrayOf(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER),
                            "${android.provider.ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                            arrayOf(id),
                            null
                        )?.use { cursor ->
                            val phoneIndex = cursor.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)
                            while (cursor.moveToNext() && phoneIndex >= 0) {
                                phoneNumbers.add(cursor.getString(phoneIndex))
                            }
                        }
                    }
                    
                    // Create contact info JSON
                    val contactInfo = com.tcc.tarasulandroid.data.ContactInfo(
                        name = contactName,
                        phoneNumbers = phoneNumbers,
                        photoUri = uri.toString()
                    )
                    
                    // Send as contact message
                    messagesRepository.sendMessage(
                        conversationId = conversationId ?: return@launch,
                        content = contactInfo.toJsonString(),
                        recipientId = contact.id
                    )
                } catch (e: Exception) {
                    android.util.Log.e("ChatScreen", "Error sending contact", e)
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        // Use default insets for top (safe choice). We’ll handle bottom/IME in bottomBar only.
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
            // ⬇️ This is the key: imePadding() for keyboard, safeDrawing bottom for gestures when keyboard hidden
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .imePadding()
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Attachment button
                    IconButton(
                        onClick = { showMediaPicker = true }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_attach),
                            contentDescription = "Attach media",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
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
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = {
                            if (messageText.isNotBlank() && conversationId != null) {
                                val textToSend = messageText.trim()
                                messageText = ""
                                coroutineScope.launch {
                                    try {
                                        messagesRepository.sendMessage(
                                            conversationId = conversationId!!,
                                            content = textToSend,
                                            recipientId = contact.id
                                        )
                                    } catch (_: Exception) { }
                                }
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
        // Content area — no IME/nav paddings here
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                state = listState,
                reverseLayout = false,
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(items = messagesWithMediaFromDb, key = { it.message.id }) { messageWithMedia ->
                    // Use new MessageBubble that supports media
                    com.tcc.tarasulandroid.feature.chat.MessageBubble(
                        messageWithMedia = messageWithMedia,
                        onDownloadClick = { mediaId ->
                            coroutineScope.launch {
                                messagesRepository.downloadMedia(mediaId)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
    
    // Media picker bottom sheet
    if (showMediaPicker) {
        MediaPickerBottomSheet(
            onDismiss = { showMediaPicker = false },
            onCameraClick = {
                if (cameraPermissionState.allPermissionsGranted) {
                    cameraImageUri = MediaPickerHelper.createTempImageUri(context)
                    cameraImageUri?.let { cameraLauncher.launch(it) }
                } else {
                    cameraPermissionState.requestPermissions()
                }
            },
            onGalleryClick = {
                if (mediaPermissionsState.allPermissionsGranted) {
                    imagePickerLauncher.launch(Unit)
                } else {
                    mediaPermissionsState.requestPermissions()
                }
            },
            onVideoClick = {
                if (mediaPermissionsState.allPermissionsGranted) {
                    videoPickerLauncher.launch(Unit)
                } else {
                    mediaPermissionsState.requestPermissions()
                }
            },
            onFileClick = {
                filePickerLauncher.launch("*/*")
            },
            onContactClick = {
                // Dismiss bottom sheet first
                showMediaPicker = false
                // Launch contact picker after a short delay to avoid TextField issues
                coroutineScope.launch {
                    kotlinx.coroutines.delay(100)
                    if (contactsPermissionState.allPermissionsGranted) {
                        contactPickerLauncher.launch(Unit)
                    } else {
                        contactsPermissionState.requestPermissions()
                    }
                }
            }
        )
    }
}

// Old MessageBubble removed - now using the one from MessageBubble.kt that supports media
