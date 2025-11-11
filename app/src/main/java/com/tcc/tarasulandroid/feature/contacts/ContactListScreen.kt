package com.tcc.tarasulandroid.feature.contacts

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tcc.tarasulandroid.R
import com.tcc.tarasulandroid.di.MessagesRepositoryEntryPoint
import com.tcc.tarasulandroid.feature.contacts.model.DeviceContact
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactListScreen(
    navController: NavController,
    viewModel: ContactsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    // Get MessagesRepository from application context via Hilt EntryPoint
    val messagesRepository = remember {
        val app = context.applicationContext as com.tcc.tarasulandroid.TarasulApplication
        dagger.hilt.android.EntryPointAccessors.fromApplication(
            app,
            MessagesRepositoryEntryPoint::class.java
        ).messagesRepository()
    }
    
    // State for creating conversation
    var isCreatingConversation by remember { mutableStateOf(false) }
    
    // Check if we already have contacts cached
    val hasContacts = uiState.contacts.isNotEmpty()
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onPermissionGranted()
            // Auto-sync contacts after permission is granted (only if we don't have cached contacts)
            if (!hasContacts) {
                viewModel.syncContacts(forceFullSync = false)
            }
        } else {
            viewModel.onPermissionDenied()
        }
    }
    
    // Request permission on first launch
    LaunchedEffect(Unit) {
        permissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
    }
    
    // Show sync message
    LaunchedEffect(uiState.lastSyncMessage) {
        uiState.lastSyncMessage?.let {
            viewModel.clearSyncMessage()
        }
    }
    
    ContactListScreenContent(
        uiState = uiState,
        searchQuery = searchQuery,
        onBackClick = { navController.popBackStack() },
        onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
        onSyncClick = { viewModel.syncContacts(forceFullSync = false) },
        onContactClick = { contact ->
            // Create conversation and navigate to chat
            isCreatingConversation = true
            kotlinx.coroutines.GlobalScope.launch {
                try {
                    val conversation = messagesRepository.getOrCreateConversation(
                        contactId = contact.id,
                        contactName = contact.name,
                        contactPhoneNumber = contact.phoneNumber
                    )
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        isCreatingConversation = false
                        // Navigate to chat screen and pop contacts from back stack
                        navController.navigate("chat/${conversation.contactId}/${conversation.contactName}/false") {
                            popUpTo("contacts") { inclusive = true }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        isCreatingConversation = false
                    }
                }
            }
        },
        onOpenSettingsClick = {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        },
        onRetryPermissionClick = {
            permissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
        }
    )
    
    // Show error snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactListScreenContent(
    uiState: ContactsUiState,
    searchQuery: String,
    onBackClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSyncClick: () -> Unit,
    onContactClick: (DeviceContact) -> Unit,
    onOpenSettingsClick: () -> Unit,
    onRetryPermissionClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.contacts)) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    },
                    actions = {
                        if (uiState.hasPermission) {
                            IconButton(
                                onClick = onSyncClick,
                                enabled = !uiState.isSyncing
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = stringResource(R.string.sync_contacts)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                
                // Search bar
                if (uiState.hasPermission && uiState.contacts.isNotEmpty()) {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                // Permission denied state
                uiState.permissionDenied -> {
                    PermissionDeniedContent(
                        onOpenSettingsClick = onOpenSettingsClick,
                        onRetryClick = onRetryPermissionClick
                    )
                }
                
                // Syncing state (first time)
                uiState.isSyncing && uiState.contacts.isEmpty() -> {
                    LoadingContent()
                }
                
                // Empty state
                uiState.hasPermission && uiState.contacts.isEmpty() && !uiState.isSyncing -> {
                    EmptyContactsContent(onSyncClick = onSyncClick)
                }
                
                // Contacts list
                uiState.hasPermission -> {
                    ContactsList(
                        contacts = uiState.displayContacts,
                        isSearching = uiState.isSearching,
                        isSyncing = uiState.isSyncing,
                        searchQuery = searchQuery,
                        onContactClick = onContactClick
                    )
                }
            }
            
            // Show sync message as snackbar
            LaunchedEffect(uiState.lastSyncMessage) {
                uiState.lastSyncMessage?.let { message ->
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
            
            // Show error as snackbar
            LaunchedEffect(uiState.error) {
                uiState.error?.let { error ->
                    snackbarHostState.showSnackbar(
                        message = error,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text(stringResource(R.string.search_contacts)) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        singleLine = true,
        shape = MaterialTheme.shapes.large,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun ContactsList(
    contacts: List<DeviceContact>,
    isSearching: Boolean,
    isSyncing: Boolean,
    searchQuery: String,
    onContactClick: (DeviceContact) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            contacts.isEmpty() && searchQuery.isNotBlank() && !isSearching -> {
                // No search results
                EmptySearchResults(searchQuery = searchQuery)
            }
            else -> {
                // Optimized LazyColumn for large lists
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    // Show sync indicator at top if syncing
                    if (isSyncing) {
                        item(key = "sync_indicator") {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                    
                    items(
                        items = contacts,
                        key = { it.id }
                    ) { contact ->
                        ContactListItem(
                            contact = contact,
                            onClick = { onContactClick(contact) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactListItem(
    contact: DeviceContact,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar with first letter
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contact.name.firstOrNull()?.uppercase() ?: "?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Contact info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = contact.phoneNumber,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PermissionDeniedContent(
    onOpenSettingsClick: () -> Unit,
    onRetryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.permission_contacts_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = stringResource(R.string.permission_contacts_message),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onOpenSettingsClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.open_settings))
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onRetryClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.try_again))
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(60.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.loading_contacts),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyContactsContent(onSyncClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.no_contacts_found),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = stringResource(R.string.no_contacts_message),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onSyncClick) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.sync_now))
        }
    }
}

@Composable
private fun EmptySearchResults(searchQuery: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.no_results_found),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = stringResource(R.string.no_results_message, searchQuery),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ==================== Previews ====================

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ContactListScreenPreview() {
    MaterialTheme {
        ContactListScreenContent(
            uiState = ContactsUiState(
                contacts = listOf(
                    DeviceContact("1", "Ahmed Salem", "+966501234567", null, "966501234567"),
                    DeviceContact("2", "Fatima Ali", "+966507654321", null, "966507654321"),
                    DeviceContact("3", "Mohammed Khan", "+966509876543", null, "966509876543"),
                    DeviceContact("4", "Sara Abdullah", "+966502345678", null, "966502345678"),
                    DeviceContact("5", "Omar Hassan", "+966508765432", null, "966508765432")
                ),
                hasPermission = true
            ),
            searchQuery = "",
            onBackClick = {},
            onSearchQueryChange = {},
            onSyncClick = {},
            onContactClick = {},
            onOpenSettingsClick = {},
            onRetryPermissionClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ContactListScreenPermissionDeniedPreview() {
    MaterialTheme {
        ContactListScreenContent(
            uiState = ContactsUiState(permissionDenied = true),
            searchQuery = "",
            onBackClick = {},
            onSearchQueryChange = {},
            onSyncClick = {},
            onContactClick = {},
            onOpenSettingsClick = {},
            onRetryPermissionClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ContactListScreenLoadingPreview() {
    MaterialTheme {
        ContactListScreenContent(
            uiState = ContactsUiState(hasPermission = true, isSyncing = true),
            searchQuery = "",
            onBackClick = {},
            onSearchQueryChange = {},
            onSyncClick = {},
            onContactClick = {},
            onOpenSettingsClick = {},
            onRetryPermissionClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ContactListScreenEmptyPreview() {
    MaterialTheme {
        ContactListScreenContent(
            uiState = ContactsUiState(hasPermission = true),
            searchQuery = "",
            onBackClick = {},
            onSearchQueryChange = {},
            onSyncClick = {},
            onContactClick = {},
            onOpenSettingsClick = {},
            onRetryPermissionClick = {}
        )
    }
}
