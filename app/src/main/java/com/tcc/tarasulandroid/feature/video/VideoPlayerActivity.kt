package com.tcc.tarasulandroid.feature.video

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.tcc.tarasulandroid.R
import java.io.File

class VideoPlayerActivity : ComponentActivity() {
    
    companion object {
        const val EXTRA_VIDEO_PATH = "extra_video_path"
        const val EXTRA_VIDEO_NAME = "extra_video_name"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Keep screen on while playing video
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // Hide system UI for immersive experience
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        )
        
        val videoPath = intent.getStringExtra(EXTRA_VIDEO_PATH)
        val videoName = intent.getStringExtra(EXTRA_VIDEO_NAME) ?: "Video"
        
        setContent {
            MaterialTheme {
                VideoPlayerScreen(
                    videoPath = videoPath,
                    videoName = videoName,
                    onBackClick = { finish() },
                    onError = { errorMessage ->
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VideoPlayerScreen(
    videoPath: String?,
    videoName: String,
    onBackClick: () -> Unit,
    onError: (String) -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var showControls by remember { mutableStateOf(true) }
    
    // Check if video file exists
    LaunchedEffect(videoPath) {
        if (videoPath == null) {
            onError("No video path provided")
            return@LaunchedEffect
        }
        
        val file = File(videoPath)
        if (!file.exists()) {
            onError("Video file not found. It may have been deleted.")
            return@LaunchedEffect
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Video player
        if (videoPath != null && File(videoPath).exists()) {
            AndroidView(
                factory = { context ->
                    VideoView(context).apply {
                        setVideoURI(Uri.fromFile(File(videoPath)))
                        
                        // Setup media controller
                        val mediaController = MediaController(context)
                        mediaController.setAnchorView(this)
                        setMediaController(mediaController)
                        
                        setOnPreparedListener { mediaPlayer ->
                            isLoading = false
                            mediaPlayer.start()
                        }
                        
                        setOnErrorListener { _, what, extra ->
                            onError("Error playing video: $what, $extra")
                            true
                        }
                        
                        setOnCompletionListener {
                            // Video finished
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Loading indicator
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        }
        
        // Top bar with back button and title
        if (showControls) {
            TopAppBar(
                title = {
                    Text(
                        text = videoName,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.5f)
                )
            )
        }
    }
}
