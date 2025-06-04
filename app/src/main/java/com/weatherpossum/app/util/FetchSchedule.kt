package com.weatherpossum.app.util

import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object FetchSchedule {
    private const val FETCH_HOUR = 4 // 4 AM

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