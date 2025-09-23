package com.weatherpossum.app.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weatherpossum.app.util.InAppUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for managing automatic app update functionality
 * Handles background checking for updates, downloading, and installing new versions
 * Runs silently in the background and only alerts user when update is available
 */
class UpdateViewModel(
    private val owner: String = "IzanahTech",
    private val repo: String = "WeatherPossum"
) : ViewModel() {

    var candidate by mutableStateOf<InAppUpdater.UpdateCandidate?>(null)
        private set
        
    var downloading by mutableStateOf(false)
        private set
        
    var progressText by mutableStateOf<String?>(null)
        private set
        
    var error by mutableStateOf<String?>(null)
        private set
        
    var hasChecked by mutableStateOf(false)
        private set

    /**
     * Automatically check for available updates in the background
     * Only shows update dialog if a newer version is found
     */
    fun checkForUpdates(context: Context) = viewModelScope.launch {
        if (hasChecked) return@launch // Prevent multiple simultaneous checks
        
        hasChecked = true
        runCatching {
            val cand = InAppUpdater.checkLatest(context, owner, repo)
            if (cand != null && InAppUpdater.isNewerThanInstalled(context, cand.tag)) {
                candidate = cand
            } else {
                candidate = null
            }
        }.onFailure { 
            // Silently handle errors - don't show to user unless critical
            error = it.message
        }
    }

    /**
     * Download and install the available update
     */
    fun downloadAndInstall(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        val cand = candidate ?: return@launch
        
        runCatching {
            downloading = true
            progressText = "Downloading APK…"
            
            val apk = InAppUpdater.downloadToCache(context, cand.apkUrl, "update.apk")

            // Verify SHA256 if available
            if (cand.shaUrl.isNotBlank()) {
                progressText = "Verifying checksum…"
                val shaFile = InAppUpdater.downloadToCache(context, cand.shaUrl, "update.apk.sha256")
                val expected = InAppUpdater.readSha256File(shaFile)
                val actual = InAppUpdater.computeSha256(apk)
                require(expected == actual) { "Checksum mismatch" }
            }

            progressText = "Verifying signature…"
            require(InAppUpdater.isSignedBySameCert(context, apk)) { "Signature mismatch" }

            withContext(Dispatchers.Main) {
                InAppUpdater.installApkAndRelaunch(context, apk)
            }
        }.onFailure { e ->
            error = e.message
        }.also {
            downloading = false
            progressText = null
        }
    }

    /**
     * Clear any error state (for retry scenarios)
     */
    fun clearError() {
        error = null
    }

    /**
     * Dismiss the update candidate (user chose "Later")
     */
    fun dismissUpdate() {
        candidate = null
    }
    
    /**
     * Reset the check state to allow re-checking (useful for retry scenarios)
     */
    fun resetCheckState() {
        hasChecked = false
        error = null
    }
}
