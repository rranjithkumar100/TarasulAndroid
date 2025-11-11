package com.tcc.tarasulandroid.feature.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
import com.tcc.tarasulandroid.data.MessageWithMedia
import com.tcc.tarasulandroid.data.MessageWithMediaAndReply
import com.tcc.tarasulandroid.data.db.MessageType
import com.tcc.tarasulandroid.feature.chat.components.ReplyPreview
import com.tcc.tarasulandroid.feature.chat.models.ReplyMessage
import com.tcc.tarasulandroid.feature.home.model.Contact
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    contact: Contact,
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit = {}
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
    var messages by remember { mutableStateOf<List<MessageWithMediaAndReply>>(emptyList()) }
    var isLoadingMessages by remember { mutableStateOf(false) }
    var hasMoreMessages by remember { mutableStateOf(true) }
    var currentOffset by remember { mutableStateOf(0) }
    var shouldAutoScroll by remember { mutableStateOf(true) }
    var replyToMessage by remember { mutableStateOf<ReplyMessage?>(null) }
    val pageSize = 20

    LaunchedEffect(contact.id) {
        try {
            val conversation = messagesRepository.getOrCreateConversation(
                contactId = contact.id,
                contactName = contact.name,
                contactPhoneNumber = ""
            )
            conversationId = conversation.id

            // Load initial messages
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
                hasMoreMessages = initialMessages.size < totalCount
                isLoadingMessages = false

                android.util.Log.d("ChatScreen", "Initial load: ${initialMessages.size} messages, offset: $currentOffset, total: $totalCount, hasMore: $hasMoreMessages")
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatScreen", "Error loading messages", e)
            isLoadingMessages = false
        }
    }

    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty() && shouldAutoScroll) {
            listState.animateScrollToItem(messages.size - 1)
            shouldAutoScroll = false
        }
    }

    // Detect when scrolling near the top to load more messages
    LaunchedEffect(listState.isScrollInProgress) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { firstVisibleIndex ->
                // Only load if:
                // 1. Not currently loading
                // 2. Has more messages to load
                // 3. Scrolled near top (within first 3 items, excluding loading indicator)
                // 4. Conversation ID exists
                val actualFirstMessageIndex = if (isLoadingMessages) firstVisibleIndex - 1 else firstVisibleIndex

                if (!isLoadingMessages &&
                    hasMoreMessages &&
                    actualFirstMessageIndex <= 2 &&
                    actualFirstMessageIndex >= 0 &&
                    conversationId != null &&
                    messages.isNotEmpty()) {

                    android.util.Log.d("ChatScreen", "Loading more messages - currentOffset: $currentOffset")
                    isLoadingMessages = true

                    try {
                        val moreMessages = messagesRepository.getMessagesWithMediaAndReplyPaginated(
                            conversationId = conversationId!!,
                            limit = pageSize,
                            offset = currentOffset
                        )

                        android.util.Log.d("ChatScreen", "Loaded ${moreMessages.size} more messages")

                        if (moreMessages.isNotEmpty()) {
                            // Prepend old messages to the beginning
                            val currentScrollIndex = listState.firstVisibleItemIndex
                            val currentScrollOffset = listState.firstVisibleItemScrollOffset

                            messages = moreMessages + messages
                            currentOffset += moreMessages.size

                            // Maintain scroll position
                            coroutineScope.launch {
                                // Account for loading indicator if present
                                val adjustedIndex = currentScrollIndex + moreMessages.size
                                listState.scrollToItem(adjustedIndex, currentScrollOffset)
                                android.util.Log.d("ChatScreen", "Adjusted scroll to index: $adjustedIndex")
                            }
                        }

                        val totalCount = messagesRepository.getMessageCount(conversationId!!)
                        hasMoreMessages = currentOffset < totalCount

                        android.util.Log.d("ChatScreen", "New offset: $currentOffset, total: $totalCount, hasMore: $hasMoreMessages")
                    } catch (e: Exception) {
                        android.util.Log.e("ChatScreen", "Error loading more messages", e)
                    } finally {
                        isLoadingMessages = false
                    }
                }
            }
    }

    // Track what was clicked (to relaunch after permission granted)
    var pendingMediaAction by remember { mutableStateOf<String?>(null) }

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
        android.util.Log.d("ChatScreen", "Image picker callback - URI: $uri")
        if (uri == null) {
            android.util.Log.w("ChatScreen", "Image picker returned null URI (cancelled or error)")
            return@rememberLauncherForActivityResult
        }

        coroutineScope.launch {
            try {
                android.util.Log.d("ChatScreen", "Sending image: $uri")
                android.util.Log.d("ChatScreen", "ConversationId: $conversationId, ContactId: ${contact.id}")

                messagesRepository.sendMediaMessage(
                    conversationId = conversationId ?: return@launch,
                    recipientId = contact.id,
                    mediaType = MessageType.IMAGE,
                    mediaUri = uri,
                    mimeType = context.contentResolver.getType(uri),
                    fileName = MediaPickerHelper.getFileName(context, uri)
                )
                android.util.Log.d("ChatScreen", "Image sent successfully")
            } catch (e: Exception) {
                android.util.Log.e("ChatScreen", "Error sending image", e)
            }
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = PickVideoContract()
    ) { uri ->
        android.util.Log.d("ChatScreen", "Video picker callback - URI: $uri")
        if (uri == null) {
            android.util.Log.w("ChatScreen", "Video picker returned null URI (cancelled or error)")
            return@rememberLauncherForActivityResult
        }

        coroutineScope.launch {
            try {
                android.util.Log.d("ChatScreen", "Sending video: $uri")
                android.util.Log.d("ChatScreen", "ConversationId: $conversationId, ContactId: ${contact.id}")

                messagesRepository.sendMediaMessage(
                    conversationId = conversationId ?: return@launch,
                    recipientId = contact.id,
                    mediaType = MessageType.VIDEO,
                    mediaUri = uri,
                    mimeType = context.contentResolver.getType(uri),
                    fileName = MediaPickerHelper.getFileName(context, uri)
                )
                android.util.Log.d("ChatScreen", "Video sent successfully")
            } catch (e: Exception) {
                android.util.Log.e("ChatScreen", "Error sending video", e)
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

                    // Send as contact message with proper type
                    messagesRepository.sendContactMessage(
                        conversationId = conversationId ?: return@launch,
                        contactInfo = contactInfo,
                        recipientId = contact.id
                    )
                } catch (e: Exception) {
                    android.util.Log.e("ChatScreen", "Error sending contact", e)
                }
            }
        }
    }

    // Watch for permission grants and auto-launch pickers
    LaunchedEffect(
        cameraPermissionState.allPermissionsGranted,
        mediaPermissionsState.allPermissionsGranted,
        contactsPermissionState.allPermissionsGranted,
        pendingMediaAction
    ) {
        android.util.Log.d("ChatScreen", "Permission state changed:")
        android.util.Log.d("ChatScreen", "  - Pending: $pendingMediaAction")
        android.util.Log.d("ChatScreen", "  - Camera granted: ${cameraPermissionState.allPermissionsGranted}")
        android.util.Log.d("ChatScreen", "  - Media granted: ${mediaPermissionsState.allPermissionsGranted}")
        android.util.Log.d("ChatScreen", "  - Contacts granted: ${contactsPermissionState.allPermissionsGranted}")

        when (pendingMediaAction) {
            "camera" -> {
                if (cameraPermissionState.allPermissionsGranted) {
                    android.util.Log.d("ChatScreen", "Permission granted, launching camera")
                    cameraImageUri = MediaPickerHelper.createTempImageUri(context)
                    cameraImageUri?.let { cameraLauncher.launch(it) }
                    pendingMediaAction = null
                }
            }
            "gallery" -> {
                if (mediaPermissionsState.allPermissionsGranted) {
                    android.util.Log.d("ChatScreen", "Permission granted, launching image picker")
                    imagePickerLauncher.launch(Unit)
                    pendingMediaAction = null
                }
            }
            "video" -> {
                if (mediaPermissionsState.allPermissionsGranted) {
                    android.util.Log.d("ChatScreen", "Permission granted, launching video picker")
                    videoPickerLauncher.launch(Unit)
                    pendingMediaAction = null
                }
            }
            "contact" -> {
                if (contactsPermissionState.allPermissionsGranted) {
                    android.util.Log.d("ChatScreen", "Permission granted, launching contact picker")
                    contactPickerLauncher.launch(Unit)
                    pendingMediaAction = null
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        // Use default insets for top (safe choice). We'll handle bottom/IME in bottomBar only.
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onProfileClick)
                    ) {
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
                Column {
                    // Reply preview
                    replyToMessage?.let { reply ->
                        ReplyPreview(
                            replyMessage = reply,
                            onCancelReply = { replyToMessage = null }
                        )
                    }

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
                                contentDescription = stringResource(R.string.attach_media),
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
                                    val replyMessageId = replyToMessage?.messageId
                                    messageText = ""
                                    replyToMessage = null
                                    coroutineScope.launch {
                                        try {
                                            messagesRepository.sendMessage(
                                                conversationId = conversationId!!,
                                                content = textToSend,
                                                recipientId = contact.id,
                                                replyToMessageId = replyMessageId
                                            )
                                            // Reload the first page to show the new message
                                            val updatedMessages = messagesRepository.getMessagesWithMediaAndReplyPaginated(
                                                conversationId = conversationId!!,
                                                limit = pageSize,
                                                offset = 0
                                            )
                                            messages = updatedMessages
                                            currentOffset = updatedMessages.size

                                            val totalCount = messagesRepository.getMessageCount(conversationId!!)
                                            hasMoreMessages = currentOffset < totalCount

                                            // Scroll to bottom
                                            shouldAutoScroll = true
                                            listState.animateScrollToItem(messages.size - 1)
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
                // Show loading indicator at top when loading more messages
                if (isLoadingMessages && messages.isNotEmpty()) {
                    item(key = "loading_indicator") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }

                items(items = messages, key = { it.message.id }) { messageWithMedia ->
                    // Swipeable message with reply support
                    SwipeableMessageItem(
                        messageWithMedia = messageWithMedia,
                        onReply = {
                            replyToMessage = messageWithMedia.toReplyMessage(contact.name)
                        },
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
                android.util.Log.d("ChatScreen", "Camera clicked")
                if (cameraPermissionState.allPermissionsGranted) {
                    android.util.Log.d("ChatScreen", "Launching camera")
                    cameraImageUri = MediaPickerHelper.createTempImageUri(context)
                    cameraImageUri?.let { cameraLauncher.launch(it) }
                } else {
                    android.util.Log.d("ChatScreen", "Requesting camera permission")
                    pendingMediaAction = "camera"
                    cameraPermissionState.requestPermissions()
                }
            },
            onGalleryClick = {
                android.util.Log.d("ChatScreen", "Gallery clicked")
                android.util.Log.d("ChatScreen", "Media permissions granted: ${mediaPermissionsState.allPermissionsGranted}")

                if (mediaPermissionsState.allPermissionsGranted) {
                    android.util.Log.d("ChatScreen", "Launching image picker")
                    imagePickerLauncher.launch(Unit)
                } else {
                    android.util.Log.d("ChatScreen", "Requesting media permissions")
                    pendingMediaAction = "gallery"
                    mediaPermissionsState.requestPermissions()
                }
            },
            onVideoClick = {
                android.util.Log.d("ChatScreen", "Video clicked")
                android.util.Log.d("ChatScreen", "Media permissions granted: ${mediaPermissionsState.allPermissionsGranted}")

                if (mediaPermissionsState.allPermissionsGranted) {
                    android.util.Log.d("ChatScreen", "Launching video picker")
                    videoPickerLauncher.launch(Unit)
                } else {
                    android.util.Log.d("ChatScreen", "Requesting media permissions")
                    pendingMediaAction = "video"
                    mediaPermissionsState.requestPermissions()
                }
            },
            onFileClick = {
                filePickerLauncher.launch("*/*")
            },
            onContactClick = {
                android.util.Log.d("ChatScreen", "Contact clicked")
                // Dismiss bottom sheet first
                showMediaPicker = false
                // Launch contact picker after a short delay to avoid TextField issues
                coroutineScope.launch {
                    kotlinx.coroutines.delay(100)
                    if (contactsPermissionState.allPermissionsGranted) {
                        android.util.Log.d("ChatScreen", "Launching contact picker")
                        contactPickerLauncher.launch(Unit)
                    } else {
                        android.util.Log.d("ChatScreen", "Requesting contacts permission")
                        pendingMediaAction = "contact"
                        contactsPermissionState.requestPermissions()
                    }
                }
            }
        )
    }
}

/**
 * Swipeable message item that triggers reply on swipe - WhatsApp style
 */
@Composable
private fun SwipeableMessageItem(
    messageWithMedia: MessageWithMediaAndReply,
    onReply: () -> Unit,
    onDownloadClick: (String) -> Unit
) {
    val message = messageWithMedia.message
    val replyToMessage = messageWithMedia.replyToMessage
    val isOutgoing = message.direction == com.tcc.tarasulandroid.data.db.MessageDirection.OUTGOING
    
    // Animation states
    val offsetX = remember { Animatable(0f) }
    val iconScale = remember { Animatable(0f) }
    val iconRotation = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    
    // Swipe threshold to trigger reply
    val swipeThreshold = 80f
    val maxSwipe = 120f
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        coroutineScope.launch {
                            iconScale.snapTo(0f)
                            iconRotation.snapTo(0f)
                        }
                    },
                    onDragEnd = {
                        coroutineScope.launch {
                            // If swiped past threshold, trigger reply
                            if (abs(offsetX.value) >= swipeThreshold) {
                                onReply()
                                
                                // Quick snap animation for feedback
                                launch {
                                    offsetX.animateTo(
                                        if (isOutgoing) -maxSwipe else maxSwipe,
                                        animationSpec = tween(50)
                                    )
                                }
                            }
                            
                            // Smooth spring animation back to center
                            launch {
                                offsetX.animateTo(
                                    0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                            launch {
                                iconScale.animateTo(0f, animationSpec = tween(150))
                            }
                            launch {
                                iconRotation.animateTo(0f, animationSpec = tween(150))
                            }
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            launch {
                                offsetX.animateTo(
                                    0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                            launch {
                                iconScale.animateTo(0f, animationSpec = tween(150))
                            }
                        }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        coroutineScope.launch {
                            val currentOffset = offsetX.value
                            val newOffset = currentOffset + dragAmount
                            
                            // Apply resistance effect for smoother feel
                            val resistance = 1f - (abs(currentOffset) / maxSwipe).coerceIn(0f, 0.7f)
                            val adjustedDragAmount = dragAmount * resistance
                            val finalOffset = currentOffset + adjustedDragAmount
                            
                            // Constrain swipe direction based on message type
                            val constrainedOffset = if (isOutgoing) {
                                // Outgoing: allow left swipe only
                                finalOffset.coerceIn(-maxSwipe, 0f)
                            } else {
                                // Incoming: allow right swipe only
                                finalOffset.coerceIn(0f, maxSwipe)
                            }
                            
                            offsetX.snapTo(constrainedOffset)
                            
                            // Animate icon scale and rotation based on swipe progress
                            val progress = (abs(constrainedOffset) / swipeThreshold).coerceIn(0f, 1f)
                            iconScale.snapTo(progress)
                            iconRotation.snapTo(progress * 360f)
                        }
                    }
                )
            }
    ) {
        // Reply icon that appears during swipe - behind the message
        Box(
            modifier = Modifier
                .align(if (isOutgoing) Alignment.CenterEnd else Alignment.CenterStart)
                .padding(horizontal = 24.dp)
        ) {
            if (abs(offsetX.value) > 5f) {
                Icon(
                    painter = painterResource(R.drawable.ic_chat),
                    contentDescription = stringResource(R.string.reply),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(28.dp)
                        .graphicsLayer {
                            alpha = iconScale.value
                            scaleX = 0.7f + (iconScale.value * 0.3f)
                            scaleY = 0.7f + (iconScale.value * 0.3f)
                            rotationZ = if (isOutgoing) -iconRotation.value * 0.2f else iconRotation.value * 0.2f
                        }
                )
            }
        }
        
        // Message bubble with smooth offset and slight rotation
        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationX = offsetX.value
                    // Subtle rotation for more natural feel
                    rotationZ = (offsetX.value / maxSwipe) * 2f
                }
        ) {
            MessageBubbleWithReply(
                messageWithMedia = messageWithMedia,
                replyToMessage = replyToMessage,
                onDownloadClick = onDownloadClick
            )
        }
    }
}

/**
 * Extension function to convert MessageWithMedia to ReplyMessage
 */
private fun MessageWithMediaAndReply.toReplyMessage(contactName: String): ReplyMessage {
    val message = this.message
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

/**
 * Message bubble that shows the reply indicator when message is a reply
 */
@Composable
private fun MessageBubbleWithReply(
    messageWithMedia: MessageWithMediaAndReply,
    replyToMessage: com.tcc.tarasulandroid.data.db.MessageEntity?,
    onDownloadClick: (String) -> Unit
) {
    // If no reply, just show regular bubble
    if (replyToMessage == null) {
        MessageBubble(
            messageWithMedia = MessageWithMedia(messageWithMedia.message, messageWithMedia.media),
            onDownloadClick = onDownloadClick
        )
    } else {
        // Show reply indicator inside the message bubble
        val message = messageWithMedia.message
        val media = messageWithMedia.media
        val isOutgoing = message.direction == com.tcc.tarasulandroid.data.db.MessageDirection.OUTGOING
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isOutgoing) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Reply indicator at the top
                    com.tcc.tarasulandroid.feature.chat.components.ReplyIndicator(
                        senderName = if (replyToMessage.direction == com.tcc.tarasulandroid.data.db.MessageDirection.OUTGOING) {
                            "You"
                        } else {
                            replyToMessage.senderId.takeIf { it != "me" } ?: "Contact"
                        },
                        content = replyToMessage.content,
                        messageType = replyToMessage.type
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Now render the actual message content (using inline rendering)
                    when (message.type) {
                        MessageType.IMAGE -> {
                            if (media != null && media.localPath != null && java.io.File(media.localPath).exists()) {
                                coil.compose.AsyncImage(
                                    model = java.io.File(media.localPath),
                                    contentDescription = stringResource(R.string.image_message),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
                        }
                        MessageType.VIDEO -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_video),
                                    contentDescription = stringResource(R.string.video),
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        MessageType.FILE -> {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_file),
                                    contentDescription = stringResource(R.string.file),
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = media?.fileName ?: "File",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        MessageType.CONTACT -> {
                            val contactInfo = remember(message.content) {
                                try {
                                    com.tcc.tarasulandroid.data.ContactInfo.fromJsonString(message.content)
                                } catch (e: Exception) {
                                    null
                                }
                            }

                            if (contactInfo != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_contact),
                                        contentDescription = stringResource(R.string.contact),
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = contactInfo.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Text("Contact card")
                            }
                        }
                        MessageType.AUDIO -> {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_audio),
                                    contentDescription = stringResource(R.string.audio),
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Audio message",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        else -> {}
                    }
                    
                    // Text content
                    if (message.content.isNotBlank() && message.type != MessageType.CONTACT) {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isOutgoing) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    
                    // Timestamp
                    Text(
                        text = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(message.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOutgoing) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        },
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

