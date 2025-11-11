package com.tcc.tarasulandroid.feature.image

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import kotlinx.coroutines.launch
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
    // Professional animation state management
    val offsetY = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    val backgroundAlpha = remember { Animatable(1f) }
    var isDragging by remember { mutableStateOf(false) }
    var isDismissing by remember { mutableStateOf(false) }
    val zoomState = rememberZoomableState()
    val coroutineScope = rememberCoroutineScope()
    
    // Dismiss threshold: swipe down by 200dp
    val dismissThreshold = 200f
    
    // Handle back button press
    BackHandler(enabled = !isDismissing) {
        if (!isDismissing) {
            isDismissing = true
            onDismiss()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = backgroundAlpha.value))
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = {
                        // Only allow swipe if not zoomed and not dismissing
                        if (zoomState.zoomFraction == 0f && !isDismissing) {
                            isDragging = true
                        }
                    },
                    onDragEnd = {
                        if (isDragging && !isDismissing) {
                            coroutineScope.launch {
                                if (abs(offsetY.value) > dismissThreshold) {
                                    // Smooth dismiss animation
                                    isDismissing = true
                                    
                                    // Animate all properties simultaneously for smooth exit
                                    launch {
                                        offsetY.animateTo(
                                            targetValue = if (offsetY.value > 0) 1000f else -1000f,
                                            animationSpec = tween(300)
                                        )
                                    }
                                    launch {
                                        scale.animateTo(
                                            targetValue = 0.5f,
                                            animationSpec = tween(300)
                                        )
                                    }
                                    launch {
                                        backgroundAlpha.animateTo(
                                            targetValue = 0f,
                                            animationSpec = tween(300)
                                        )
                                    }
                                    
                                    kotlinx.coroutines.delay(300)
                                    onDismiss()
                                } else {
                                    // Smooth spring snap-back animation (WhatsApp style)
                                    launch {
                                        offsetY.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    }
                                    launch {
                                        scale.animateTo(
                                            targetValue = 1f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    }
                                    launch {
                                        backgroundAlpha.animateTo(
                                            targetValue = 1f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    }
                                }
                                isDragging = false
                            }
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            isDragging = false
                            // Smooth spring snap-back
                            launch {
                                offsetY.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                            }
                            launch {
                                scale.animateTo(
                                    targetValue = 1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                            }
                            launch {
                                backgroundAlpha.animateTo(
                                    targetValue = 1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                            }
                        }
                    },
                    onVerticalDrag = { _, dragAmount ->
                        if (isDragging && zoomState.zoomFraction == 0f && !isDismissing) {
                            coroutineScope.launch {
                                // Update offset instantly for responsive feel
                                offsetY.snapTo(offsetY.value + dragAmount)
                                
                                // Update alpha and scale based on offset (smooth calculations)
                                val progress = (abs(offsetY.value) / dismissThreshold).coerceIn(0f, 1f)
                                backgroundAlpha.snapTo((1f - progress).coerceIn(0f, 1f))
                                scale.snapTo((1f - progress * 0.2f).coerceIn(0.8f, 1f))
                            }
                        }
                    }
                )
            }
    ) {
        // Top bar with semi-transparent background
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(
                    onClick = {
                        if (!isDismissing) {
                            isDismissing = true
                            onDismiss()
                        }
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black.copy(alpha = 0.3f * backgroundAlpha.value)
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
                    translationY = offsetY.value
                    scaleX = scale.value
                    scaleY = scale.value
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
    
}
