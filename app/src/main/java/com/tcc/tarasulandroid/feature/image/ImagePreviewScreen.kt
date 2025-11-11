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
    // Professional animation state management with WhatsApp-quality parameters
    val offsetY = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    val backgroundAlpha = remember { Animatable(1f) }
    val imageAlpha = remember { Animatable(1f) }
    var isDragging by remember { mutableStateOf(false) }
    var isDismissing by remember { mutableStateOf(false) }
    val zoomState = rememberZoomableState()
    val coroutineScope = rememberCoroutineScope()
    
    // Dismiss threshold: swipe down by 150dp (more sensitive, like WhatsApp)
    val dismissThreshold = 150f
    
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
                                    // Professional WhatsApp-style dismiss with velocity
                                    isDismissing = true
                                    
                                    // Faster, smoother dismiss - all properties animate together
                                    launch {
                                        offsetY.animateTo(
                                            targetValue = if (offsetY.value > 0) 2000f else -2000f,
                                            animationSpec = tween(durationMillis = 250, delayMillis = 0)
                                        )
                                    }
                                    launch {
                                        scale.animateTo(
                                            targetValue = 0.7f,
                                            animationSpec = tween(durationMillis = 250, delayMillis = 0)
                                        )
                                    }
                                    launch {
                                        backgroundAlpha.animateTo(
                                            targetValue = 0f,
                                            animationSpec = tween(durationMillis = 250, delayMillis = 0)
                                        )
                                    }
                                    launch {
                                        imageAlpha.animateTo(
                                            targetValue = 0f,
                                            animationSpec = tween(durationMillis = 250, delayMillis = 0)
                                        )
                                    }
                                    
                                    kotlinx.coroutines.delay(250)
                                    onDismiss()
                                } else {
                                    // Professional spring snap-back with perfect parameters
                                    val springSpec = spring<Float>(
                                        dampingRatio = 0.75f,  // Less bouncy, more controlled
                                        stiffness = 400f       // Fast but smooth
                                    )
                                    
                                    launch { offsetY.animateTo(0f, springSpec) }
                                    launch { scale.animateTo(1f, springSpec) }
                                    launch { backgroundAlpha.animateTo(1f, springSpec) }
                                    launch { imageAlpha.animateTo(1f, springSpec) }
                                }
                                isDragging = false
                            }
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            isDragging = false
                            // Professional snap-back
                            val springSpec = spring<Float>(
                                dampingRatio = 0.75f,
                                stiffness = 400f
                            )
                            
                            launch { offsetY.animateTo(0f, springSpec) }
                            launch { scale.animateTo(1f, springSpec) }
                            launch { backgroundAlpha.animateTo(1f, springSpec) }
                            launch { imageAlpha.animateTo(1f, springSpec) }
                        }
                    },
                    onVerticalDrag = { _, dragAmount ->
                        if (isDragging && zoomState.zoomFraction == 0f && !isDismissing) {
                            coroutineScope.launch {
                                // Update offset instantly for responsive feel
                                offsetY.snapTo(offsetY.value + dragAmount)
                                
                                // Professional smooth calculations with better curves
                                val progress = (abs(offsetY.value) / (dismissThreshold * 2f)).coerceIn(0f, 1f)
                                
                                // Background fades faster for better reveal
                                backgroundAlpha.snapTo((1f - progress * 1.5f).coerceIn(0f, 1f))
                                
                                // Image scales down smoothly
                                scale.snapTo((1f - progress * 0.3f).coerceIn(0.7f, 1f))
                                
                                // Image also fades slightly when far
                                imageAlpha.snapTo((1f - progress * 0.5f).coerceIn(0.5f, 1f))
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
        
        // Zoomable image with professional transforms
        SubcomposeAsyncImage(
            model = File(imagePath),
            contentDescription = "Full screen image",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = offsetY.value
                    scaleX = scale.value
                    scaleY = scale.value
                    alpha = imageAlpha.value
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
