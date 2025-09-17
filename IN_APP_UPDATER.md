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

## 🎨 UI Components

### UpdateSheet

The update dialog displays:
- **Version Information** - New version name and tag
- **Release Notes** - Changelog from GitHub release
- **Progress Indicators** - Download and verification status
- **Error Handling** - Clear error messages for failed operations

### User Experience

- **Non-blocking** - Users can dismiss and continue using the app
- **Progress Feedback** - Clear indication of download/verification progress
- **Error Recovery** - Graceful handling of network or verification failures

## 🚀 Deployment

### Creating Releases

1. **Build APK** - Generate signed APK for release
2. **Create Checksum** - Generate SHA256 file:
   ```bash
   sha256sum WeatherPossum-v1.2.0.apk > WeatherPossum-v1.2.0.apk.sha256
   ```
3. **Upload to GitHub** - Create release with APK and checksum files
4. **Add Release Notes** - Include changelog and new features

### Testing Updates

1. **Install Test Version** - Install APK with lower version code
2. **Create Release** - Upload newer version to GitHub
3. **Test Update Flow** - Verify download, verification, and installation

## 🔧 Troubleshooting

### Common Issues

**"Install unknown apps" permission required**
- Android 8+ requires users to enable installation from your app
- The updater automatically opens the permission settings

**Checksum verification failed**
- Ensure SHA256 file format matches: `hash filename`
- Verify the checksum file was uploaded correctly

**Signature verification failed**
- Ensure APK is signed with the same certificate as installed app
- Check that the signing configuration is consistent

### Debug Information

The updater provides detailed error messages for:
- Network failures
- Checksum mismatches
- Signature verification failures
- Installation permission issues

## 📈 Future Enhancements

- **Delta Updates** - Download only changed parts of the app
- **Background Updates** - Download updates in background
- **Rollback Support** - Ability to revert to previous version
- **Update Scheduling** - Schedule updates for optimal times
- **Bandwidth Management** - Pause/resume downloads

---

**The in-app updater provides a seamless way to keep WeatherPossum users on the latest version with enhanced security and user experience.**
