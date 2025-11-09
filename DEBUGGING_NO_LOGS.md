# Debugging: No Logs After Selection

## Problem
After selecting image/video, no logs appear at all.

## Added Enhanced Logging

### 1. Click Detection
```kotlin
onGalleryClick = {
    Log.d("ChatScreen", "Gallery clicked")  // ✅ Should see this when tapping
    Log.d("ChatScreen", "Media permissions granted: $granted")
    
    if (granted) {
        Log.d("ChatScreen", "Launching image picker")  // ✅ Should see this
        imagePickerLauncher.launch(Unit)
    } else {
        Log.d("ChatScreen", "Requesting media permissions")  // ⚠️ Or this
    }
}
```

### 2. Picker Callback
```kotlin
val imagePickerLauncher = rememberLauncherForActivityResult(...) { uri ->
    Log.d("ChatScreen", "Image picker callback - URI: $uri")  // ✅ First log in callback
    
    if (uri == null) {
        Log.w("ChatScreen", "Picker returned null")  // ⚠️ User cancelled or error
        return
    }
    
    // Continue with sending...
}
```

## What to Look For in Logcat

### Scenario 1: Permission Issue
```
ChatScreen: Gallery clicked
ChatScreen: Media permissions granted: false
ChatScreen: Requesting media permissions
```
**Solution:** Grant READ_MEDIA_IMAGES permission

### Scenario 2: Picker Launched
```
ChatScreen: Gallery clicked
ChatScreen: Media permissions granted: true
ChatScreen: Launching image picker
```
(Then nothing) - **Picker opened but callback not called**

### Scenario 3: Picker Cancelled
```
ChatScreen: Gallery clicked
ChatScreen: Media permissions granted: true
ChatScreen: Launching image picker
ChatScreen: Image picker callback - URI: null
ChatScreen: Picker returned null
```
**User cancelled** or picker error

### Scenario 4: Success
```
ChatScreen: Gallery clicked
ChatScreen: Media permissions granted: true
ChatScreen: Launching image picker
ChatScreen: Image picker callback - URI: content://...
ChatScreen: Sending image: content://...
ChatScreen: ConversationId: xxx, ContactId: yyy
MediaRepository: saveOutgoingMedia - uri: content://...
MediaRepository: Copying file to: /data/.../media/xxx.jpg
MediaRepository: File copied successfully, size: 12345 bytes
MessagesRepository: Inserting media message: xxx
ChatScreen: Image sent successfully
```
**Everything works!**

## How to Debug

### Step 1: Run logcat
```bash
adb logcat -s ChatScreen:* MediaRepository:* MessagesRepository:*
```

### Step 2: Tap attachment button
Should see:
```
(nothing yet - bottom sheet opening doesn't log)
```

### Step 3: Tap Gallery
Should see:
```
ChatScreen: Gallery clicked
ChatScreen: Media permissions granted: true/false
ChatScreen: Launching image picker (or Requesting permissions)
```

### Step 4: Select an image
Should see:
```
ChatScreen: Image picker callback - URI: content://...
```

## Common Issues

### A) No "Gallery clicked" log
**Problem:** Button not triggering onClick
**Check:** 
- Is bottom sheet actually visible?
- Try tapping different areas
- Check if button is being overlapped

### B) "Gallery clicked" but no "Launching picker"
**Problem:** Permissions not granted
**Solution:** 
```bash
# Grant permissions manually
adb shell pm grant com.tcc.tarasulandroid android.permission.READ_MEDIA_IMAGES
adb shell pm grant com.tcc.tarasulandroid android.permission.READ_MEDIA_VIDEO
```

### C) "Launching picker" but no callback
**Problem:** Picker opened but callback never fires
**Possible causes:**
1. Activity is being recreated
2. Compose recomposition issue
3. PickImageContract not working properly

**Try:** Use Android's built-in picker contract:
```kotlin
val imagePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
) { uri ->
    Log.d("ChatScreen", "Image selected: $uri")
    // ...
}

// Launch with:
imagePickerLauncher.launch("image/*")
```

### D) "Picker returned null"
**Problem:** User cancelled or picker error
**This is normal** - user backed out of picker

## Quick Test

Run this and tell me what you see:

```bash
adb logcat -s ChatScreen:*
```

Then:
1. Tap attachment button
2. Tap Gallery
3. Select an image
4. Copy-paste EXACTLY what logs appear

This will tell me the exact problem! 🔍
