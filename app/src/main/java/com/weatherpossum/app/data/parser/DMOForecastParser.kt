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
            // fallback: full container text minus title
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

    private fun canonicalizeTitle(s: String): String =
        normalizeSpaces(
            s.lowercase()
             .replace("&", "and")
             .replace(Regex("\\bforecast\\b"), "")
             .replace(Regex("\\bfor\\b"), "")
             .replace(Regex(":+\\s*$"), "")
        )

    private fun classifyTitle(raw: String): ForecastSection {
        val t = canonicalizeTitle(raw)
        return when {
            Regex("(?i)\\btoday\\b.*\\btonight\\b").containsMatchIn(t) -> ForecastSection.TODAY_TONIGHT
            Regex("(?i)\\bthis\\s*evening\\b.*\\btonight\\b").containsMatchIn(t) -> ForecastSection.TODAY_TONIGHT
            Regex("(?i)\\btoday\\b(?!.*tonight)").containsMatchIn(t) -> ForecastSection.TODAY
            Regex("(?i)\\b(tonight|overnight)\\b").containsMatchIn(t) -> ForecastSection.TONIGHT
            Regex("(?i)\\b(tomorrow|next\\s*day)\\b").containsMatchIn(t) -> ForecastSection.TOMORROW
            Regex("(?i)\\b(next\\s*24\\s*hours|the\\s*next\\s*24\\s*hours|24\\s*hrs?)\\b").containsMatchIn(t) ->
                ForecastSection.TWENTY_FOUR_HOURS
            else -> {
                // Heuristic nudge: infer TODAY/TONIGHT when UNKNOWN but text clearly includes those words
                when {
                    Regex("(?i)\\btonight\\b").containsMatchIn(t) -> ForecastSection.TONIGHT
                    Regex("(?i)\\btoday\\b").containsMatchIn(t) -> ForecastSection.TODAY
                    else -> ForecastSection.UNKNOWN(raw)
                }
            }
        }
    }
}
