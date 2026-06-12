package com.weatherpossum.app.data.parser

import com.weatherpossum.app.data.model.DMOForecastResult
import com.weatherpossum.app.data.model.ForecastSection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class ParseException(msg: String) : RuntimeException(msg)

object DMOForecastParser {

    private val FORECAST_TITLE = Regex("(?i)\\bforecast\\s+for\\b")
    private val FIELD_LABELS = setOf(
        "valid from", "synopsis", "wind", "sea conditions", "sea state", "waves",
        "warning/advisory", "warning", "advisory", "sunrise", "sunset",
        "low tide", "high tide"
    )

    /**
     * Parses the Dominica Met Office forecast section:
     * <div class="forecast_for_today">
     *   <p><strong>Forecast for Today and Tonight:</strong></p>
     *   <p>Body…</p>
     * </div>
     */
    fun parse(html: String): DMOForecastResult {
        val doc = Jsoup.parse(html)
        val container = selectContainer(doc)
            ?: throw ParseException("DMO: forecast container not found")
        return parseFromElement(container)
    }

    /**
     * Fallback when the forecast title and body are bare paragraphs (no wrapper div).
     */
    fun parseLoose(root: Element): DMOForecastResult? {
        val titleParagraph = root.select("p:has(strong)").firstOrNull { paragraph ->
            FORECAST_TITLE.containsMatchIn(paragraph.text())
        } ?: return null

        val titleCandidate = titleParagraph.selectFirst("strong")?.text()
            ?: titleParagraph.ownText()
        val titleRaw = normalizeSpaces(titleCandidate)
        if (titleRaw.isBlank()) return null

        val bodyParts = mutableListOf<String>()
        var sibling = titleParagraph.nextElementSibling()
        while (sibling != null && sibling.tagName() == "p") {
            val strongText = sibling.selectFirst("strong")?.text()?.let(::normalizeSpaces)
            if (strongText != null) {
                val label = strongText.substringBefore(":").trim().lowercase()
                if (label in FIELD_LABELS) break
                if (FORECAST_TITLE.containsMatchIn(strongText)) break
            }
            normalizeSpaces(sibling.text()).takeIf { it.isNotBlank() }?.let(bodyParts::add)
            sibling = sibling.nextElementSibling()
        }

        val body = bodyParts.joinToString("\n\n")
        if (body.isBlank()) return null

        return DMOForecastResult(
            section = classifyTitle(titleRaw),
            titleRaw = titleRaw.removeSuffix(":").trim(),
            body = body
        )
    }

    fun parseShortTerm(root: Element, factsScope: Element): DMOForecastResult? {
        ForecastPageLocator.forecastContainer(root, factsScope)
            ?.let { container ->
                runCatching { parseFromElement(container) }.getOrNull()
                    ?.takeIf { result ->
                        result.body.isNotBlank() &&
                            FORECAST_TITLE.containsMatchIn(result.titleRaw)
                    }
                    ?.let { return it }
            }
        return parseLoose(root)
    }

    fun parseFromElement(container: Element): DMOForecastResult {
        val paragraphs = container.select("p")

        val titleCandidate =
            container.selectFirst("p strong")?.text()
                ?: paragraphs.firstOrNull()?.ownText()
                ?: container.ownText()

        val titleRaw = normalizeSpaces(titleCandidate)
        val section = classifyTitle(titleRaw)

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

    fun selectContainer(doc: Document): Element? {
        val root = ForecastPageLocator.articleRoot(doc)
        val factsScope = ForecastPageLocator.factsScope(root)
        return ForecastPageLocator.forecastContainer(root, factsScope)
    }

    internal fun normalizeSpaces(s: String): String =
        s.replace('\u00A0', ' ')
            .replace(Regex("\\s+"), " ")
            .trim()

    private const val CONNECT = "(?:\\s*(?:and|through|into|to|/|,|;|–|—)\\s*)"

    private const val TODAY_WORD =
        "(?:today|this\\s+(?:morning|afternoon|day)|rest\\s+of\\s+today|remainder\\s+of\\s+today|rest\\s+of\\s+the\\s+day)"
    private const val TONIGHT_WORD =
        "(?:tonight|overnight|late\\s+tonight|this\\s+evening|late\\s+evening|rest\\s+of\\s+tonight|remainder\\s+of\\s+tonight)"
    private const val TOMORROW_WORD =
        "(?:tomorrow(?:\\s+(?:morning|afternoon|evening|night))?|next\\s*day)"

    private val RE_COMBO_MORNING_TONIGHT = Regex("\\bthis\\s*morning\\b$CONNECT\\b$TONIGHT_WORD\\b", RegexOption.IGNORE_CASE)
    private val RE_COMBO_AFTERNOON_TONIGHT = Regex("\\bthis\\s*afternoon\\b$CONNECT\\b$TONIGHT_WORD\\b", RegexOption.IGNORE_CASE)
    private val RE_COMBO_EVENING_TONIGHT = Regex("\\bthis\\s*evening\\b$CONNECT\\b$TONIGHT_WORD\\b", RegexOption.IGNORE_CASE)
    private val RE_COMBO_TODAY_TONIGHT_GENERIC = Regex("\\b$TODAY_WORD\\b$CONNECT\\b$TONIGHT_WORD\\b", RegexOption.IGNORE_CASE)
    private val RE_COMBO_TONIGHT_TOMORROW = Regex("\\b$TONIGHT_WORD\\b$CONNECT\\b$TOMORROW_WORD\\b", RegexOption.IGNORE_CASE)

    private val RE_TODAY_SINGLE = Regex("\\b$TODAY_WORD\\b", RegexOption.IGNORE_CASE)
    private val RE_TONIGHT_SINGLE = Regex("\\b$TONIGHT_WORD\\b", RegexOption.IGNORE_CASE)
    private val RE_TOMORROW_SINGLE = Regex("\\b$TOMORROW_WORD\\b", RegexOption.IGNORE_CASE)

    private val RE_24H = Regex("\\b(?:next|the\\s*next)\\s*24\\s*(?:hours|hrs?)\\b", RegexOption.IGNORE_CASE)
    private val RE_48H = Regex("\\b(?:next|the\\s*next)\\s*48\\s*(?:hours|hrs?)\\b", RegexOption.IGNORE_CASE)

    private val RE_FORECAST_PREFIX = Regex("(?i)^\\s*forecast(?:s)?\\s*(?:for)?\\s*[:\\-–—]?\\s*")

    private fun canonicalizeTitle(s: String): String =
        normalizeSpaces(
            s.lowercase()
                .replace(RE_FORECAST_PREFIX, "")
                .replace("&", " and ")
                .replace(Regex("\\s+–\\s+|\\s+—\\s+|\\s+-\\s+"), " - ")
                .replace(Regex("(?i)late\\s+night"), " late tonight")
                .replace(Regex("(?i)tonight\\s*/\\s*tomorrow"), "tonight and tomorrow")
                .replace(Regex(":+\\s*$"), "")
        )

    internal fun classifyTitle(raw: String): ForecastSection {
        val t = canonicalizeTitle(raw)

        when {
            RE_COMBO_MORNING_TONIGHT.containsMatchIn(t) -> return ForecastSection.TODAY_TONIGHT
            RE_COMBO_AFTERNOON_TONIGHT.containsMatchIn(t) -> return ForecastSection.TODAY_TONIGHT
            RE_COMBO_EVENING_TONIGHT.containsMatchIn(t) -> return ForecastSection.TODAY_TONIGHT
            RE_COMBO_TONIGHT_TOMORROW.containsMatchIn(t) -> return ForecastSection.TODAY_TONIGHT
            RE_COMBO_TODAY_TONIGHT_GENERIC.containsMatchIn(t) -> return ForecastSection.TODAY_TONIGHT
        }

        if (RE_24H.containsMatchIn(t) || RE_48H.containsMatchIn(t)) {
            return ForecastSection.TWENTY_FOUR_HOURS
        }

        val hasTonight = RE_TONIGHT_SINGLE.containsMatchIn(t)
        val hasToday = RE_TODAY_SINGLE.containsMatchIn(t)
        val hasTomorrow = RE_TOMORROW_SINGLE.containsMatchIn(t)

        if (hasToday && hasTonight) return ForecastSection.TODAY_TONIGHT

        if (hasToday && !hasTonight) return ForecastSection.TODAY
        if (hasTonight && !hasTomorrow) return ForecastSection.TONIGHT
        if (hasTomorrow) return ForecastSection.TOMORROW

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
