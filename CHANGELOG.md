# WeatherPossum Changelog

# Changelog

All notable changes to WeatherPossum are documented here.

WeatherPossum follows semantic-style versioning where practical, although some historical releases were rapid maintenance or versioning updates.

---

## [1.8.6] - 2026-08-12

### Fixed

* Fixed an issue where Central Tropical Atlantic forecasts were not separated into their own card.

---

## [1.8.5] - 2026-06-30

### Added

* Added the next moon phase and its date to the Moon Phase card.

### Changed

* Removed the WeatherPossum widget name from the home-screen widget.
* Credits card now opens with the first credit expanded.

---

## [1.8.0] - 2026-06-30

### Fixed

* Improved home-screen widget text scaling across different widget sizes and display configurations.

---

## [1.7.6] - 2026-06-30

### Changed

* Further optimized the WeatherPossum home-screen widget.

---

## [1.7.5] - 2026-06-18

### Fixed

* Fixed an issue that made the Active Storms card difficult to read in dark mode.

---

## [1.7.4] - 2026-06-16

### Added

* Added an animated icon to the Credits card.
* Added credit for the developer of Time4J, which powers WeatherPossum's Sun and Moon calculations.

---

## [1.7.3] - 2026-06-16

### Added

* Added a dedicated Credits section acknowledging the data providers, libraries and developers that make WeatherPossum possible.

---

## [1.7.2] - 2026-06-16

### Fixed

* Fixed a home-screen widget display issue.
* Removed a redundant secondary widget configuration screen when adding the widget.

---

## [1.7.1] - 2026-06-16

### Fixed

* Fixed an application icon display issue.

### Changed

* Updated the application version to 1.7.1.

---

## [1.7.0] - 2026-06-16

### Changed

* Modernized the Android build toolchain and project dependencies.
* Updated libraries and build configuration.
* Improved lint and build cleanliness.
* Applied security-related dependency and configuration updates.
* Performed general project optimizations.

---

## [1.6.2] - 2026-06-12

### Fixed

* Fixed several issues in the in-app updater.
* Improved APK installation by correctly resolving the active Android `Activity`.
* Ensured installation intents are launched with the appropriate task flags.
* Updated the install flow to use the active Activity rather than a generic context wrapper.
* Fixed duplicated `WeatherPossum` text in update-version labels.

---

## [1.6.1] - 2026-06-12

### Fixed

* General bug fixes following the 1.6.0 overhaul.

### Changed

* Updated application versioning.

---

## [1.6.0] - 2026-06-12

### Major UI Overhaul

WeatherPossum received a substantial interface and codebase redesign in this release.

### Changed

* Redesigned large portions of the application interface.
* Refined WeatherPossum's visual language and card-based presentation.
* Improved overall layout and information hierarchy.
* Enhanced existing weather cards and presentation components.
* Performed extensive code cleanup and internal restructuring.
* Improved general app polish and behavior.

### Fixed

* Fixed a collection of UI and application bugs uncovered during the redesign.

---

## [1.5.2] - 2025-11-20

### Changed

* Maintenance/versioning release based on the 1.5.1 feature set.

---

## [1.5.1] - 2025-11-20

### Added

* Added an animated daylight-progress indicator to the greeting card using Material 3's `LinearWavyProgressIndicator`.
* The indicator shows the percentage of daylight elapsed between sunrise and sunset.

### Changed

* Improved greeting-card visuals.
* Updated Material 3 dependencies.
* Improved tab navigation behavior so switching sections resets the scroll position appropriately.

### Fixed

* Fixed greeting-card clipping and shadow artifacts.
* Fixed compiler warnings and related Material 3 API configuration issues.

---

## [1.5.0] - 2025-11-18

### Changed

* Enhanced the WeatherPossum interface for Material You / Material 3 Expressive styling.
* Refined application visuals and presentation.

### Fixed

* General bug fixes.

---

## [1.4.9] - 2025-09-24

### Fixed

* Fixed multiple issues in the hurricane parser.
* Applied additional application fixes.

---

## [1.4.7] - 2025-09-24

### Fixed

* Fixed multiple issues in the hurricane parser.
* Applied additional application fixes.

---

## [1.4.6] - 2025-09-23

### Added

* Added enhanced Atlantic hurricane tracking.
* Added individual containers for tropical systems in the Hurricane Outlook.
* Added formation-probability indicators for tropical systems.
* Added additional parser and data-model unit tests.

### Changed

* Reworked hurricane data retrieval to use National Hurricane Center JSON feeds instead of RSS scraping.
* Restricted tropical-cyclone tracking to Atlantic systems.
* Improved Dominica Meteorological Service forecast parsing with more resilient title classification.
* Improved Kotlin implementation patterns and project build compatibility.

### Fixed

* Fixed Java compatibility issues and build warnings.

---

## Earlier Development - 2025

WeatherPossum evolved rapidly during its initial development period.

Notable early work included:

### Weather Data and Parsing

* Replaced experimental GPT-based HTML-to-JSON parsing with deterministic local parsing.
* Added more resilient parsing logic for Dominica Meteorological Service forecast pages.
* Overhauled forecast parsing multiple times to better tolerate changes in source-page formatting.
* Improved weather-condition classification logic.

### User Interface

* Reworked the initial interface to make the app easier to navigate.
* Added and refined condition-aware Lottie weather animations.
* Improved dark-mode behavior.
* Improved greeting-card behavior and personalization.
* Added pull-to-refresh using Jetpack Compose Material 3.

### Forecast and Extras

* Added the extended forecast.
* Added expandable extended-forecast presentation.
* Added Moon Phase information.
* Added WeatherPossum Facts.
* Improved the Extras section.

### Tropical Weather

* Added and repeatedly refined the Hurricane Outlook.
* Separated individual tropical systems into their own containers.
* Improved active-system parsing and display.

### Application Updates

* Added an in-app update system using GitHub Releases.
* Improved the updater to operate automatically and quietly when no update is available.
* Added automatic application relaunch behavior after update installation.

### Internal Improvements

* Refactored and cleaned up large portions of the codebase.
* Standardized project dependencies.
* Improved Gradle configuration.
* Fixed compilation and KSP/Moshi configuration issues.

---

## Current Development Direction

WeatherPossum is intentionally focused on delivering clear, reliable weather information for Dominica rather than becoming a general-purpose global weather platform.

Current work primarily focuses on:

* Improving UI consistency and readability
* Strengthening forecast parser resilience
* Refining existing weather cards
* Improving the home-screen widget
* Improving warning and advisory handling
* Adding notifications for newly issued or changed warnings and advisories
* General reliability and maintenance
