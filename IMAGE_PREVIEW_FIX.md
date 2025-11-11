# Image Preview Callback Chain Fix

## Issue
Compilation error: `Unresolved reference 'onImageClick'` at line 978 in ChatScreen.kt

## Root Cause
The `onImageClick` callback wasn't being passed through the `SwipeableMessageItem` component, breaking the callback chain from NavGraph to MessageBubble.

## Solution

### 1. Added Parameter to SwipeableMessageItem
```kotlin
@Composable
private fun SwipeableMessageItem(
    messageWithMedia: MessageWithMediaAndReply,
    onReply: () -> Unit,
    onDownloadClick: (String) -> Unit,
    onImageClick: (String) -> Unit = {} // ← Added
)
```

### 2. Passed Callback from LazyColumn
```kotlin
items(items = messages, key = { it.message.id }) { messageWithMedia ->
    SwipeableMessageItem(
        messageWithMedia = messageWithMedia,
        onReply = { ... },
        onDownloadClick = { ... },
        onImageClick = onImageClick // ← Added
    )
}
```

## Complete Callback Chain

Now the callback chain is complete and working:

```
NavGraph
  ↓ onImageClick = { imagePath -> navigate("image_preview/$encodedPath") }
  
ChatScreen
  ↓ onImageClick: (String) -> Unit
  
SwipeableMessageItem (items in LazyColumn)
  ↓ onImageClick = onImageClick
  
MessageBubbleWithReply
  ↓ onImageClick = onImageClick
  
MessageBubble
  ↓ onImageClick = onImageClick
  
ImageMessageContent
  ↓ .clickable { onImageClick(media.localPath) }
  
✅ User taps image → Opens ImagePreviewScreen
```

## Verification

✅ No linter errors  
✅ Callback properly propagated through all layers  
✅ Type-safe (String parameter)  
✅ Default empty lambda for optional usage  

## Testing

To test the complete flow:
1. Open any chat with image messages
2. Tap any image
3. Should open full-screen image preview
4. Pinch to zoom should work
5. Swipe down should dismiss
6. Back button should return to chat

---
**Status:** ✅ Fixed  
**Date:** 2025-11-11
