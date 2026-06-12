package com.weatherpossum.app.data.parser

import com.weatherpossum.app.data.model.ForecastDay
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * Parses the DMO extended (7-day) forecast page:
 * [Extended forecast page](https://weather.gov.dm/forecast/extended-forecast)
 *
 * Day blocks live in `#ext_forecast` as `div.third` cards with labeled fields in `div.entry`.
 */
object ExtendedForecastParser {

    fun parse(html: String): List<ForecastDay> = parseDocument(Jsoup.parse(html))

    fun parseDocument(doc: Document): List<ForecastDay> {
        val container = locateForecastContainer(doc) ?: return emptyList()
        return locateDayBlocks(container).mapNotNull { block -> parseDayBlock(block) }
    }

    private fun locateForecastContainer(doc: Document): Element? {
        doc.selectFirst("div#ext_forecast")?.let { return it }
        doc.selectFirst("div[id*=ext_forecast]")?.let { return it }
        return doc.selectFirst("div[itemprop=articleBody]")
            ?.select("div")
            ?.firstOrNull { div ->
                div.select("div.entry p:has(strong)").any { paragraph ->
                    paragraph.text().contains("Max Temp", ignoreCase = true)
                }
            }
    }

    private fun locateDayBlocks(container: Element): List<Element> {
        container.select("div.third").takeIf { it.isNotEmpty() }?.let { return it }
        container.select("div[class*=third]").takeIf { it.isNotEmpty() }?.let { return it }
        return container.children().filter { child ->
            child.selectFirst("div.entry") != null
        }
    }

    private fun parseDayBlock(block: Element): ForecastDay? {
        val entry = block.selectFirst("div.entry") ?: return null
        val fields = entry.labeledFields()

        val weather = fields.pick("Weather")
            ?: entry.selectFirst("img[alt]")?.attr("alt")?.norm()
            ?: entry.selectFirst("img[title]")?.attr("title")?.norm()

        val maxTemp = fields.pick("Max Temp.", "Max Temp")
        val minTemp = fields.pick("Min Temp.", "Min Temp")
        val wind = fields.pick("Wind")
        val seas = fields.pick("Seas", "Sea Conditions")
        val waves = fields.pick("Waves")

        if (listOf(weather, maxTemp, minTemp, wind, seas, waves).all { it.isNullOrBlank() }) {
            return null
        }

        return ForecastDay(
            date = parseDayHeading(block.selectFirst("h3")),
            maxTemp = maxTemp.orEmpty(),
            minTemp = minTemp.orEmpty(),
            weather = weather.orEmpty(),
            wind = wind.orEmpty(),
            seas = seas.orEmpty(),
            waves = waves.orEmpty()
        )
    }

    private fun parseDayHeading(heading: Element?): String {
        if (heading == null) return ""

        val monthDay = heading.selectFirst("strong")?.text()?.norm()
        val dayName = heading.wholeText()
            .replace(monthDay.orEmpty(), "")
            .norm()
            .trim()
            .trimEnd(',')

        return when {
            dayName.isNotBlank() && !monthDay.isNullOrBlank() -> "$dayName, $monthDay"
            !monthDay.isNullOrBlank() -> monthDay
            else -> dayName
        }
    }

    private fun Element.labeledFields(): Map<String, String> {
        val result = linkedMapOf<String, String>()
        select("p:has(strong), li:has(strong)").forEach { block ->
            val strongText = block.selectFirst("strong")?.text()?.norm() ?: return@forEach

            val inlineLabel = Regex("^([^:]+):\\s*(.+)$").find(strongText)
            if (inlineLabel != null) {
                val label = inlineLabel.groupValues[1].trim()
                val value = inlineLabel.groupValues[2].norm()
                if (value.isNotBlank()) result[label] = value
                return@forEach
            }

            val label = strongText.trimEnd('.').trimEnd(':')
            block.labeledValue(label)?.let { result[label] = it }
            if (label.endsWith('.')) {
                block.labeledValue(label.trimEnd('.'))?.let { result[label.trimEnd('.')] = it }
            }
        }
        return result
    }

    private fun Element.labeledValue(label: String): String? {
        val whole = wholeText().replace('\u00A0', ' ')
        val pattern = Regex(
            "^\\s*${Regex.escape(label)}\\.?\\s*:?(.*)",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
        return pattern.find(whole)?.groupValues?.getOrNull(1)?.norm()?.takeIf { it.isNotBlank() }
    }

    private fun Map<String, String>.pick(vararg labels: String): String? =
        labels.firstNotNullOfOrNull { label ->
            entries.firstOrNull { (key, _) ->
                key.equals(label, ignoreCase = true) ||
                    key.equals("${label.trimEnd('.')}.", ignoreCase = true)
            }?.value
        }

    private fun String.norm(): String = replace('\u00A0', ' ')
        .replace(Regex("\\s+"), " ")
        .trim()
}
