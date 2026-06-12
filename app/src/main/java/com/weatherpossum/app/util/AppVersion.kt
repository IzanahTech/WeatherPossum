package com.weatherpossum.app.util

/**
 * Semantic version parsing and comparison for in-app update checks.
 */
object AppVersion {

    private val VERSION_NUMBER = Regex("""(\d+(?:\.\d+)*)""")

    fun normalize(tagOrSemver: String): String {
        val stripped = tagOrSemver
            .removePrefix("v")
            .removePrefix("V")
            .replace("WeatherPossum", "", ignoreCase = true)
            .trim()
        return VERSION_NUMBER.find(stripped)?.value ?: stripped
    }

    /**
     * @return positive if [v1] > [v2], negative if [v1] < [v2], 0 if equal
     */
    fun compare(v1: String, v2: String): Int {
        val parts1 = v1.split(".").mapNotNull { it.toIntOrNull() }
        val parts2 = v2.split(".").mapNotNull { it.toIntOrNull() }
        val maxLength = maxOf(parts1.size, parts2.size)

        for (i in 0 until maxLength) {
            val part1 = parts1.getOrElse(i) { 0 }
            val part2 = parts2.getOrElse(i) { 0 }
            when {
                part1 > part2 -> return 1
                part1 < part2 -> return -1
            }
        }
        return 0
    }

    fun isNewer(remote: String, installed: String): Boolean =
        compare(normalize(remote), installed) > 0
}
