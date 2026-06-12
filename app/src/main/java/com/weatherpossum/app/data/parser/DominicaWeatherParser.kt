package com.weatherpossum.app.data.parser

import com.weatherpossum.app.data.model.DominicaParsedForecast
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * Parses the DMO daily forecast article. Extraction is label-driven so renamed
 * wrapper divs or column layouts are less likely to break the app.
 */
object DominicaWeatherParser {

    fun parse(html: String): DominicaParsedForecast {
        val doc = Jsoup.parse(html)
        return parseDocument(doc)
    }

    fun parseDocument(doc: Document): DominicaParsedForecast {
        val root = ForecastPageLocator.articleRoot(doc)
        val factsScope = ForecastPageLocator.factsScope(root)
        val outlookScope = ForecastPageLocator.outlookScope(root)

        val fields = mergeFields(
            factsScope.labeledFields(),
            root.labeledFields()
        )

        val shortTermForecast = DMOForecastParser.parseShortTerm(root, factsScope)

        val outlookTitle = ForecastPageLocator.outlookTitle(outlookScope)
            ?: outlookScope?.select("h2, h3, h4, h5")?.firstOrNull()?.text()?.norm()

        val outlookFields = outlookScope?.labeledFields().orEmpty()
        val outlookValidFrom = outlookFields["Valid from"] ?: fields["Valid from"]
        val outlookText = outlookScope?.outlookBodyText()

        return DominicaParsedForecast(
            validFrom = fields["Valid from"],
            synopsis = fields.pick("Synopsis"),
            shortTermForecast = shortTermForecast,
            wind = fields.pick("Wind"),
            seaConditions = fields.pick("Sea Conditions", "Sea State"),
            waves = fields.pick("Waves"),
            advisory = fields.pick("Warning/Advisory", "Warning", "Advisory")
                ?.takeIf { it.isActiveAdvisory() },
            sunrise = fields.pick("Sunrise"),
            sunset = fields.pick("Sunset"),
            lowTide = fields.pick("Low Tide"),
            highTide = fields.pick("High Tide"),
            outlookTitle = outlookTitle,
            outlookValidFrom = outlookValidFrom,
            outlookText = outlookText
        )
    }

    /** Prefer scoped fields; fill gaps from the wider article when wrappers move. */
    private fun mergeFields(
        primary: Map<String, String>,
        fallback: Map<String, String>
    ): Map<String, String> = buildMap {
        putAll(fallback)
        putAll(primary)
    }

    private fun Map<String, String>.pick(vararg labels: String): String? =
        labels.firstNotNullOfOrNull { label ->
            entries.firstOrNull { (key, _) -> key.equals(label, ignoreCase = true) }?.value
        }

    private fun Element.labeledFields(): Map<String, String> {
        val result = linkedMapOf<String, String>()
        select("p:has(strong), li:has(strong)").forEach { block ->
            val strongText = block.selectFirst("strong")?.text()?.norm() ?: return@forEach

            // e.g. <strong>Valid from: 12:00 PM on Wednesday...</strong>
            val inlineLabel = Regex("^([^:]+):\\s*(.+)$").find(strongText)
            if (inlineLabel != null) {
                val label = inlineLabel.groupValues[1].trim()
                val value = inlineLabel.groupValues[2].norm()
                if (value.isNotBlank()) result[label] = value
                return@forEach
            }

            // e.g. <strong>Synopsis</strong>: A high pressure system...
            val label = strongText.trimEnd(':')
            block.labeledValue(label)?.let { result[label] = it }
        }
        return result
    }

    private fun Element.labeledValue(label: String): String? {
        val whole = wholeText().replace('\u00A0', ' ')
        val pattern = Regex(
            "^\\s*${Regex.escape(label)}\\s*:?(.*)",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
        return pattern.find(whole)?.groupValues?.getOrNull(1)?.norm()?.takeIf { it.isNotBlank() }
    }

    private fun Element.outlookBodyText(): String? =
        select("p")
            .filterNot { paragraph ->
                val strong = paragraph.selectFirst("strong")?.text().orEmpty()
                strong.startsWith("Valid from", ignoreCase = true)
            }
            .mapNotNull { paragraph ->
                paragraph.text().norm().takeIf { it.isNotBlank() }
            }
            .joinToString("\n\n")
            .ifBlank { null }

    private fun String.norm(): String = replace('\u00A0', ' ')
        .replace(Regex("\\s+"), " ")
        .trim()

    private fun String.isActiveAdvisory(): Boolean {
        val normalized = lowercase()
        return normalized !in INACTIVE_ADVISORY_PHRASES &&
            !normalized.startsWith("none")
    }

    private val INACTIVE_ADVISORY_PHRASES = setOf(
        "none",
        "none at this time",
        "n/a",
        "nil",
        "no advisory",
        "no warning"
    )
}
