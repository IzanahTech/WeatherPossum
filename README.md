# WeatherPossum README

# 🌤️ WeatherPossum

**Weather for Dominica, without the clutter. 🇩🇲**

WeatherPossum is a native Android weather app built specifically for **Dominica**.

It takes official forecasts and meteorological information from the **Dominica Meteorological Service**, combines them with Atlantic tropical-weather information from the **U.S. National Hurricane Center**, and presents everything in a clean, friendly interface designed for quick everyday use.

WeatherPossum isn't trying to be another worldwide weather platform. It has one job: make the weather information that matters to people in Dominica easier to access, understand and use.

---

## ✨ Features

### 🌦️ Official Dominica Forecast

WeatherPossum retrieves and presents forecast information published by the Dominica Meteorological Service, including:

* Current official forecast
* Meteorological synopsis
* Weather warnings and advisories
* Wind direction and speed
* Sea conditions
* Wave heights
* High and low tides
* Extended weather outlook
* Weather Outlook for Dominica and the Lesser Antilles

Condition-aware animations accompany the forecast while the official meteorological information remains the focus.

---

### ⚠️ Warnings & Advisories

Warnings and advisories contained in the official forecast are displayed prominently within the app.

These may include:

* Small Craft Advisories
* Flood Watches and Warnings
* Severe weather advisories
* Other notices published by the Dominica Meteorological Service

WeatherPossum does not generate its own weather warnings.

---

### 🌊 Wind, Seas & Tides

Marine information is given dedicated space in WeatherPossum because sea conditions matter just as much as the general forecast on an island.

Depending on the current forecast, the app can display:

* Wind direction
* Wind speed
* Gust information
* Sea state
* Wave heights in metres and feet
* High tide times
* Low tide times
* Marine advisories

---

### 📆 Extended Outlook

WeatherPossum presents the extended forecast published by the Dominica Meteorological Service.

Individual forecast days can be expanded to show detailed information including:

* Expected weather
* Wind conditions
* Sea state
* Wave heights
* Applicable advisories

Each forecast period is paired with condition-aware weather artwork/animation.

The title of the forecast product is preserved from the Dominica Meteorological Service.

---

### 🌎 Weather Outlook

The app includes the longer-form **Weather Outlook for Dominica and the Lesser Antilles**.

This preserves the regional meteorological discussion published by the Dominica Meteorological Service, which may contain information about:

* Tropical waves
* Pressure systems
* Moisture and atmospheric instability
* Saharan dust
* Rainfall expectations
* Regional weather patterns
* Developing weather systems

---

## ☀️ Sun Information

WeatherPossum includes locally calculated solar information for Dominica.

The Sun card provides:

* Sunrise
* Sunset
* Day length
* Daylight progress
* Solar altitude
* Solar azimuth

The main greeting card also includes an animated daylight-progress indicator showing the progression of the current day between sunrise and sunset.

---

## 🌙 Moon Information

WeatherPossum performs local astronomical calculations to provide detailed lunar information including:

* Current moon phase
* Illumination percentage
* Moonrise
* Moonset
* Next major moon phase
* Date of the next phase

Astronomical calculations are performed using **Time4A**.

---

## 🌀 Atlantic Tropical Weather

WeatherPossum includes Atlantic tropical-weather information from the **U.S. National Hurricane Center (NHC)**.

### Hurricane Outlook

The Hurricane Outlook can display:

* Areas being monitored for tropical development
* Tropical disturbances
* Formation probabilities
* Regional tropical-weather discussions

Individual areas of interest are separated for easier reading.

### Active Storms

When tropical cyclones are active in the Atlantic, WeatherPossum provides dedicated Active Storms information.

When there are no active systems, the app clearly indicates that the Atlantic is quiet.

---

## 🐾 WeatherPossum Facts

Weather checking doesn't have to be completely serious.

WeatherPossum includes a collection of weather, climate and hurricane facts covering Dominica and the wider Caribbean.

Tap the WeatherPossum Fact card to discover another one.

Occasionally the possum chooses historical violence before breakfast.

---

## 🏠 Android Home-Screen Widget

WeatherPossum includes a responsive Android home-screen widget built with **Jetpack Glance**.

The widget has its own layout, text-sizing, wrapping and update logic designed to adapt to different widget sizes while providing useful forecast information without opening the full app.

Background updates are handled using Android WorkManager.

---

## 🎨 Interface

WeatherPossum uses a custom card-based interface built entirely with **Jetpack Compose**.

The current interface includes:

* Condition-aware animated weather icons
* Dynamic greeting card
* Animated daylight-progress indicator
* Floating pill-style **Now / Extras** navigation
* Expandable forecast cards
* Dedicated Sun and Moon cards
* Hurricane Outlook and Active Storm cards
* WeatherPossum Facts
* Pull-to-refresh
* Loading and status indicators
* Light and dark theme support
* Material 3 components

Cards use different visual treatments to distinguish general weather, astronomical information, tropical systems, warnings and other types of information while retaining a consistent overall design.

---


## 🗺️ Data Sources

### Dominica Meteorological Service

The Dominica Meteorological Service is WeatherPossum's primary source for local weather information.

WeatherPossum retrieves and parses published Met Office information including forecasts, outlooks and marine conditions.

### U.S. National Hurricane Center

The National Hurricane Center provides WeatherPossum's Atlantic tropical-weather information, including areas of potential development and active tropical cyclones.

WeatherPossum does not generate independent meteorological forecasts.

---

## 📍 Location

WeatherPossum is intentionally designed for **Dominica**.

It is not a multi-location or worldwide weather service.

The application uses Dominica-specific location and timezone information where required for local calculations.

**Timezone:** `America/Dominica`

This narrow focus is deliberate: WeatherPossum exists to provide a better interface to the weather information that matters locally.

---

## 💾 Offline Support

WeatherPossum caches retrieved weather information so previously loaded forecast data can remain available when connectivity is temporarily unavailable.

This is particularly useful when mobile or internet connectivity becomes unreliable during poor weather.

Pull-to-refresh allows the latest information to be requested whenever a connection is available.

---

## 🔄 Secure In-App Updates

WeatherPossum includes its own update system using **GitHub Releases**.

When the application starts, it can check for a newer WeatherPossum release. If an update is available, the user is given the option to download and install it.

The updater includes:

* GitHub Releases integration
* APK downloading
* SHA-256 checksum verification
* APK signing-certificate verification
* Secure FileProvider handling
* Download progress
* Android system installation flow
* Release notes

Downloaded APKs are checked to ensure that they are signed by the same developer certificate as the installed WeatherPossum application.


---

## 🏗️ Architecture

WeatherPossum is a native Android application written in Kotlin.

The codebase separates data retrieval, parsing, application/domain logic, presentation and widget functionality.

```text
app/src/main/java/com/weatherpossum/app/
├── data/
│   ├── api/              # Network/API access
│   ├── model/            # Weather and application data models
│   ├── parser/           # Met Office parsing and mapping
│   └── repository/       # Data repositories
│
├── di/                   # Dependency injection
├── domain/               # Application/domain logic
│
├── presentation/
│   ├── components/       # Compose cards, animations and navigation
│   └── ...               # Screens and presentation logic
│
├── ui/
│   └── theme/            # Application theme
│
├── util/                 # Utilities and update infrastructure
│
├── widget/               # Glance home-screen widget
│
└── AppEntry.kt           # Application entry point
```

---

## 🌦️ Forecast Parsing

The Dominica Meteorological Service publishes its forecasts primarily as web content rather than through a dedicated WeatherPossum API.

WeatherPossum therefore contains a dedicated parsing layer for locating, interpreting and mapping the published information into app data.

Current parser components include:

```text
DMOForecastParser
DominicaWeatherParser
ExtendedForecastParser
ForecastPageLocator
TwoTextParser
WeatherCardMapper
```

The parsing layer is designed to handle variations in the way forecast information is published while preserving the meaning of the official forecast.

Because WeatherPossum depends on external source formatting, parser resilience is an important part of ongoing development.

---

## 🧰 Technology

WeatherPossum currently uses:

* **Kotlin**
* **Jetpack Compose**
* **Material 3**
* **Koin**
* **Retrofit**
* **OkHttp**
* **JSoup**
* **Moshi**
* **DataStore**
* **Time4A**
* **Lottie Compose**
* **Jetpack Glance**
* **WorkManager**
* **Haze**
* **AndroidX**

### Current Android Configuration

| Setting        | Value                   |
| -------------- | ----------------------- |
| Application ID | `com.weatherpossum.app` |
| Minimum SDK    | 31                      |
| Compile SDK    | 37                      |
| Target SDK     | 37                      |
| JVM Target     | 17                      |

Release builds use code shrinking, resource shrinking and optimized ProGuard/R8 configuration.

---

## 🚀 Building WeatherPossum

### Requirements

* Android Studio
* JDK 17
* Android SDK
* Internet connection for dependencies and live weather retrieval

Clone the repository:

```bash
git clone https://github.com/IzanahTech/WeatherPossum.git
cd WeatherPossum
```

Open the project in Android Studio and allow Gradle to synchronize.

To build a debug APK from the command line:

```bash
./gradlew assembleDebug
```

To run the unit tests:

```bash
./gradlew test
```

To run the complete Gradle verification tasks:

```bash
./gradlew check
```

---

## 📦 Release System

WeatherPossum includes an automated release assistant:

```bash
./create_release.sh
```

The release script handles the complete WeatherPossum release workflow.

It can:

1. Verify that the Git working tree is clean.
2. Select or calculate the next semantic version.
3. Increment Android `versionCode`.
4. Collect release notes.
5. Update `CHANGELOG.md`.
6. Build the signed release APK using Gradle.
7. Verify the APK's `versionName` and `versionCode`.
8. Verify the APK signature.
9. Confirm that the APK is signed with the expected WeatherPossum release certificate.
10. Generate the SHA-256 checksum.
11. Create the release commit.
12. Create an annotated Git tag.
13. Atomically push the commit and tag.
14. Create the GitHub Release.
15. Upload the APK and checksum.

### Version Bumps

```bash
./create_release.sh --patch
./create_release.sh --minor
./create_release.sh --major
./create_release.sh --version 2.0.0
```

A dry run is also available:

```bash
./create_release.sh --patch --dry-run
```

### Signing

Release signing is configured through a local:

```text
keystore.properties
```

The signing configuration contains the keystore location and credentials and **must not be committed to the repository**.

The release script can assist with creating this local configuration.

---

## 🧪 Testing

Run unit tests:

```bash
./gradlew test
```

Run Android instrumented tests:

```bash
./gradlew connectedAndroidTest
```

Run the project's Gradle verification tasks:

```bash
./gradlew check
```

Parser changes should be tested carefully because the reliability of WeatherPossum depends on correctly interpreting externally published meteorological information.

---

## 🛠️ Project Direction

WeatherPossum is intentionally focused.

The goal is **not** to reproduce every feature available in large commercial weather applications.

Development is primarily focused on improving the information WeatherPossum already provides:

* UI consistency and readability
* Forecast presentation
* Parser resilience
* Widget refinement
* Tropical-weather presentation
* Reliability and maintenance

### Planned

* **Warning and advisory notifications**

Notifications are intended to alert users when important warnings or advisories are issued or meaningfully changed without turning WeatherPossum into a noisy general-purpose notification service.

Features such as worldwide locations, large-scale radar integration and historical weather databases are not currently part of WeatherPossum's direction.

---

## 🤝 Contributing

Contributions, bug reports and suggestions are welcome.

When contributing:

1. Fork the repository.
2. Create a focused branch for your change.
3. Keep changes consistent with WeatherPossum's Dominica-first scope.
4. Test your changes.
5. Open a pull request explaining what changed and why.

For forecast-parser changes in particular, avoid altering the meaning of official meteorological information when transforming it for display.

---

## 📄 License

WeatherPossum is licensed under the **MIT License**.

See [`LICENSE`](LICENSE) for details.

---

## ⚠️ Disclaimer

WeatherPossum is an independent application.

It is **not an official application of the Dominica Meteorological Service, the Government of Dominica, or the U.S. National Hurricane Center**.

WeatherPossum retrieves, processes and presents information from official meteorological sources but should not be treated as a replacement for official emergency instructions.

During hazardous or severe weather, always follow information and instructions issued directly by the appropriate authorities.

---

## 🙏 Credits

WeatherPossum relies on information and open-source technology from a number of sources, including:

* **Dominica Meteorological Service** — local forecasts, outlooks and marine information
* **U.S. National Hurricane Center** — Atlantic tropical-weather information
* **Time4A / Time4J** — astronomical calculations
* **Jetpack Compose and AndroidX**
* The open-source libraries used throughout the project

Additional acknowledgements are available from the **Credits** section inside WeatherPossum.

---

## 🐾 Why WeatherPossum?

Because weather apps don't all need to become enormous platforms.

WeatherPossum is built around a simple idea:

**Take the official weather information available for Dominica and make it pleasant, useful and easy to read.**

<p align="center">
  <strong>Made for Dominica 🇩🇲</strong>
</p>

<p align="center">
  <em>Weather without the clutter.</em>
</p>
