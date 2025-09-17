package com.weatherpossum.app.util

import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object FetchSchedule {
    private const val FETCH_HOUR = 4 // 4 AM
    private const val RATE_LIMIT_COOLDOWN_HOURS = 6 // Wait 6 hours after rate limit error

    /**
     * Checks if we need to fetch new data based on the last fetch time
     * @param lastFetchTime The timestamp of the last successful fetch
     * @return true if we need to fetch new data, false otherwise
     */
    fun shouldFetchMoonData(lastFetchTime: Long?): Boolean {
        if (lastFetchTime == null) return true

        val lastFetch = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(lastFetchTime),
            ZoneId.systemDefault()
        )
        val now = LocalDateTime.now()

        // If we've never fetched before, or if it's a new day after 4 AM
        return lastFetch.toLocalDate().isBefore(now.toLocalDate()) &&
               now.toLocalTime().isAfter(LocalTime.of(FETCH_HOUR, 0))
    }

    /**
     * Checks if we should skip fetching due to recent rate limit errors
     * @param lastRateLimitErrorTime The timestamp of the last rate limit error
     * @return true if we should skip fetching due to recent rate limit error
     */
    fun shouldSkipDueToRateLimit(lastRateLimitErrorTime: Long?): Boolean {
        if (lastRateLimitErrorTime == null) return false

        val lastError = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(lastRateLimitErrorTime),
            ZoneId.systemDefault()
        )
        val now = LocalDateTime.now()

        // Skip if less than RATE_LIMIT_COOLDOWN_HOURS have passed since the last rate limit error
        return lastError.plusHours(RATE_LIMIT_COOLDOWN_HOURS.toLong()).isAfter(now)
    }

    /**
     * Gets the next fetch time for moon data
     * @return LocalDateTime representing the next scheduled fetch time
     */
    fun getNextMoonFetchTime(): LocalDateTime {
        val now = LocalDateTime.now()
        return if (now.toLocalTime().isBefore(LocalTime.of(FETCH_HOUR, 0))) {
            // If it's before 4 AM, schedule for today at 4 AM
            now.with(LocalTime.of(FETCH_HOUR, 0))
        } else {
            // If it's after 4 AM, schedule for tomorrow at 4 AM
            now.plusDays(1).with(LocalTime.of(FETCH_HOUR, 0))
        }
    }
} 