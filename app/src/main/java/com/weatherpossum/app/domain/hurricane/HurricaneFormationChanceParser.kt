package com.weatherpossum.app.domain.hurricane

data class FormationChance(
    val timeframe: String,
    val unit: String,
    val level: String,
    val percentage: String
)

object HurricaneFormationChanceParser {
    private val patterns = listOf(
        Regex("\\*\\s*Formation chance through (\\d+) (hours?)\\.\\.\\.(\\w+)\\.\\.\\.(\\d+) percent", RegexOption.IGNORE_CASE),
        Regex("\\*\\s*Formation chance through (\\d+) (days?)\\.\\.\\.(\\w+)\\.\\.\\.(\\d+) percent", RegexOption.IGNORE_CASE),
        Regex("Formation chance through (\\d+) (hours?)\\.\\.\\.(\\w+)\\.\\.\\.(\\d+) percent", RegexOption.IGNORE_CASE),
        Regex("Formation chance through (\\d+) (days?)\\.\\.\\.(\\w+)\\.\\.\\.(\\d+) percent", RegexOption.IGNORE_CASE),
        Regex("\\*\\s*Formation chance through (\\d+) (hours?)\\.\\.\\.(\\w+)\\.\\.\\.(\\d+)%", RegexOption.IGNORE_CASE),
        Regex("\\*\\s*Formation chance through (\\d+) (days?)\\.\\.\\.(\\w+)\\.\\.\\.(\\d+)%", RegexOption.IGNORE_CASE),
        Regex("Formation chance through (\\d+) (hours?)\\.\\.\\.(\\w+)\\.\\.\\.(\\d+)%", RegexOption.IGNORE_CASE),
        Regex("Formation chance through (\\d+) (days?)\\.\\.\\.(\\w+)\\.\\.\\.(\\d+)%", RegexOption.IGNORE_CASE)
    )

    private val stripPatterns = listOf(
        Regex("\\*\\s*Formation chance through \\d+ (hours?|days?)\\.\\.\\.\\w+\\.\\.\\.\\d+ percent\\.?", RegexOption.IGNORE_CASE),
        Regex("\\*\\s*Formation chance through \\d+ (hours?|days?)\\.\\.\\.\\w+\\.\\.\\.\\d+%\\.?", RegexOption.IGNORE_CASE),
        Regex("Formation chance through \\d+ (hours?|days?)\\.\\.\\.\\w+\\.\\.\\.\\d+ percent\\.?", RegexOption.IGNORE_CASE),
        Regex("Formation chance through \\d+ (hours?|days?)\\.\\.\\.\\w+\\.\\.\\.\\d+%\\.?", RegexOption.IGNORE_CASE)
    )

    fun parse(text: String): List<FormationChance> {
        val formationChances = mutableListOf<FormationChance>()
        val seen = mutableSetOf<String>()

        patterns.forEach { pattern ->
            pattern.findAll(text).forEach { matchResult ->
                val timeframe = matchResult.groupValues[1]
                val unit = matchResult.groupValues[2]
                val level = matchResult.groupValues[3]
                val percentage = matchResult.groupValues[4]
                val key = "$timeframe|$unit|$level|$percentage"

                if (seen.add(key)) {
                    formationChances.add(
                        FormationChance(
                            timeframe = timeframe,
                            unit = unit,
                            level = level,
                            percentage = percentage
                        )
                    )
                }
            }
        }

        return formationChances
    }

    fun stripFromText(text: String): String {
        var cleanText = text
        stripPatterns.forEach { pattern ->
            cleanText = cleanText.replace(pattern, "")
        }
        cleanText = cleanText.replace(Regex("\\n\\s*\\n"), "\n\n")
        cleanText = cleanText.replace(Regex("\\s+"), " ")
        cleanText = cleanText.replace(Regex("\\.\\s*\\.+"), ".")
        cleanText = cleanText.replace(Regex("\\.\\s*$"), "")
        return cleanText.trim()
    }
}
