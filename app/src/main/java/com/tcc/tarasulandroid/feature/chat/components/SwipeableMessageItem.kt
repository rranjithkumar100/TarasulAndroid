package com.tcc.tarasulandroid.feature.chat.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tcc.tarasulandroid.R
import com.tcc.tarasulandroid.data.MessageWithMediaAndReply
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Swipeable message item that triggers reply on swipe - WhatsApp style.
 * 
 * Features:
 * - Horizontal swipe gesture detection
 * - Animated reply icon that appears during swipe
 * - Spring-based snap-back animation
 * - Direction-aware swipe (left for outgoing, right for incoming)
 *
 * @param messageWithMedia The message data to display
 * @param onReply Callback when swipe threshold is reached
 * @param onDownloadClick Callback for media download
 * @param onImageClick Callback when image is clicked
 */
@Composable
fun SwipeableMessageItem(
    messageWithMedia: MessageWithMediaAndReply,
    onReply: () -> Unit,
    onDownloadClick: (String) -> Unit,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val message = messageWithMedia.message
    val isOutgoing = message.direction == com.tcc.tarasulandroid.data.db.MessageDirection.OUTGOING
    
    // Animation states
    val offsetX = remember { Animatable(0f) }
    val iconScale = remember { Animatable(0f) }
    val iconRotation = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    
    // Swipe parameters
    val swipeThreshold = 80f
    val maxSwipe = 120f
    
    Box(
        modifier = modifier
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
                            // Trigger reply if threshold reached
                            if (abs(offsetX.value) >= swipeThreshold) {
                                onReply()
                                launch {
                                    offsetX.animateTo(
                                        if (isOutgoing) -maxSwipe else maxSwipe,
                                        animationSpec = tween(50)
                                    )
                                }
                            }
                            
                            // Spring snap-back animation
                            val springSpec = spring<Float>(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                            
                            launch { offsetX.animateTo(0f, springSpec) }
                            launch { iconScale.animateTo(0f, tween(150)) }
                            launch { iconRotation.animateTo(0f, tween(150)) }
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            val springSpec = spring<Float>(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                            launch { offsetX.animateTo(0f, springSpec) }
                            launch { iconScale.animateTo(0f, tween(150)) }
                        }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        coroutineScope.launch {
                            val currentOffset = offsetX.value
                            
                            // Apply resistance for smoother feel
                            val resistance = 1f - (abs(currentOffset) / maxSwipe).coerceIn(0f, 0.7f)
                            val adjustedDragAmount = dragAmount * resistance
                            val finalOffset = currentOffset + adjustedDragAmount
                            
                            // Constrain direction based on message type
                            val constrainedOffset = if (isOutgoing) {
                                finalOffset.coerceIn(-maxSwipe, 0f) // Left swipe only
                            } else {
                                finalOffset.coerceIn(0f, maxSwipe)  // Right swipe only
                            }
                            
                            offsetX.snapTo(constrainedOffset)
                            
                            // Animate icon based on progress
                            val progress = (abs(constrainedOffset) / swipeThreshold).coerceIn(0f, 1f)
                            iconScale.snapTo(progress)
                            iconRotation.snapTo(progress * 360f)
                        }
                    }
                )
            }
    ) {
        // Reply icon (appears behind message during swipe)
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
        
        // Message bubble with offset and subtle rotation
        Box(
            modifier = Modifier.graphicsLayer {
                translationX = offsetX.value
                rotationZ = (offsetX.value / maxSwipe) * 2f
            }
        ) {
            MessageBubbleWithReply(
                messageWithMedia = messageWithMedia,
                replyToMessage = messageWithMedia.replyToMessage,
                onDownloadClick = onDownloadClick,
                onImageClick = onImageClick
            )
        }
    }
}
