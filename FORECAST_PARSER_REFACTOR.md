# Forecast Parser Refactor Summary

## Problem Solved
The original forecast parsing was brittle and would break whenever the `<div class="forecast_for_today">` title changed (e.g., "Forecast for Today and Tonight", "Forecast for This Evening & Tonight"). This caused `forecastTonight` or `forecastTomorrow` to return null, leaving the Forecast card empty.

## Solution Implemented

### 1. New Data Classes
- **`DMOForecastModels.kt`**: Contains the parsed result with section type, title, and body
- **`ForecastSection`**: Sealed class with TODAY, TONIGHT, TODAY_TONIGHT, TOMORROW, TWENTY_FOUR_HOURS, and UNKNOWN variants
- **`DMOForecastResult`**: Data class with section, titleRaw, body, and sourceUrl
- **`ParseException`**: Custom exception for parsing errors

### 2. New Robust Parser
- **`DMOForecastParser.kt`**: Robust parser that handles title variations
- Uses Jsoup to extract the `<div class="forecast_for_today">` section with multiple fallback selectors
- Normalizes title text by lowercasing, trimming, stripping "forecast"/"for", collapsing whitespace, and converting & → "and"
- Maps to enums using regex-based classification for future-proofing
- Handles missing `<strong>` tags by using first paragraph's ownText
- Cleans HTML entities and normalizes whitespace
- Returns single result instead of list for simplicity

### 3. Optional Source Wrapper
- **`DMOForecastSource.kt`**: Lightweight wrapper for fetching HTML from weather.gov.dm
- Uses OkHttpClient for HTTP requests
- Includes proper User-Agent header

### 4. Updated Integration
- **`WeatherRepository.kt`**: Updated to use the new `DMOForecastParser`
- Uses new parser for forecast content while keeping existing parser for other data
- Dynamic title generation based on section type
- Maintains backward compatibility with existing card structure

### 5. Comprehensive Testing with Fixtures
- **`DMOForecastParserTest.kt`**: Unit tests using HTML fixtures
- **Fixtures in `app/src/test/resources/fixtures/dmo/`**:
  - `forecast_today_tonight.html`
  - `forecast_tonight.html`
  - `forecast_this_evening_and_tonight.html`
  - `forecast_today.html`
  - `forecast_no_strong.html`
  - `forecast_unknown_title.html`

## Key Features

### Resilient Title Classification
The parser uses regex-based pattern matching to classify forecast sections:
- **TODAY_TONIGHT**: Contains both "today" and "tonight"/"evening"
- **TONIGHT**: Contains "tonight" or "overnight"
- **TODAY**: Contains "today" (but not "tonight")
- **TOMORROW**: Contains "tomorrow" or "next day"
- **TWENTY_FOUR_HOURS**: Contains "next 24 hours" or "24 hrs"
- **UNKNOWN**: Preserves original title for display

### Error Handling
- Throws `ParseException` for critical errors (container not found)
- Gracefully handles malformed HTML and missing elements
- Falls back to cached data in WeatherRepository if parsing fails
- Never fails the whole parse just because title changed

### Backward Compatibility
- No changes to UI code required
- WeatherRepository continues to work with existing card structure
- Existing error handling and caching mechanisms preserved
- Uses new parser for forecast content while keeping existing parser for other data

## Files Modified/Created

### New Files
- `app/src/main/java/com/weatherpossum/app/data/parser/DMOForecastModels.kt`
- `app/src/main/java/com/weatherpossum/app/data/parser/DMOForecastParser.kt`
- `app/src/main/java/com/weatherpossum/app/data/parser/DMOForecastSource.kt`
- `app/src/test/java/com/weatherpossum/app/data/parser/DMOForecastParserTest.kt`
- `app/src/test/resources/fixtures/dmo/` (6 HTML fixture files)

### Modified Files
- `app/src/main/java/com/weatherpossum/app/data/repository/WeatherRepository.kt`

## Testing
The implementation includes comprehensive unit tests that can be run with:
```bash
./gradlew test --tests DMOForecastParserTest
```

## Benefits
1. **Resilient**: Handles title variations without breaking
2. **Maintainable**: Clear separation of concerns with dedicated parser
3. **Testable**: Comprehensive unit test coverage
4. **Future-proof**: Regex-based classification can handle new variations
5. **Backward compatible**: No breaking changes to existing code
6. **Defensive**: Graceful error handling prevents app crashes
