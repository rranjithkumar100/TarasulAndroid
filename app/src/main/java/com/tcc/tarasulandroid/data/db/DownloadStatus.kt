package com.tcc.tarasulandroid.data.db

enum class DownloadStatus {
    NOT_STARTED,  // Download not yet initiated
    PENDING,      // Download queued
    DOWNLOADING,  // Currently downloading
    DONE,         // Download completed
    FAILED        // Download failed
}
