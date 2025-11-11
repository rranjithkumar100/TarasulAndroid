package com.tcc.tarasulandroid.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tcc.tarasulandroid.R
import com.tcc.tarasulandroid.feature.home.model.Contact

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileInfoScreen(
    contact: Contact,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.contact_info)) },
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Header
            ProfileHeader(contact = contact)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // About Section
            ProfileSection(title = stringResource(R.string.about)) {
                ProfileInfoItem(
                    icon = Icons.Default.Person,
                    label = stringResource(R.string.name),
                    value = contact.name
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Phone Number Section (if available)
            ProfileSection(title = stringResource(R.string.phone_number)) {
                ProfileInfoItem(
                    icon = Icons.Default.Phone,
                    label = stringResource(R.string.mobile),
                    value = "+966 XX XXX XXXX" // Placeholder
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Media, Links, and Docs (placeholder for future implementation)
            ProfileSection(title = stringResource(R.string.media_links_docs)) {
                ProfileActionItemWithPainter(
                    icon = painterResource(R.drawable.ic_gallery),
                    label = stringResource(R.string.media),
                    value = "0",
                    onClick = { /* TODO: Navigate to media gallery */ }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Actions Section
            ProfileSection(title = stringResource(R.string.actions)) {
                ProfileActionItem(
                    icon = Icons.Default.Close,
                    label = stringResource(R.string.block_contact),
                    isDestructive = true,
                    onClick = { /* TODO: Block contact */ }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ProfileActionItem(
                    icon = Icons.Default.Warning,
                    label = stringResource(R.string.report_contact),
                    isDestructive = true,
                    onClick = { /* TODO: Report contact */ }
                )
            }
        }
    }
}

@Composable
private fun ProfileHeader(contact: Contact) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.name.first().toString(),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Name
            Text(
                text = contact.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Online Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (contact.isOnline) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (contact.isOnline) 
                        stringResource(R.string.online) 
                    else 
                        stringResource(R.string.offline),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontWeight = FontWeight.SemiBold
            )
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileInfoItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    ListItem(
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        headlineContent = { Text(value) },
        supportingContent = { Text(label) },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileActionItem(
    icon: ImageVector,
    label: String,
    value: String? = null,
    isDestructive: Boolean = false,
    onClick: () -> Unit = {}
) {
    ListItem(
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        headlineContent = {
            Text(
                label,
                color = if (isDestructive) 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.onSurface
            )
        },
        trailingContent = value?.let {
            {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileActionItemWithPainter(
    icon: Painter,
    label: String,
    value: String? = null,
    isDestructive: Boolean = false,
    onClick: () -> Unit = {}
) {
    ListItem(
        leadingContent = {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = if (isDestructive) 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        headlineContent = {
            Text(
                label,
                color = if (isDestructive) 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.onSurface
            )
        },
        trailingContent = value?.let {
            {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.clickable(onClick = onClick)
    )
}
