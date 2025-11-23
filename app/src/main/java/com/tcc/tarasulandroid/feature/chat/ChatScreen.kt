package com.tcc.tarasulandroid.feature.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.tcc.tarasulandroid.core.*
import com.tcc.tarasulandroid.data.MessageWithMediaAndReply
import com.tcc.tarasulandroid.feature.chat.components.*
import com.tcc.tarasulandroid.feature.chat.models.ReplyMessage
import com.tcc.tarasulandroid.feature.home.model.Contact
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    contact: Contact,
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit = {},
    onImageClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State
    var messageText by remember { mutableStateOf("") }
    var showMediaPicker by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImagePreview by remember { mutableStateOf(false) }
    var selectedImagePath by remember { mutableStateOf<String?>(null) }
    var conversationId by remember { mutableStateOf<String?>(null) }
    var messages by remember { mutableStateOf<List<MessageWithMediaAndReply>>(emptyList()) }
    var isLoadingMessages by remember { mutableStateOf(false) }
    var hasMoreMessages by remember { mutableStateOf(true) }
    var currentOffset by remember { mutableStateOf(0) }
    var shouldAutoScroll by remember { mutableStateOf(true) }
    var replyToMessage by remember { mutableStateOf<ReplyMessage?>(null) }
    var isFirstLoad by remember { mutableStateOf(true) }
    var pendingMediaAction by remember { mutableStateOf<String?>(null) }
    var userHasScrolled by remember { mutableStateOf(false) }
    var isPaginating by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val pageSize = 20

    // Repository
    val messagesRepository = remember {
        val app = context.applicationContext as com.tcc.tarasulandroid.TarasulApplication
        dagger.hilt.android.EntryPointAccessors.fromApplication(
            app,
            com.tcc.tarasulandroid.di.MessagesRepositoryEntryPoint::class.java
        ).messagesRepository()
    }

    // Permissions
    val cameraPermissionState = rememberMultiplePermissionsState(
        permissions = MediaPermissions.getCameraPermissions()
    )
    val mediaPermissionsState = rememberMultiplePermissionsState(
        permissions = MediaPermissions.getMediaPermissions()
    )
    val contactsPermissionState = rememberMultiplePermissionsState(
        permissions = MediaPermissions.getContactsPermissions()
    )

    // Media launchers
    val imagePickerLauncher = rememberLauncherForActivityResult(PickImageContract()) { uri ->
        uri?.let {
            android.util.Log.d("ChatScreen", "Image picker callback - URI: $uri")
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                android.util.Log.e("ChatScreen", "Failed to take persistable permission", e)
            }

            conversationId?.let { convId ->
                coroutineScope.launch {
                    try {
                        android.util.Log.d("ChatScreen", "Sending image: $uri")
                        messagesRepository.sendMediaMessage(
                            conversationId = convId,
                            recipientId = contact.id,
                            mediaUri = uri,
                            mediaType = com.tcc.tarasulandroid.data.db.MessageType.IMAGE,
                            caption = replyToMessage?.messageId ?: ""
                        )
                        android.util.Log.d("ChatScreen", "Image sent successfully")
                        reloadMessages(messagesRepository, convId, pageSize, userHasScrolled) {
                            messages = it.first
                            currentOffset = it.second
                            hasMoreMessages = it.third
                            isFirstLoad = false
                            shouldAutoScroll = it.fourth
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ChatScreen", "Error sending image", e)
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(PickVideoContract()) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                android.util.Log.e("ChatScreen", "Failed to take persistable permission", e)
            }

            conversationId?.let { convId ->
                coroutineScope.launch {
                    try {
                        messagesRepository.sendMediaMessage(
                            conversationId = convId,
                            recipientId = contact.id,
                            mediaUri = uri,
                            mediaType = com.tcc.tarasulandroid.data.db.MessageType.VIDEO,
                            caption = replyToMessage?.messageId ?: ""
                        )
                        reloadMessages(messagesRepository, convId, pageSize, userHasScrolled) {
                            messages = it.first
                            currentOffset = it.second
                            hasMoreMessages = it.third
                            isFirstLoad = false
                            shouldAutoScroll = it.fourth
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ChatScreen", "Error sending video", e)
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(PickFileContract()) { uri ->
        uri?.let {
            conversationId?.let { convId ->
                coroutineScope.launch {
                    try {
                        messagesRepository.sendMediaMessage(
                            conversationId = convId,
                            recipientId = contact.id,
                            mediaUri = uri,
                            mediaType = com.tcc.tarasulandroid.data.db.MessageType.FILE,
                            caption = replyToMessage?.messageId ?: ""
                        )
                        reloadMessages(messagesRepository, convId, pageSize, userHasScrolled) {
                            messages = it.first
                            currentOffset = it.second
                            hasMoreMessages = it.third
                            isFirstLoad = false
                            shouldAutoScroll = it.fourth
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ChatScreen", "Error sending file", e)
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            conversationId?.let { convId ->
                coroutineScope.launch {
                    try {
                        messagesRepository.sendMediaMessage(
                            conversationId = convId,
                            recipientId = contact.id,
                            mediaUri = cameraImageUri!!,
                            mediaType = com.tcc.tarasulandroid.data.db.MessageType.IMAGE,
                            caption = replyToMessage?.messageId ?: ""
                        )
                        reloadMessages(messagesRepository, convId, pageSize, userHasScrolled) {
                            messages = it.first
                            currentOffset = it.second
                            hasMoreMessages = it.third
                            isFirstLoad = false
                            shouldAutoScroll = it.fourth
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ChatScreen", "Error sending camera image", e)
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    val contactPickerLauncher = rememberLauncherForActivityResult(PickContactContract()) { uri ->
        uri?.let {
            conversationId?.let { convId ->
                coroutineScope.launch {
                    try {
                        val contactInfo = MediaPickerHelper.getContactInfo(context, uri)
                        if (contactInfo != null) {
                            android.util.Log.d("ChatScreen", "Sending contact: ${contactInfo.name}")
                            messagesRepository.sendContactMessage(
                                conversationId = convId,
                                contactInfo = contactInfo,
                                recipientId = contact.id,
                                replyToMessageId = replyToMessage?.messageId
                            )
                            android.util.Log.d("ChatScreen", "Contact sent successfully")
                            reloadMessages(messagesRepository, convId, pageSize, userHasScrolled) {
                                messages = it.first
                                currentOffset = it.second
                                hasMoreMessages = it.third
                                isFirstLoad = false
                                shouldAutoScroll = it.fourth
                            }
                        } else {
                            android.util.Log.e("ChatScreen", "Failed to extract contact information")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ChatScreen", "Error sending contact", e)
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    // Load conversation and messages
    LaunchedEffect(contact.id) {
        try {
            val conversation = messagesRepository.getOrCreateConversation(
                contactId = contact.id,
                contactName = contact.name,
                contactPhoneNumber = ""
            )
            conversationId = conversation.id

            if (conversationId != null) {
                isLoadingMessages = true
                val initialMessages = messagesRepository.getMessagesWithMediaAndReplyPaginated(
                    conversationId = conversationId!!,
                    limit = pageSize,
                    offset = 0
                )
                messages = initialMessages
                currentOffset = initialMessages.size

                val totalCount = messagesRepository.getMessageCount(conversationId!!)
                hasMoreMessages = currentOffset < totalCount

                isLoadingMessages = false
                shouldAutoScroll = true

                android.util.Log.d("ChatScreen", "📱 INITIAL LOAD: ${messages.size} messages loaded")
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatScreen", "Error loading conversation", e)
            isLoadingMessages = false
        }
    }

    // Detect when user manually scrolls to disable auto-scroll PERMANENTLY
    LaunchedEffect(listState) {
        snapshotFlow {
            listState.firstVisibleItemIndex to listState.isScrollInProgress
        }
            // Add debouncing to reduce recompositions
            .collect { (firstIndex, isScrolling) ->
                // Skip monitoring during pagination
                if (isPaginating) {
                    return@collect
                }

                if (messages.isNotEmpty() && !isFirstLoad) {
                    val lastIndex = messages.size - 1
                    val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    val isAtBottom = lastVisibleIndex >= lastIndex - 1

                    // Only log significant events
                    if (isScrolling && !isAtBottom && !userHasScrolled) {
                        android.util.Log.d("ChatScreen", "🔴 USER SCROLLED AWAY")
                        userHasScrolled = true
                        shouldAutoScroll = false
                    }

                    // Check if viewing old messages
                    if (!isAtBottom && firstIndex < lastIndex - 5 && !userHasScrolled) {
                        userHasScrolled = true
                        shouldAutoScroll = false
                    }
                }
            }
    }

    // Pagination detection - OPTIMIZED
    LaunchedEffect(listState) {
        snapshotFlow {
            listState.firstVisibleItemIndex
        }
            .collect { firstVisibleIndex ->
                if (firstVisibleIndex <= 3 &&
                    hasMoreMessages &&
                    !isLoadingMessages &&
                    !isFirstLoad &&
                    !isPaginating) {

                    android.util.Log.d("ChatScreen", "📄 Loading older messages...")

                    isPaginating = true
                    isLoadingMessages = true
                    conversationId?.let { convId ->
                        try {
                            val currentFirstIndex = firstVisibleIndex
                            val currentScrollOffset = listState.firstVisibleItemScrollOffset

                            val olderMessages = messagesRepository.getMessagesWithMediaAndReplyPaginated(
                                conversationId = convId,
                                limit = pageSize,
                                offset = currentOffset
                            )

                            if (olderMessages.isNotEmpty()) {
                                messages = olderMessages + messages
                                currentOffset += olderMessages.size

                                val totalCount = messagesRepository.getMessageCount(convId)
                                hasMoreMessages = currentOffset < totalCount

                                // Wait for recomposition
                                //kotlinx.coroutines.delay(16) // One frame at 60fps

                                // Maintain scroll position
                                val newIndex = currentFirstIndex + olderMessages.size
                                listState.scrollToItem(
                                    index = newIndex,
                                    scrollOffset = currentScrollOffset
                                )

                                android.util.Log.d("ChatScreen", "📄 Loaded ${olderMessages.size} messages")
                            } else {
                                hasMoreMessages = false
                            }
                        } catch (e: java.util.concurrent.CancellationException) {
                            android.util.Log.d("ChatScreen", "⚠️ Pagination cancelled")
                        } catch (e: Exception) {
                            android.util.Log.e("ChatScreen", "❌ Pagination error", e)
                        } finally {
                            isLoadingMessages = false
                            //kotlinx.coroutines.delay(100)
                            isPaginating = false
                        }
                    }
                }
            }
    }

    // Auto-scroll - ONLY on first load or when sending new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty() && shouldAutoScroll && !userHasScrolled) {
            try {
                val targetIndex = messages.size - 1

                if (isFirstLoad) {
                    // Initial load - wait for layout
                    //kotlinx.coroutines.delay(300)
                    listState.scrollToItem(targetIndex)

                    // Extra delay for images
                    //kotlinx.coroutines.delay(300)
                    listState.scrollToItem(targetIndex)

                    isFirstLoad = false
                    shouldAutoScroll = false
                    android.util.Log.d("ChatScreen", "✅ Initial load complete")
                } else {
                    // New message - smooth scroll
                    listState.animateScrollToItem(targetIndex)
                    shouldAutoScroll = false
                }
            } catch (e: java.util.concurrent.CancellationException) {
                shouldAutoScroll = false
                userHasScrolled = true
            } catch (e: Exception) {
                android.util.Log.e("ChatScreen", "❌ Auto-scroll error", e)
                shouldAutoScroll = false
            }
        }
    }

    // Handle pending permission results
    LaunchedEffect(pendingMediaAction, cameraPermissionState.allPermissionsGranted,
        mediaPermissionsState.allPermissionsGranted, contactsPermissionState.allPermissionsGranted) {

        when (pendingMediaAction) {
            "camera" -> {
                if (cameraPermissionState.allPermissionsGranted) {
                    cameraImageUri = MediaPickerHelper.createTempImageUri(context)
                    cameraImageUri?.let { cameraLauncher.launch(it) }
                    pendingMediaAction = null
                }
            }
            "gallery" -> {
                if (mediaPermissionsState.allPermissionsGranted) {
                    imagePickerLauncher.launch(Unit)
                    pendingMediaAction = null
                }
            }
            "video" -> {
                if (mediaPermissionsState.allPermissionsGranted) {
                    videoPickerLauncher.launch(Unit)
                    pendingMediaAction = null
                }
            }
            "contact" -> {
                if (contactsPermissionState.allPermissionsGranted) {
                    contactPickerLauncher.launch(Unit)
                    pendingMediaAction = null
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ChatTopBar(
                contact = contact,
                onBackClick = onBackClick,
                onProfileClick = onProfileClick
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.ime)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                ChatInputField(
                    messageText = messageText,
                    onMessageTextChange = { messageText = it },
                    onSendClick = {
                        if (messageText.isNotBlank() && conversationId != null) {
                            coroutineScope.launch {
                                try {
                                    messagesRepository.sendMessage(
                                        conversationId = conversationId!!,
                                        recipientId = contact.id,
                                        content = messageText,
                                        replyToMessageId = replyToMessage?.messageId
                                    )

                                    // TEST: Echo incoming message
                                    //kotlinx.coroutines.delay(500)
                                    try {
                                        messagesRepository.receiveTestMessage(
                                            conversationId = conversationId!!,
                                            senderId = contact.id,
                                            content = messageText
                                        )
                                    } catch (e: Exception) {
                                        android.util.Log.e("ChatScreen", "Error creating test incoming message", e)
                                    }

                                    messageText = ""
                                    replyToMessage = null

                                    reloadMessages(messagesRepository, conversationId!!, pageSize, userHasScrolled) {
                                        messages = it.first
                                        currentOffset = it.second
                                        hasMoreMessages = it.third
                                        isFirstLoad = false
                                        shouldAutoScroll = it.fourth
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("ChatScreen", "Error sending message", e)
                                }
                            }
                        }
                    },
                    onAttachClick = { showMediaPicker = true },
                    replyToMessage = replyToMessage,
                    onCancelReply = { replyToMessage = null }
                )
            }
        }
    ) { paddingValues ->
        ChatMessagesList(
            messages = messages,
            listState = listState,
            isLoadingMore = isLoadingMessages,
            onReply = { messageWithMedia ->
                replyToMessage = messageWithMedia.toReplyMessage(contact.name)
            },
            onDownloadClick = { mediaId ->
                coroutineScope.launch {
                    messagesRepository.downloadMedia(mediaId)
                }
            },
            onImageClick = { imagePath ->
                selectedImagePath = imagePath
                showImagePreview = true
            },
            modifier = Modifier.padding(paddingValues)
        )
    }

    // Media picker bottom sheet
    if (showMediaPicker) {
        MediaPickerBottomSheet(
            onDismiss = { showMediaPicker = false },
            onCameraClick = {
                showMediaPicker = false
                if (cameraPermissionState.allPermissionsGranted) {
                    cameraImageUri = MediaPickerHelper.createTempImageUri(context)
                    cameraImageUri?.let { cameraLauncher.launch(it) }
                } else {
                    pendingMediaAction = "camera"
                    cameraPermissionState.requestPermissions()
                }
            },
            onGalleryClick = {
                showMediaPicker = false
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    imagePickerLauncher.launch(Unit)
                } else {
                    if (mediaPermissionsState.allPermissionsGranted) {
                        imagePickerLauncher.launch(Unit)
                    } else {
                        pendingMediaAction = "gallery"
                        mediaPermissionsState.requestPermissions()
                    }
                }
            },
            onVideoClick = {
                showMediaPicker = false
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    videoPickerLauncher.launch(Unit)
                } else {
                    if (mediaPermissionsState.allPermissionsGranted) {
                        videoPickerLauncher.launch(Unit)
                    } else {
                        pendingMediaAction = "video"
                        mediaPermissionsState.requestPermissions()
                    }
                }
            },
            onFileClick = {
                showMediaPicker = false
                filePickerLauncher.launch("*/*")
            },
            onContactClick = {
                showMediaPicker = false
                coroutineScope.launch {
                    //kotlinx.coroutines.delay(100)
                    if (contactsPermissionState.allPermissionsGranted) {
                        contactPickerLauncher.launch(Unit)
                    } else {
                        pendingMediaAction = "contact"
                        contactsPermissionState.requestPermissions()
                    }
                }
            }
        )
    }

    // Image preview dialog
    if (showImagePreview && selectedImagePath != null) {
        com.tcc.tarasulandroid.feature.image.ImagePreviewDialog(
            imagePath = selectedImagePath!!,
            onDismiss = {
                showImagePreview = false
                selectedImagePath = null
            }
        )
    }
}

/**
 * Helper function to reload messages after sending.
 * Returns: (messages, offset, hasMore, shouldAutoScroll)
 */
private suspend fun reloadMessages(
    messagesRepository: com.tcc.tarasulandroid.data.MessagesRepository,
    conversationId: String,
    pageSize: Int,
    userHasScrolled: Boolean,
    onResult: (Quadruple<List<MessageWithMediaAndReply>, Int, Boolean, Boolean>) -> Unit
) {
    try {
        val updatedMessages = messagesRepository.getMessagesWithMediaAndReplyPaginated(
            conversationId = conversationId,
            limit = pageSize,
            offset = 0
        )

        val totalCount = messagesRepository.getMessageCount(conversationId)
        val hasMore = updatedMessages.size < totalCount
        val shouldAutoScroll = !userHasScrolled

        onResult(Quadruple(updatedMessages, updatedMessages.size, hasMore, shouldAutoScroll))
    } catch (e: Exception) {
        android.util.Log.e("ChatScreen", "❌ Reload error", e)
    }
}

/**
 * Helper class for 4-tuple return value
 */
private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

/**
 * Extension to convert message to reply format.
 */
private fun MessageWithMediaAndReply.toReplyMessage(contactName: String): ReplyMessage {
    val senderName = if (message.direction == com.tcc.tarasulandroid.data.db.MessageDirection.OUTGOING) {
        "You"
    } else {
        contactName
    }

    return ReplyMessage(
        messageId = message.id,
        senderName = senderName,
        content = message.content,
        messageType = message.type
    )
}