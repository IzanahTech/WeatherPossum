package com.weatherpossum.app.data.parser

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

object DMOForecastParser {

    /**
     * Parses the Dominica Met Office forecast section:
     * <div class="forecast_for_today">
     *   <p><strong>Forecast for Today and Tonight:</strong></p>
     *   <p>Body…</p>
     * </div>
     *
     * Tolerates variations in title wording, missing <strong>, &nbsp;, etc.
     */
    fun parse(html: String): DMOForecastResult {
        val doc = Jsoup.parse(html)

        val container = selectContainer(doc)
            ?: throw ParseException("DMO: forecast container not found")

        val paragraphs = container.select("p")

        // Title candidate: prefer first <strong>, else first <p> ownText, else container ownText
        val titleCandidate =
            container.selectFirst("p strong")?.text()
                ?: paragraphs.firstOrNull()?.ownText()
                ?: container.ownText()

        val titleRaw = normalizeSpaces(titleCandidate)
        val section = classifyTitle(titleRaw)

        // Body: join all <p> after the title paragraph; if empty, fallback to container text minus title
        val bodyParts = paragraphs
            .drop(if (paragraphs.isNotEmpty()) 1 else 0)
            .map { normalizeSpaces(it.text()) }
            .filter { it.isNotBlank() }

        val body = bodyParts.joinToString("\n\n").ifBlank {
            val full = normalizeSpaces(container.text())
            full.removePrefix(titleRaw).trim().ifBlank { full }
        }

        return DMOForecastResult(
            section = section,
            titleRaw = titleRaw.removeSuffix(":").trim(),
            body = body
        )
    }

    private fun selectContainer(doc: Document): Element? {
        return doc.selectFirst("div.forecast_for_today")
            ?: doc.selectFirst("section:has(div.forecast_for_today)")
            ?: doc.selectFirst("div:has(p:matches((?i)\\bforecast\\b))")
            ?: doc.selectFirst("div:matchesOwn((?i)\\bforecast\\b)")
    }

    private fun normalizeSpaces(s: String): String =
        s.replace('\u00A0', ' ')      // NBSP -> space
            .replace(Regex("\\s+"), " ")
            .trim()

    // -------------------- Title normalization & patterns --------------------

    // Connectors that join two time windows
    private const val CONNECT = "(?:\\s*(?:and|through|into|to|/|,|;|–|—)\\s*)"

    // Buckets for matching (broader vocabulary)
    private const val TODAY_WORD =
        "(?:today|this\\s+(?:morning|afternoon|day)|rest\\s+of\\s+today|remainder\\s+of\\s+today|rest\\s+of\\s+the\\s+day)"
    private const val TONIGHT_WORD =
        "(?:tonight|overnight|late\\s+tonight|this\\s+evening|late\\s+evening|rest\\s+of\\s+tonight|remainder\\s+of\\s+tonight)"
    private const val TOMORROW_WORD =
        "(?:tomorrow(?:\\s+(?:morning|afternoon|evening|night))?|next\\s*day)"

    // Precompiled combo regexes (explicit first to guarantee your six titles)
    private val RE_COMBO_MORNING_TONIGHT = Regex("\\bthis\\s*morning\\b$CONNECT\\b$TONIGHT_WORD\\b", RegexOption.IGNORE_CASE)
    private val RE_COMBO_AFTERNOON_TONIGHT = Regex("\\bthis\\s*afternoon\\b$CONNECT\\b$TONIGHT_WORD\\b", RegexOption.IGNORE_CASE)
    private val RE_COMBO_EVENING_TONIGHT = Regex("\\bthis\\s*evening\\b$CONNECT\\b$TONIGHT_WORD\\b", RegexOption.IGNORE_CASE)
    private val RE_COMBO_TODAY_TONIGHT_GENERIC = Regex("\\b$TODAY_WORD\\b$CONNECT\\b$TONIGHT_WORD\\b", RegexOption.IGNORE_CASE)
    private val RE_COMBO_TONIGHT_TOMORROW = Regex("\\b$TONIGHT_WORD\\b$CONNECT\\b$TOMORROW_WORD\\b", RegexOption.IGNORE_CASE)

    // Singles
    private val RE_TODAY_SINGLE = Regex("\\b$TODAY_WORD\\b", RegexOption.IGNORE_CASE)
    private val RE_TONIGHT_SINGLE = Regex("\\b$TONIGHT_WORD\\b", RegexOption.IGNORE_CASE)
    private val RE_TOMORROW_SINGLE = Regex("\\b$TOMORROW_WORD\\b", RegexOption.IGNORE_CASE)

    // Windows
    private val RE_24H = Regex("\\b(?:next|the\\s*next)\\s*24\\s*(?:hours|hrs?)\\b", RegexOption.IGNORE_CASE)
    private val RE_48H = Regex("\\b(?:next|the\\s*next)\\s*48\\s*(?:hours|hrs?)\\b", RegexOption.IGNORE_CASE)

    // Prefix like "Forecast for:" / "FORECAST –"
    private val RE_FORECAST_PREFIX = Regex("(?i)^\\s*forecast(?:s)?\\s*(?:for)?\\s*[:\\-–—]?\\s*")

    // Normalize punctuation & synonyms so classification is easier
    private fun canonicalizeTitle(s: String): String =
        normalizeSpaces(
            s.lowercase()
                .replace(RE_FORECAST_PREFIX, "") // drop "Forecast for:"
                .replace("&", " and ")           // unify &
                .replace(Regex("\\s+–\\s+|\\s+—\\s+|\\s+-\\s+"), " - ") // normalize dashes to " - "
                .replace(Regex("(?i)late\\s+night"), " late tonight")
                .replace(Regex("(?i)tonight\\s*/\\s*tomorrow"), "tonight and tomorrow")
                .replace(Regex(":+\\s*$"), "")   // trailing colon(s)
        )

    // -------------------- Classification --------------------

    private fun classifyTitle(raw: String): ForecastSection {
        val t = canonicalizeTitle(raw)

        // 1) Explicit combos to guarantee your listed titles
        when {
            RE_COMBO_MORNING_TONIGHT.containsMatchIn(t) -> return ForecastSection.TODAY_TONIGHT
            RE_COMBO_AFTERNOON_TONIGHT.containsMatchIn(t) -> return ForecastSection.TODAY_TONIGHT
            RE_COMBO_EVENING_TONIGHT.containsMatchIn(t) -> return ForecastSection.TODAY_TONIGHT
            RE_COMBO_TONIGHT_TOMORROW.containsMatchIn(t) -> return ForecastSection.TODAY_TONIGHT
            RE_COMBO_TODAY_TONIGHT_GENERIC.containsMatchIn(t) -> return ForecastSection.TODAY_TONIGHT
        }

        // 2) Multi-hour windows
        if (RE_24H.containsMatchIn(t) || RE_48H.containsMatchIn(t)) {
            return ForecastSection.TWENTY_FOUR_HOURS
        }

        // 3) Singles
        val hasTonight = RE_TONIGHT_SINGLE.containsMatchIn(t)
        val hasToday = RE_TODAY_SINGLE.containsMatchIn(t)
        val hasTomorrow = RE_TOMORROW_SINGLE.containsMatchIn(t)

        // If both today+tonight appear but missed by combo (odd punctuation), still map to TODAY_TONIGHT
        if (hasToday && hasTonight) return ForecastSection.TODAY_TONIGHT

        if (hasToday && !hasTonight) return ForecastSection.TODAY
        if (hasTonight && !hasTomorrow) return ForecastSection.TONIGHT
        if (hasTomorrow) return ForecastSection.TOMORROW

        // 4) Heuristics
        return when {
            Regex("\\bthis\\s+afternoon\\b", RegexOption.IGNORE_CASE).containsMatchIn(t) -> ForecastSection.TODAY
            Regex("\\bthis\\s+morning\\b", RegexOption.IGNORE_CASE).containsMatchIn(t) -> ForecastSection.TODAY
            Regex("\\bthis\\s+evening\\b", RegexOption.IGNORE_CASE).containsMatchIn(t) -> ForecastSection.TONIGHT
            Regex("\\btonight\\b", RegexOption.IGNORE_CASE).containsMatchIn(t) -> ForecastSection.TONIGHT
            Regex("\\btoday\\b", RegexOption.IGNORE_CASE).containsMatchIn(t) -> ForecastSection.TODAY
            Regex("\\btomorrow\\b", RegexOption.IGNORE_CASE).containsMatchIn(t) -> ForecastSection.TOMORROW
            else -> ForecastSection.UNKNOWN(raw)
        }
    }
}
