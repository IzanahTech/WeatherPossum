package com.weatherpossum.app.data.parser

import com.weatherpossum.app.data.model.DominicaParsedForecast
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

object DominicaWeatherParser {
    
    fun parseDominicaArticleBody(html: String): DominicaParsedForecast {
        val doc: Document = Jsoup.parse(html)
        val root: Element = doc.selectFirst("div[itemprop=articleBody]") ?: doc.body()

        // Left column (facts & daily forecasts)
        val leftCol = root.selectFirst("div.col-sm-6:not(.outlook_da_la)") ?: root

        // Helper: normalize whitespace (handles <br>, newlines, &nbsp;)
        fun String.norm(): String = replace('\u00A0', ' ')
            .replace(Regex("\\s+"), " ")
            .trim()

        // Helper: extract value from a <p><strong>Label</strong>: value ...</p>,
        // even if there are line breaks (e.g., tides) -> use ownText + text nodes
        fun getValueFor(label: String): String? {
            // find <p> where its first <strong> matches the label (case-insensitive)
            val p = leftCol.select("p:has(strong)").firstOrNull { pEl ->
                val t = pEl.selectFirst("strong")?.text()?.norm() ?: ""
                t.equals(label, ignoreCase = true) || t.equals("$label:", ignoreCase = true)
            } ?: return null
            // Everything in that <p> except the <strong> label is the value.
            // Use wholeText to preserve " ... and ..." from line breaks.
            val whole = p.wholeText().replace('\u00A0', ' ')
            // Build a regex like "^\\s*(Valid from|Synopsis)\\s*:?(.*)$" across lines
            val rx = Regex("^\\s*${Regex.escape(label)}\\s*:?(.*)$", setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
            val m = rx.find(whole)
            return m?.groupValues?.getOrNull(1)?.norm()?.takeIf { it.isNotBlank() }
        }

        // Forecast sub-block (Tonight/Tomorrow) – the text is in the next <p> after each label <p>
        val fBlock = leftCol.selectFirst("div.forecast_for_today")
        
        // More flexible forecast extraction using pattern matching
        fun getForecastByPattern(patterns: List<String>): Pair<String?, String?>? {
            val labelP = fBlock?.select("p:has(strong)")?.firstOrNull { pEl ->
                val t = pEl.selectFirst("strong")?.text()?.norm() ?: ""
                patterns.any { pattern -> t.contains(pattern, ignoreCase = true) }
            } ?: return null
            // Get the actual title from the strong tag
            val actualTitle = labelP.selectFirst("strong")?.text()?.norm()
            // the very next <p> sibling is the narrative
            val next = labelP.nextElementSibling()
            val content = next?.takeIf { it.tagName() == "p" }?.text()?.norm()?.takeIf { it.isNotBlank() }
            return Pair(actualTitle, content)
        }

        val validFrom = getValueFor("Valid from")
        val synopsis = getValueFor("Synopsis")
        
        // Use flexible pattern matching for forecasts
        val forecastTonightData = getForecastByPattern(listOf(
            "Tonight", 
            "Today and Tonight", 
            "This Evening", 
            "This Afternoon",
            "Afternoon and Tonight",
            "This Afternoon and Tonight",
            "Tonight and Tomorrow"
        ))
        val forecastTomorrowData = getForecastByPattern(listOf("Tomorrow", "Next Day", "Following Day"))
        
        val forecastTonight = forecastTonightData?.second
        val forecastTonightTitle = forecastTonightData?.first
        val forecastTomorrow = forecastTomorrowData?.second
        val forecastTomorrowTitle = forecastTomorrowData?.first
        val wind = getValueFor("Wind")
        val sea = getValueFor("Sea Conditions") ?: getValueFor("Sea State")
        val waves = getValueFor("Waves")
        val advisoryRaw = getValueFor("Warning/Advisory")
        // Don't capture weather outlook content as advisory
        val advisory = if (advisoryRaw?.contains("tropical wave", ignoreCase = true) == true ||
                          advisoryRaw?.contains("unstable conditions", ignoreCase = true) == true ||
                          advisoryRaw?.contains("seas are also expected", ignoreCase = true) == true) {
            null // This is weather outlook content, not advisory
        } else {
            advisoryRaw
        }
        val sunrise = getValueFor("Sunrise")
        val sunset  = getValueFor("Sunset")
        val lowTide = getValueFor("Low Tide")    // will include "… and …"
        val highTide= getValueFor("High Tide")   // will include "… and …"

        // Right column: Outlook
        val outlookCol = root.selectFirst("div.outlook_da_la.col-sm-6")
        val outlookTitle: String? = null // Explicitly set to null to use fallback in WeatherRepository
        val outlookValidFrom = outlookCol?.select("p:has(strong)")?.firstOrNull { pEl ->
            pEl.selectFirst("strong")?.text()?.startsWith("Valid from", ignoreCase = true) == true
        }?.let { p ->
            val whole = p.wholeText().replace('\u00A0', ' ')
            Regex("^\\s*Valid from\\s*:?(.*)$", setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
                .find(whole)?.groupValues?.getOrNull(1)?.norm()
        }

        // join all remaining <p> in the outlook (excluding the Valid from line)
        val outlookText = outlookCol?.select("p") // Get all <p> tags in the outlook column
            ?.filterNot { it.selectFirst("strong")?.text()?.startsWith("Valid from", true) == true }
            ?.mapNotNull { p -> 
                val text = p.text().norm()
                if (text.isBlank()) null else text
            }
            ?.joinToString("\n\n")
            ?.ifBlank { null }

        return DominicaParsedForecast(
            validFrom = validFrom,
            synopsis = synopsis,
            forecastTonight = forecastTonight,
            forecastTonightTitle = forecastTonightTitle,
            forecastTomorrow = forecastTomorrow,
            forecastTomorrowTitle = forecastTomorrowTitle,
            wind = wind,
            seaConditions = sea,
            waves = waves,
            advisory = advisory,
            sunrise = sunrise,
            sunset = sunset,
            lowTide = lowTide,
            highTide = highTide,
            outlookTitle = outlookTitle,
            outlookValidFrom = outlookValidFrom,
            outlookText = outlookText
        )
    }
}
