package com.weatherpossum.app.domain.hurricane

data class OutlookSection(
    val title: String,
    val content: String
)

object HurricaneOutlookSectionParser {
    private val headings = listOf(
        "Active Systems",
        "Central and Western Tropical Atlantic",
        "Central Tropical Atlantic",
        "Eastern Tropical Atlantic",
        "Western Tropical Atlantic",
        "Central Subtropical Atlantic",
        "Eastern Caribbean Sea",
        "Western Caribbean Sea",
        "Northwestern Caribbean Sea",
        "Southwestern Caribbean Sea",
        "Caribbean Sea",
        "Southwestern Gulf of America",
        "Northwestern Gulf of America",
        "Western Gulf of America",
        "Eastern Gulf of America",
        "Gulf of America",
        "Gulf of Mexico"
    )

    private val headingPattern = headings
        .sortedByDescending { it.length }
        .joinToString("|") { Regex.escape(it) }

    private val headingRegex = Regex(
        """($headingPattern)(?:\s+\(AL\d+\))?\s*:""",
        RegexOption.IGNORE_CASE
    )

    private val introRegex = Regex(
        """For the North Atlantic(?:\.\.\.|…)Caribbean Sea and the Gulf of (?:America|Mexico):""",
        RegexOption.IGNORE_CASE
    )

    private val footerRegex = Regex("""&&|\$\$""")

    fun parse(text: String): List<OutlookSection> {
        val body = text.replace(introRegex, "")
        val matches = headingRegex.findAll(body).toList()
        if (matches.isEmpty()) return emptyList()

        return matches.mapIndexedNotNull { index, match ->
            val title = match.value.trim().trimEnd(':').trim()
            val contentStart = match.range.last + 1
            val nextHeadingStart = matches.getOrNull(index + 1)?.range?.first
            val footerStart = footerRegex.find(body, contentStart)?.range?.first
            val contentEnd = listOfNotNull(nextHeadingStart, footerStart, body.length).min()
            val content = body.substring(contentStart, contentEnd).trim()
            if (content.isBlank()) null else OutlookSection(title = title, content = content)
        }
    }
}
