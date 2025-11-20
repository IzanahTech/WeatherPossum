# Changelog

## Version 1.5.1

### Features
- **Daylight Progress Indicator**: Added a daylight progress bar to the greeting card showing the percentage of daylight elapsed. Uses Material3's `LinearWavyProgressIndicator` for an expressive, animated wavy progress indicator.

### Improvements
- **Tab Navigation**: Fixed tab switching behavior - when switching between "Now" and "Extras" tabs, the scroll position now always resets to the top, providing a consistent user experience.
- **Greeting Card Visual Polish**: 
  - Removed card elevation to eliminate shadow artifacts
  - Added proper clipping to hide container edges around rounded corners
  - Improved visual consistency

### Technical Updates
- Updated Material3 dependency to version 1.4.0-alpha04 to support `LinearWavyProgressIndicator`
- Added opt-in for Material3 experimental APIs in compiler arguments
- Code cleanup: Fixed compiler warnings for unused variables and deprecated APIs

### Bug Fixes
- Fixed forecast card display logic to restore proper functionality
- Fixed various compiler warnings and deprecated API usage

---

## Version 1.5.0
*(Previous version - changelog not available)*

