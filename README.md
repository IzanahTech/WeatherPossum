# WeatherPossum

A beautiful, modern Android weather application built with Jetpack Compose, featuring accurate astronomical calculations and comprehensive weather information for Dominica and the Caribbean region.

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

### Project Structure
```
app/src/main/java/com/weatherpossum/app/
├── data/
│   ├── api/              # Network API interfaces
│   ├── model/            # Data models
│   ├── parser/           # HTML parsing logic
│   └── repository/       # Data repositories
├── presentation/
│   ├── components/       # Reusable UI components
│   └── *.kt             # Screen composables
├── ui/
│   ├── theme/           # App theming
│   └── viewmodel/       # ViewModels
├── util/                # Utility classes
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
   git clone https://github.com/yourusername/WeatherPossum.git
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

## 📱 Screenshots

<img width="1080" height="2400" alt="Screenshot_20250917_100914" src="https://github.com/user-attachments/assets/a14cea56-6609-4d5d-a54e-d8a2d2c445fe" />
<img width="1080" height="2400" alt="Screenshot_20250917_100906" src="https://github.com/user-attachments/assets/3944ad1b-4311-44b3-81cd-3c1df1666bd8" />
<img width="1080" height="2400" alt="Screenshot_20250917_100852" src="https://github.com/user-attachments/assets/73f21c0f-82d7-4036-ac4e-334353ce7957" />
<img width="1080" height="2400" alt="Screenshot_20250917_100839" src="https://github.com/user-attachments/assets/33fbb7b2-b118-4874-8f08-a8b991ef823d" />


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

### Utility Dependencies
- **Time4A** 4.8-2021a - Astronomical calculations
- **DataStore Preferences** 1.0.0 - Data persistence
- **Koin** 3.5.6 - Dependency injection

## 🤝 Contributing

Feel free to contribute towards this project however you can. 

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


1. Check the [Issues](https://github.com/yourusername/WeatherPossum/issues) page
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

### Version History
- **v1.0.0** - Initial release with core weather features
- **v1.1.0** - Added moon phase calculations
- **v1.2.0** - Enhanced hurricane tracking
- **v1.3.0** - UI/UX improvements and accessibility

---

**Made with ❤️ for Dominica and the Caribbean**

*WeatherPossum - Your trusted weather companion*
