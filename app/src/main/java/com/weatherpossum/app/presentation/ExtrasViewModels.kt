package com.weatherpossum.app.presentation

import android.app.Application
import android.content.Context
import android.util.Log
import com.weatherpossum.app.R
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.TimeoutCancellationException
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weatherpossum.app.data.UserPreferences
import com.weatherpossum.app.data.model.HurricaneData
import com.weatherpossum.app.data.model.MoonData
import com.weatherpossum.app.data.repository.HurricaneRepository
import com.weatherpossum.app.data.repository.MoonFetchSchedule
import com.weatherpossum.app.data.repository.MoonRepository
import com.weatherpossum.app.util.InAppUpdater
import com.weatherpossum.app.widget.WeatherWidgetUpdateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

// ── Moon ─────────────────────────────────────────────────────────────────────

private const val MOON_TAG = "MoonViewModel"

sealed class MoonUiState {
    data object Loading : MoonUiState()
    data class Success(
        val moonData: MoonData,
        val isStale: Boolean = false
    ) : MoonUiState()
    data class Error(val message: String) : MoonUiState()
}

class MoonViewModel(
    private val application: Application,
    private val repository: MoonRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {
    private val _uiState = MutableStateFlow<MoonUiState>(MoonUiState.Loading)
    val uiState: StateFlow<MoonUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.warmDiskCache()
            combine(
                userPreferences.lastMoonFetchTime,
                repository.moonData
            ) { lastFetchTime, moonData ->
                lastFetchTime to moonData
            }.collect { (lastFetchTime, moonData) ->
                when {
                    moonData == null || MoonFetchSchedule.shouldFetchMoonData(lastFetchTime) -> {
                        if (moonData == null) {
                            _uiState.value = MoonUiState.Loading
                        }
                        try {
                            repository.refreshMoonData().fold(
                                onSuccess = { newMoonData ->
                                    userPreferences.updateLastMoonFetchTime(System.currentTimeMillis())
                                    _uiState.value = MoonUiState.Success(newMoonData)
                                },
                                onFailure = { exception ->
                                    Log.e(MOON_TAG, "Error fetching moon data", exception)
                                    _uiState.value = if (moonData != null) {
                                        MoonUiState.Success(moonData, isStale = true)
                                    } else {
                                        MoonUiState.Error(
                                            exception.message
                                                ?: application.getString(R.string.moon_error_fetch_failed)
                                        )
                                    }
                                }
                            )
                        } catch (e: Exception) {
                            Log.e(MOON_TAG, "Error fetching moon data", e)
                            _uiState.value = if (moonData != null) {
                                MoonUiState.Success(moonData, isStale = true)
                            } else {
                                MoonUiState.Error(
                                    e.message ?: application.getString(R.string.moon_error_fetch_failed)
                                )
                            }
                        }
                    }
                    else -> {
                        _uiState.value = MoonUiState.Success(moonData)
                    }
                }
            }
        }
    }

    suspend fun forceRefresh() {
        val cached = repository.moonData.value
        _uiState.value = MoonUiState.Loading
        try {
            repository.refreshMoonData().fold(
                onSuccess = { moonData ->
                    userPreferences.updateLastMoonFetchTime(System.currentTimeMillis())
                    _uiState.value = MoonUiState.Success(moonData)
                },
                onFailure = { exception ->
                    Log.e(MOON_TAG, "Error force-refreshing moon data", exception)
                    _uiState.value = if (cached != null) {
                        MoonUiState.Success(cached, isStale = true)
                    } else {
                        MoonUiState.Error(
                            exception.message
                                ?: application.getString(R.string.moon_error_fetch_failed)
                        )
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(MOON_TAG, "Error force-refreshing moon data", e)
            _uiState.value = if (cached != null) {
                MoonUiState.Success(cached, isStale = true)
            } else {
                MoonUiState.Error(
                    e.message ?: application.getString(R.string.moon_error_fetch_failed)
                )
            }
        }
    }
}

// ── Hurricane ────────────────────────────────────────────────────────────────

private const val HURRICANE_TAG = "HurricaneViewModel"

sealed class HurricaneUiState {
    data object Loading : HurricaneUiState()
    data class Success(val hurricaneData: HurricaneData) : HurricaneUiState()
    data class Error(val message: String) : HurricaneUiState()
}

class HurricaneViewModel(
    private val application: Application,
    private val repository: HurricaneRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<HurricaneUiState>(HurricaneUiState.Loading)
    val uiState: StateFlow<HurricaneUiState> = _uiState.asStateFlow()

    init {
        refreshHurricaneData()
    }

    fun refreshHurricaneData() {
        viewModelScope.launch {
            refreshHurricaneDataAwait()
        }
    }

    suspend fun refreshHurricaneDataAwait() {
        if (_loadInFlight) return

        _loadInFlight = true
        val cached = repository.hurricaneData.value
        if (cached != null) {
            _uiState.value = HurricaneUiState.Success(cached)
        } else {
            _uiState.value = HurricaneUiState.Loading
        }

        try {
            repository.refreshHurricaneData().fold(
                onSuccess = { hurricaneData ->
                    _uiState.value = HurricaneUiState.Success(hurricaneData)
                    viewModelScope.launch(Dispatchers.IO) {
                        WeatherWidgetUpdateManager.updateAllWidgets(application)
                    }
                },
                onFailure = { exception ->
                    Log.e(HURRICANE_TAG, "Error refreshing hurricane data", exception)
                    if (cached != null) {
                        _uiState.value = HurricaneUiState.Success(cached)
                    } else {
                        _uiState.value = HurricaneUiState.Error(hurricaneErrorMessage(exception))
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(HURRICANE_TAG, "Error refreshing hurricane data", e)
            if (cached != null) {
                _uiState.value = HurricaneUiState.Success(cached)
            } else {
                _uiState.value = HurricaneUiState.Error(hurricaneErrorMessage(e))
            }
        } finally {
            _loadInFlight = false
        }
    }

    private var _loadInFlight = false

    private fun hurricaneErrorMessage(e: Throwable): String {
        val root = generateSequence(e) { it.cause }.last()
        return when {
            root is UnknownHostException ->
                application.getString(R.string.hurricane_error_no_connection)
            root is SocketTimeoutException || root is TimeoutCancellationException ->
                application.getString(R.string.hurricane_error_timeout)
            e.message?.contains("timeout", ignoreCase = true) == true ->
                application.getString(R.string.hurricane_error_timeout)
            else -> application.getString(R.string.hurricane_error_generic)
        }
    }
}

// ── App updates ──────────────────────────────────────────────────────────────

class UpdateViewModel(
    private val application: Application,
    private val owner: String = "IzanahTech",
    private val repo: String = "WeatherPossum"
) : ViewModel() {

    var candidate by mutableStateOf<InAppUpdater.UpdateCandidate?>(null)
        private set

    var downloading by mutableStateOf(false)
        private set

    var progressText by mutableStateOf<String?>(null)
        private set

    var downloadProgress by mutableStateOf(0f)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var hasChecked by mutableStateOf(false)
        private set

    fun checkForUpdates(context: Context) = viewModelScope.launch {
        if (hasChecked) return@launch
        hasChecked = true

        val appContext = context.applicationContext
        runCatching {
            withContext(Dispatchers.IO) {
                InAppUpdater.checkLatest(appContext, owner, repo)
            }
        }.onSuccess { cand ->
            if (cand != null) {
                val isNewer = InAppUpdater.isNewerThanInstalled(appContext, cand.tag) ||
                    InAppUpdater.isNewerThanInstalled(appContext, cand.versionName)
                candidate = if (isNewer) cand else null
            }
        }.onFailure {
            Log.e("UpdateViewModel", "Update check failed", it)
        }
    }

    fun downloadAndInstall(context: Context) = viewModelScope.launch {
        val cand = candidate ?: return@launch
        val appContext = context.applicationContext

        downloading = true
        error = null
        progressText = application.getString(R.string.update_downloading)
        downloadProgress = 0f

        try {
            val apk = withContext(Dispatchers.IO) {
                InAppUpdater.downloadToCache(
                    appContext,
                    cand.apkUrl,
                    "update.apk",
                    onProgress = { progress ->
                        withContext(Dispatchers.Main) { downloadProgress = progress }
                    }
                )
            }

            if (cand.shaUrl.isNotBlank()) {
                progressText = application.getString(R.string.update_verifying_checksum)
                downloadProgress = 0.9f
                withContext(Dispatchers.IO) {
                    val shaFile = InAppUpdater.downloadToCache(
                        appContext,
                        cand.shaUrl,
                        "update.apk.sha256"
                    )
                    val expected = InAppUpdater.readSha256File(shaFile)
                    val actual = InAppUpdater.computeSha256(apk)
                    require(expected == actual) { "Checksum mismatch" }
                }
            }

            progressText = application.getString(R.string.update_verifying_signature)
            downloadProgress = 0.95f
            withContext(Dispatchers.IO) {
                require(InAppUpdater.isSignedBySameCert(appContext, apk)) { "Signature mismatch" }
            }

            downloadProgress = 1.0f
            progressText = application.getString(R.string.update_installing)
            InAppUpdater.installApk(context, apk)
        } catch (e: Exception) {
            Log.e("UpdateViewModel", "Update install failed", e)
            error = e.message
            progressText = null
            downloadProgress = 0f
        } finally {
            downloading = false
        }
    }

    fun clearError() {
        error = null
    }

    fun dismissUpdate() {
        candidate = null
    }

    fun resetCheckState() {
        hasChecked = false
        error = null
    }
}
