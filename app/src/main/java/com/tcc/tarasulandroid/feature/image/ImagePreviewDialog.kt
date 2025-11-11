package com.tcc.tarasulandroid.feature.image

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.launch
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable
import java.io.File
import kotlin.math.abs

/**
 * Professional WhatsApp-style full-screen image preview with smooth animations.
 * 
 * This is rendered as a Dialog overlay on top of the chat screen, which:
 * - Keeps the chat visible underneath (no white screen)
 * - Provides buttery smooth 60fps animations
 * - Supports pinch-to-zoom, double-tap zoom, and swipe-down-to-dismiss
 * - Uses physics-based spring animations for natural feel
 * 
 * @param imagePath Local file path to the image
 * @param onDismiss Callback when the dialog should be dismissed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePreviewDialog(
    imagePath: String,
    onDismiss: () -> Unit
) {
    // Professional animation state with Animatable for GPU-accelerated smoothness
    val offsetY = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    val backgroundAlpha = remember { Animatable(1f) }
    val imageAlpha = remember { Animatable(1f) }
    val topBarAlpha = remember { Animatable(1f) }
    
    var isDragging by remember { mutableStateOf(false) }
    var isDismissing by remember { mutableStateOf(false) }
    val zoomState = rememberZoomableState()
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    
    // Professional threshold (150dp = ~1.5 inches on most phones)
    val dismissThreshold = with(density) { 150.dp.toPx() }
    
    // Animate entrance
    LaunchedEffect(Unit) {
        launch {
            backgroundAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 200)
            )
        }
        launch {
            imageAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 200)
            )
        }
        launch {
            topBarAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 200)
            )
        }
    }
    
    // Handle back button
    BackHandler(enabled = !isDismissing) {
        if (!isDismissing) {
            isDismissing = true
            coroutineScope.launch {
                // Smooth fade-out animation
                launch {
                    backgroundAlpha.animateTo(0f, tween(200))
                }
                launch {
                    imageAlpha.animateTo(0f, tween(200))
                }
                launch {
                    topBarAlpha.animateTo(0f, tween(200))
                }
                kotlinx.coroutines.delay(200)
                onDismiss()
            }
        }
    }
    
    // Full-screen dialog that sits on top of chat screen
    Dialog(
        onDismissRequest = {
            if (!isDismissing) {
                isDismissing = true
                coroutineScope.launch {
                    launch { backgroundAlpha.animateTo(0f, tween(200)) }
                    launch { imageAlpha.animateTo(0f, tween(200)) }
                    launch { topBarAlpha.animateTo(0f, tween(200)) }
                    kotlinx.coroutines.delay(200)
                    onDismiss()
                }
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false, // Full screen
            decorFitsSystemWindows = false   // Edge-to-edge
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = backgroundAlpha.value))
                .systemBarsPadding()
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = {
                            // Only allow swipe if not zoomed
                            if (zoomState.zoomFraction == 0f && !isDismissing) {
                                isDragging = true
                            }
                        },
                        onDragEnd = {
                            if (isDragging && !isDismissing) {
                                coroutineScope.launch {
                                    if (abs(offsetY.value) > dismissThreshold) {
                                        // Professional dismiss with velocity continuation
                                        isDismissing = true
                                        
                                        val targetY = if (offsetY.value > 0) 2500f else -2500f
                                        val animSpec = tween<Float>(durationMillis = 250)
                                        
                                        // All properties animate smoothly together
                                        launch { offsetY.animateTo(targetY, animSpec) }
                                        launch { scale.animateTo(0.6f, animSpec) }
                                        launch { backgroundAlpha.animateTo(0f, animSpec) }
                                        launch { imageAlpha.animateTo(0f, animSpec) }
                                        launch { topBarAlpha.animateTo(0f, animSpec) }
                                        
                                        kotlinx.coroutines.delay(250)
                                        onDismiss()
                                    } else {
                                        // Professional spring snap-back (WhatsApp quality)
                                        val springSpec = spring<Float>(
                                            dampingRatio = 0.72f,  // Slightly underdamped for subtle bounce
                                            stiffness = 380f        // Fast but smooth
                                        )
                                        
                                        launch { offsetY.animateTo(0f, springSpec) }
                                        launch { scale.animateTo(1f, springSpec) }
                                        launch { backgroundAlpha.animateTo(1f, springSpec) }
                                        launch { imageAlpha.animateTo(1f, springSpec) }
                                        launch { topBarAlpha.animateTo(1f, springSpec) }
                                    }
                                    isDragging = false
                                }
                            }
                        },
                        onDragCancel = {
                            coroutineScope.launch {
                                isDragging = false
                                val springSpec = spring<Float>(
                                    dampingRatio = 0.72f,
                                    stiffness = 380f
                                )
                                
                                launch { offsetY.animateTo(0f, springSpec) }
                                launch { scale.animateTo(1f, springSpec) }
                                launch { backgroundAlpha.animateTo(1f, springSpec) }
                                launch { imageAlpha.animateTo(1f, springSpec) }
                                launch { topBarAlpha.animateTo(1f, springSpec) }
                            }
                        },
                        onVerticalDrag = { _, dragAmount ->
                            if (isDragging && zoomState.zoomFraction == 0f && !isDismissing) {
                                coroutineScope.launch {
                                    // Instant response for finger tracking
                                    offsetY.snapTo(offsetY.value + dragAmount)
                                    
                                    // Professional smooth curves with better math
                                    val absOffset = abs(offsetY.value)
                                    val progress = (absOffset / (dismissThreshold * 2.5f)).coerceIn(0f, 1f)
                                    
                                    // Background fades progressively (reveals chat underneath)
                                    backgroundAlpha.snapTo((1f - progress * 1.3f).coerceIn(0f, 1f))
                                    
                                    // Image scales down smoothly (WhatsApp style)
                                    scale.snapTo((1f - progress * 0.4f).coerceIn(0.6f, 1f))
                                    
                                    // Image fades slightly when far
                                    imageAlpha.snapTo((1f - progress * 0.4f).coerceIn(0.6f, 1f))
                                    
                                    // Top bar fades faster for cleaner look
                                    topBarAlpha.snapTo((1f - progress * 2f).coerceIn(0f, 1f))
                                }
                            }
                        }
                    )
                }
        ) {
            // Semi-transparent top bar
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (!isDismissing) {
                                isDismissing = true
                                coroutineScope.launch {
                                    launch { backgroundAlpha.animateTo(0f, tween(200)) }
                                    launch { imageAlpha.animateTo(0f, tween(200)) }
                                    launch { topBarAlpha.animateTo(0f, tween(200)) }
                                    kotlinx.coroutines.delay(200)
                                    onDismiss()
                                }
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
                    containerColor = Color.Black.copy(alpha = 0.4f * topBarAlpha.value)
                ),
                modifier = Modifier.graphicsLayer {
                    alpha = topBarAlpha.value
                }
            )
            
            // Professional zoomable image with all transforms
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
                            // Optional: single tap to toggle UI
                        }
                    ),
                contentScale = ContentScale.Fit,
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                text = "Failed to load image",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            TextButton(
                                onClick = {
                                    if (!isDismissing) {
                                        isDismissing = true
                                        onDismiss()
                                    }
                                }
                            ) {
                                Text("Close", color = Color.White)
                            }
                        }
                    }
                }
            )
            
            // Hint text (only show when not zoomed and not dragging)
            if (zoomState.zoomFraction == 0f && !isDragging && topBarAlpha.value > 0.5f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 48.dp)
                        .graphicsLayer {
                            alpha = topBarAlpha.value
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "Swipe down to close",
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
