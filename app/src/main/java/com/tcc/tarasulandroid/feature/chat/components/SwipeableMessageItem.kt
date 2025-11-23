
// ============================================
// FILE 2: SwipeableMessageItem.kt (FIXED)
// ============================================
package com.tcc.tarasulandroid.feature.chat.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tcc.tarasulandroid.R
import com.tcc.tarasulandroid.data.MessageWithMediaAndReply
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

/**
 * Swipeable message item with FIXED gesture handling.
 * Uses awaitEachGesture for proper gesture detection that doesn't conflict with scroll.
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
    val swipeThreshold = 100f  // Increased from 80f
    val maxSwipe = 140f        // Increased from 120f
    val horizontalThreshold = 50f  // Must move this much horizontally
    val verticalThreshold = 30f    // Can't move more than this vertically

    Box(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var totalHorizontalDrag = 0f
                    var totalVerticalDrag = 0f
                    var isSwipeGesture = false
                    var hasStartedConsuming = false

                    // Wait for horizontal drag to start
                    val drag = horizontalDrag(down.id) { change ->
                        val positionChange = change.positionChange()
                        totalHorizontalDrag += positionChange.x
                        totalVerticalDrag += positionChange.y

                        // Determine if this is a swipe or scroll gesture
                        if (!hasStartedConsuming) {
                            val absHorizontal = abs(totalHorizontalDrag)
                            val absVertical = abs(totalVerticalDrag)

                            // Check if movement is primarily horizontal
                            if (absHorizontal > horizontalThreshold) {
                                // This is a horizontal swipe if vertical movement is minimal
                                if (absVertical < verticalThreshold || absHorizontal > absVertical * 2) {
                                    isSwipeGesture = true
                                    hasStartedConsuming = true
                                } else {
                                    // Too much vertical movement - this is a scroll
                                    isSwipeGesture = false
                                    return@horizontalDrag
                                }
                            }
                        }

                        // Only process if this is confirmed as a swipe gesture
                        if (isSwipeGesture && hasStartedConsuming) {
                            coroutineScope.launch {
                                val currentOffset = offsetX.value

                                // Apply resistance
                                val resistance = 1f - (abs(currentOffset) / maxSwipe).coerceIn(0f, 0.7f)
                                val adjustedDragAmount = positionChange.x * resistance
                                val finalOffset = currentOffset + adjustedDragAmount

                                // Constrain swipe direction based on message type
                                val constrainedOffset = if (isOutgoing) {
                                    finalOffset.coerceIn(0f, maxSwipe)
                                } else {
                                    finalOffset.coerceIn(-maxSwipe, 0f)
                                }

                                // Check if swiping in correct direction
                                val isCorrectDirection = if (isOutgoing) {
                                    constrainedOffset > 0f
                                } else {
                                    constrainedOffset < 0f
                                }

                                if (isCorrectDirection) {
                                    change.consume()

                                    offsetX.snapTo(constrainedOffset)

                                    // Animate icon
                                    val progress = (abs(constrainedOffset) / swipeThreshold).coerceIn(0f, 1f)
                                    iconScale.snapTo(progress)
                                    iconRotation.snapTo(progress * 360f)
                                }
                            }
                        }
                    }

                    // Handle drag end
                    if (drag != null && isSwipeGesture && hasStartedConsuming) {
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

                            // Spring snap-back
                            val springSpec = spring<Float>(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )

                            launch { offsetX.animateTo(0f, springSpec) }
                            launch { iconScale.animateTo(0f, tween(150)) }
                            launch { iconRotation.animateTo(0f, tween(150)) }
                        }
                    } else if (!isSwipeGesture || !hasStartedConsuming) {
                        // Not a swipe gesture or cancelled - reset immediately
                        coroutineScope.launch {
                            offsetX.snapTo(0f)
                            iconScale.snapTo(0f)
                            iconRotation.snapTo(0f)
                        }
                    }
                }
            }
    ) {
        // Reply icon
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

        // Message bubble
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