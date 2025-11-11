package com.tcc.tarasulandroid.feature.image

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable
import java.io.File
import kotlin.math.abs

/**
 * Full-screen image preview with WhatsApp-style features:
 * - Pinch to zoom
 * - Double-tap to zoom
 * - Swipe down to dismiss
 * - Smooth transitions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePreviewScreen(
    imagePath: String,
    onDismiss: () -> Unit
) {
    var offsetY by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val zoomState = rememberZoomableState()
    
    // Dismiss threshold: swipe down by 200dp
    val dismissThreshold = 200f
    
    // Background opacity based on vertical offset
    val backgroundAlpha = remember(offsetY) {
        (1f - (abs(offsetY) / dismissThreshold)).coerceIn(0f, 1f)
    }
    
    // Scale effect based on vertical offset
    val scale = remember(offsetY) {
        (1f - (abs(offsetY) / (dismissThreshold * 5))).coerceIn(0.8f, 1f)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = backgroundAlpha))
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = {
                        // Only allow swipe if not zoomed
                        if (zoomState.zoomFraction == 0f) {
                            isDragging = true
                        }
                    },
                    onDragEnd = {
                        if (isDragging) {
                            if (abs(offsetY) > dismissThreshold) {
                                // Dismiss if swiped far enough
                                onDismiss()
                            } else {
                                // Snap back to original position
                                offsetY = 0f
                            }
                            isDragging = false
                        }
                    },
                    onDragCancel = {
                        offsetY = 0f
                        isDragging = false
                    },
                    onVerticalDrag = { _, dragAmount ->
                        if (isDragging && zoomState.zoomFraction == 0f) {
                            offsetY += dragAmount
                        }
                    }
                )
            }
    ) {
        // Top bar with semi-transparent background
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black.copy(alpha = 0.3f * backgroundAlpha)
            ),
            modifier = Modifier.statusBarsPadding()
        )
        
        // Zoomable image
        SubcomposeAsyncImage(
            model = File(imagePath),
            contentDescription = "Full screen image",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = offsetY
                    scaleX = scale
                    scaleY = scale
                }
                .zoomable(
                    state = zoomState,
                    onClick = {
                        // Optional: Single tap to toggle UI visibility
                    }
                ),
            contentScale = ContentScale.Fit,
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            },
            error = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Failed to load image",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onDismiss) {
                            Text("Close", color = Color.White)
                        }
                    }
                }
            }
        )
        
        // Hint text for swipe down (only show when not zoomed)
        if (zoomState.zoomFraction == 0f && !isDragging) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Swipe down to close",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
    
    // Auto-dismiss animation when user releases far down
    LaunchedEffect(offsetY) {
        if (abs(offsetY) > dismissThreshold && !isDragging) {
            // Animate to fully transparent and dismiss
            kotlinx.coroutines.delay(100)
            onDismiss()
        }
    }
}
