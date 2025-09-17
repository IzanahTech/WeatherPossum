# 🌤️ WeatherPossum

A beautiful, modern Android weather application built with Jetpack Compose, featuring accurate astronomical calculations, comprehensive weather information for Dominica and the Caribbean region, and **in-app updates via GitHub Releases**.

![WeatherPossum](https://img.shields.io/badge/Android-WeatherPossum-green?style=for-the-badge&logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.23-blue?style=for-the-badge&logo=kotlin)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-2025.09.00-blue?style=for-the-badge&logo=jetpack-compose)
![AGP](https://img.shields.io/badge/Android%20Gradle%20Plugin-8.6.0-green?style=for-the-badge)

## ✨ Features

### 🌦️ Weather Information
- **Real-time Weather Data** - Current conditions, forecasts, and synopsis
- **Extended Forecasts** - Multi-day weather predictions with detailed information
- **Weather Outlook** - Regional weather outlook for Dominica and Lesser Antilles
- **Wind & Sea Conditions** - Marine weather information for sailors and fishermen
- **Sun Times** - Accurate sunrise and sunset calculations

### 🌙 Astronomical Features
- **Precise Moon Phases** - Professional-grade moon phase calculations using Sun-Moon elongation
- **Moon Illumination** - Accurate illumination percentages (e.g., "Waning Crescent 10%")
- **Moonrise/Moonset Times** - Location-specific lunar timing
- **Sun Information** - Comprehensive solar data and timing

### 🌀 Hurricane Tracking
- **Atlantic Tropical Weather Outlook** - Official NHC tropical weather updates
- **Active Systems** - Current tropical storm and hurricane information
- **Formation Chances** - Probability indicators for tropical development
- **Eastern Tropical Atlantic** - Regional tropical weather analysis

### 🔄 In-App Updates
- **Fully Automatic** - Runs silently in background on app launch
- **Zero Configuration** - No user setup or manual controls required
- **Silent Operation** - Only alerts user when update is actually available
- **Secure Downloads** - SHA256 checksum verification and certificate validation
- **Seamless Experience** - Clean update dialog with minimal user interaction

### 🎨 Modern UI/UX
- **Dynamic Color Themes** - Greeting cards that change colors based on daylight percentage
- **Smooth Animations** - Lottie animations for weather conditions
- **Accessibility Compliant** - WCAG-compliant color contrast and luminance
- **Material Design 3** - Modern Android design language
- **Pull-to-Refresh** - Intuitive data refresh functionality

### 🔧 Technical Excellence
- **Offline Support** - Cached data for offline viewing
- **Robust Parsing** - Flexible HTML parsing that adapts to changing weather service formats
- **Dependency Injection** - Clean architecture with Koin DI
- **Modern Android Stack** - Latest Android SDK, Compose, and Kotlin

## 🏗️ Architecture

### Tech Stack
- **Language**: Kotlin 1.9.23
- **UI Framework**: Jetpack Compose (BOM 2025.09.00)
- **Architecture**: MVVM with Repository Pattern
- **Dependency Injection**: Koin 3.5.6
- **Networking**: Retrofit 2.11.0 + OkHttp 4.12.0
- **HTML Parsing**: JSoup 1.17.2
- **Astronomical Calculations**: Time4A 4.8-2021a
- **Data Persistence**: DataStore Preferences
- **Animations**: Lottie Compose 6.4.0
- **JSON Parsing**: Moshi 1.15.0

### Project Structure
```
app/src/main/java/com/weatherpossum/app/
├── data/
│   ├── api/              # Network API interfaces (including GitHub API)
│   ├── model/            # Data models
│   ├── parser/           # HTML parsing logic
│   └── repository/       # Data repositories
├── presentation/
│   ├── components/       # Reusable UI components (including UpdateSheet)
│   └── *.kt             # Screen composables
├── ui/
│   ├── theme/           # App theming
│   └── viewmodel/       # ViewModels (including UpdateViewModel)
├── util/                # Utility classes (including InAppUpdater)
└── di/                  # Dependency injection
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 31+ (Android 12+)
- JDK 17

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/IzanahTech/WeatherPossum.git
   cd WeatherPossum
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the WeatherPossum directory

3. **Sync and Build**
   - Android Studio will automatically sync Gradle
   - Build the project: `Build > Make Project`

4. **Run the App**
   - Connect an Android device or start an emulator
   - Click the "Run" button or use `Shift + F10`

### Build Configuration
- **Compile SDK**: 36 (Android 14)
- **Target SDK**: 36
- **Min SDK**: 31 (Android 12)
- **Java Version**: 17

## 🔄 In-App Updates

WeatherPossum includes a complete in-app updater system that allows users to update the app independently of the Play Store.

### How It Works
1. **Automatic Check** - App silently checks for updates on launch (2-second delay)
2. **Background Processing** - Runs without user awareness or performance impact
3. **Silent Result** - No update = no user notification
4. **Update Available** - Shows clean dialog with "Update Now" or "Later" options
5. **Secure Installation** - Handles download, verification, and installation automatically

### For Developers
- **Release Script** - Use `./create_release.sh` to create releases with proper files
- **Documentation** - See [IN_APP_UPDATER.md](IN_APP_UPDATER.md) for detailed implementation
- **Repository** - Configured for `IzanahTech/WeatherPossum` repository

## 📱 Screenshots

*Screenshots would go here showing the app's beautiful UI*

## 🔧 Configuration

### Weather Data Sources
The app fetches weather data from:
- **Dominica Meteorological Service** - Primary weather data
- **National Hurricane Center** - Tropical weather outlooks

### Location Settings
- **Default Location**: Dominica (15.415°N, 61.371°W)
- **Timezone**: America/Dominica
- **Elevation**: Sea level (0m)

## 🧪 Testing

### Running Tests
```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# All tests
./gradlew check
```

### Test Coverage
- Repository layer testing
- ViewModel testing
- UI component testing

## 📦 Dependencies

### Core Dependencies
- **AndroidX Core KTX** 1.12.0
- **Lifecycle Runtime KTX** 2.7.0
- **Activity Compose** 1.8.2
- **Compose BOM** 2025.09.00

### UI Dependencies
- **Material3** - Modern Material Design
- **Material Icons Extended** - Extended icon set
- **Lottie Compose** 6.4.0 - Smooth animations

### Network Dependencies
- **Retrofit** 2.11.0 - HTTP client
- **OkHttp** 4.12.0 - HTTP transport
- **JSoup** 1.17.2 - HTML parsing
- **Moshi** 1.15.0 - JSON parsing

### Utility Dependencies
- **Time4A** 4.8-2021a - Astronomical calculations
- **DataStore Preferences** 1.0.0 - Data persistence
- **Koin** 3.5.6 - Dependency injection

## 🤝 Contributing

We welcome contributions! Please follow these guidelines:

### Development Setup
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Make your changes
4. Run tests: `./gradlew check`
5. Commit changes: `git commit -m 'Add amazing feature'`
6. Push to branch: `git push origin feature/amazing-feature`
7. Open a Pull Request

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add documentation for public APIs
- Write unit tests for new features

### Commit Messages
Use descriptive commit messages:
```
feat: add moon phase calculation
fix: resolve weather parsing issue
docs: update README with new features
refactor: improve UI component structure
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Dominica Meteorological Service** for providing weather data
- **National Hurricane Center** for tropical weather information
- **Time4A Library** for professional astronomical calculations
- **Jetpack Compose Team** for the amazing UI framework
- **Material Design Team** for design guidelines

## 📞 Support

If you encounter any issues or have questions:

1. Check the [Issues](https://github.com/IzanahTech/WeatherPossum/issues) page
2. Create a new issue with detailed information
3. Include device information and error logs

## 🔮 Roadmap

### Upcoming Features
- [ ] Weather alerts and notifications
- [ ] Multiple location support
- [ ] Weather widgets
- [ ] Dark mode improvements
- [ ] Weather radar integration
- [ ] Historical weather data
- [ ] Delta updates for smaller downloads

### Version History
- **v1.0.0** - Initial release with core weather features
- **v1.1.0** - Added moon phase calculations
- **v1.2.0** - Enhanced hurricane tracking
- **v1.3.0** - UI/UX improvements and accessibility
- **v1.4.0** - In-app updater with GitHub Releases integration

---

**Made with ❤️ for Dominica and the Caribbean**

*WeatherPossum - Your trusted weather companion with seamless updates*
