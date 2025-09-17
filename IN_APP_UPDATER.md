# 🔄 In-App Updater Implementation

WeatherPossum now includes a complete in-app updater system that allows users to update the app independently of the Play Store using GitHub Releases.

## ✨ Features

- **GitHub Releases Integration** - Automatically checks for new releases
- **Secure Downloads** - SHA256 checksum verification
- **Signature Verification** - Ensures APKs are signed by the same certificate
- **User-Friendly UI** - Clean update dialog with progress indicators
- **Automatic Installation** - Handles the entire update process

## 🏗️ Architecture

### Components

1. **GitHubApi** - Retrofit interface for GitHub Releases API
2. **InAppUpdater** - Core updater logic with download and verification
3. **UpdateViewModel** - State management for update process
4. **UpdateSheet** - Compose UI for update dialog

### Security Features

- **SHA256 Verification** - Downloads and verifies checksums
- **Certificate Validation** - Ensures APK is signed by same developer
- **FileProvider Security** - Secure file sharing for APK installation

## 📱 Usage

The updater is automatically integrated into the main `WeatherScreen` and will:

1. **Check for updates** on app launch
2. **Display update dialog** if newer version is available
3. **Download and verify** the APK file
4. **Install the update** with user permission

### Manual Update Check

```kotlin
// In any composable with access to UpdateViewModel
val updateViewModel: UpdateViewModel = koinViewModel()

// Check for updates
updateViewModel.check(context)
```

## 🔧 Configuration

### GitHub Repository Setup

The updater is configured for the `IzanahTech/WeatherPossum` repository. To use with your own repository:

```kotlin
// In UpdateViewModel constructor
class UpdateViewModel(
    private val owner: String = "your-username",
    private val repo: String = "your-repo-name"
) : ViewModel()
```

### Release Requirements

For the updater to work, your GitHub releases must include:

1. **APK File** - The app's APK file
2. **SHA256 File** (Optional) - Checksum file with `.sha256` extension
3. **Release Notes** - Description of changes

### Example Release Structure

```
v1.2.0/
├── WeatherPossum-v1.2.0.apk
├── WeatherPossum-v1.2.0.apk.sha256
└── Release notes with changelog
```

## 🛡️ Security Considerations

### APK Signing

- The updater verifies that downloaded APKs are signed by the same certificate as the installed app
- This prevents malicious APKs from being installed

### Network Security

- All downloads use HTTPS
- SHA256 checksums prevent corrupted downloads
- FileProvider ensures secure file sharing

### Permissions

The updater requires these permissions:

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES"/>
```

## 🔄 Update Process Flow

1. **Check Latest Release** - Query GitHub API for latest release
2. **Compare Versions** - Determine if update is available
3. **Download APK** - Download to app's cache directory
4. **Verify Checksum** - Validate SHA256 hash (if available)
5. **Verify Signature** - Ensure certificate matches installed app
6. **Install APK** - Launch system installer with user permission

# 🔄 Automatic In-App Updater Implementation

WeatherPossum includes a **fully automatic** in-app updater system that runs silently in the background and only alerts users when updates are available.

## ✨ Features

- **🔄 Fully Automatic** - Runs silently in background on app launch
- **🔕 Silent Operation** - No user interaction required unless update is available
- **🛡️ Secure Downloads** - SHA256 checksum verification and certificate validation
- **🎯 User-Friendly** - Clean update dialog only appears when needed
- **⚡ Background Processing** - No impact on app performance or user experience

## 🏗️ Architecture

### Automatic Operation Flow

1. **App Launch** → Silent background check (2-second delay)
2. **GitHub API** → Fetch latest release information
3. **Version Comparison** → Determine if update needed
4. **Silent Result** → No update = no user notification
5. **Update Available** → Show clean dialog with options
6. **User Choice** → "Update Now" or "Later"

### Key Components

1. **UpdateViewModel** - Automatic state management with `checkForUpdates()`
2. **InAppUpdater** - Core updater logic with download and verification
3. **UpdateSheet** - Clean dialog that only appears when update is available
4. **WeatherScreen Integration** - Automatic launch-time checking

## 📱 User Experience

### Silent Operation
- **No Manual Controls** - No buttons or settings for update checking
- **Background Processing** - Runs automatically without user awareness
- **No Performance Impact** - Minimal resource usage during checks
- **Error Handling** - Silent error handling for network issues

### Update Dialog
- **Only When Needed** - Dialog only appears if update is available
- **Clear Information** - Shows version, release notes, and progress
- **Simple Choices** - "Update Now" or "Later" options
- **Progress Feedback** - Clear indication during download/installation

## 🔧 Implementation Details

### Automatic Check Timing
```kotlin
// In WeatherScreen - runs automatically on app launch
LaunchedEffect(Unit) {
    kotlinx.coroutines.delay(2000) // Wait for app to fully load
    updateViewModel.checkForUpdates(context)
}
```

### Silent Error Handling
```kotlin
// Errors are handled silently - no user notification unless critical
runCatching {
    val cand = InAppUpdater.checkLatest(context, owner, repo)
    // ... update logic
}.onFailure { 
    // Silently handle errors - don't show to user unless critical
    error = it.message
}
```

### State Management
- **hasChecked** - Prevents multiple simultaneous checks
- **candidate** - Only set when update is actually available
- **downloading** - Tracks installation progress
- **error** - Handles failures gracefully

## 🛡️ Security Features

### Automatic Verification
- **SHA256 Verification** - Downloads and verifies checksums automatically
- **Certificate Validation** - Ensures APK is signed by same developer
- **FileProvider Security** - Secure file sharing for APK installation

### Network Security
- **HTTPS Downloads** - All downloads use encrypted connections
- **Rate Limiting** - Respects GitHub API rate limits
- **Error Recovery** - Graceful handling of network failures

## 🚀 Deployment

### Creating Releases
The updater automatically detects releases with:
1. **APK File** - The app's APK file
2. **SHA256 File** (Optional) - Checksum file with `.sha256` extension
3. **Release Notes** - Description of changes

### Release Structure
```
v1.2.0/
├── WeatherPossum-v1.2.0.apk
├── WeatherPossum-v1.2.0.apk.sha256
└── Release notes with changelog
```

## 🔄 Update Process Flow

1. **App Launch** → Automatic background check (2s delay)
2. **Silent Check** → Query GitHub API for latest release
3. **Version Compare** → Determine if update is available
4. **Silent Result** → No update = no user notification
5. **Update Dialog** → Show only if update is available
6. **User Choice** → "Update Now" or "Later"
7. **Download & Verify** → Automatic download and verification
8. **Install** → Launch system installer with user permission

## 🎨 UI Components

### UpdateSheet Features
- **Conditional Display** - Only appears when update is available
- **Clean Design** - Material Design 3 with clear information
- **Progress Indicators** - Shows download and verification status
- **Error Handling** - Clear error messages for failed operations

### User Interaction
- **Minimal Required** - Only "Update Now" or "Later" choices
- **Non-blocking** - Users can dismiss and continue using app
- **Progress Feedback** - Clear indication of installation progress

## 🔧 Configuration

### Repository Setup
Configured for `IzanahTech/WeatherPossum` repository:
```kotlin
class UpdateViewModel(
    private val owner: String = "IzanahTech",
    private val repo: String = "WeatherPossum"
)
```

### Timing Configuration
- **Launch Delay** - 2 seconds after app start
- **Single Check** - Prevents multiple simultaneous checks
- **Error Recovery** - Silent handling of network issues

## 🚀 Benefits

### For Users
- **Zero Configuration** - Works automatically without setup
- **No Interruption** - Only notified when update is actually available
- **Seamless Experience** - Minimal user interaction required
- **Always Current** - Automatically keeps app up to date

### For Developers
- **No Manual Distribution** - Updates distributed via GitHub releases
- **Automatic Verification** - Built-in security checks
- **User-Friendly** - Clean, non-intrusive update experience
- **Reliable** - Robust error handling and recovery

## 🔧 Troubleshooting

### Common Scenarios

**No Update Dialog Appears**
- Check if newer version is actually available on GitHub
- Verify repository configuration in UpdateViewModel
- Check network connectivity

**Update Dialog Appears Repeatedly**
- User may have dismissed update multiple times
- Check if installation was successful
- Verify APK signature matches installed app

**Silent Failures**
- Network errors are handled silently
- Check device connectivity
- Verify GitHub API accessibility

## 📈 Future Enhancements

- **Periodic Checks** - Check for updates periodically during app usage
- **Smart Timing** - Check for updates at optimal times (WiFi, charging)
- **Background Downloads** - Download updates in background when available
- **Delta Updates** - Download only changed parts of the app

---

**The automatic in-app updater provides a seamless, silent way to keep WeatherPossum users on the latest version with zero configuration required.**

