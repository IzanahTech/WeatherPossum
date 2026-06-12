package com.weatherpossum.app.data.parser

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * Locates forecast page regions using a fallback chain.
 *
 * DMO markup has changed before (column classes, outlook wrapper names). We prefer
 * stable anchors — schema.org article body, bold field labels, heading text — and
 * only use CSS classes as hints, not requirements.
 */
internal object ForecastPageLocator {

  private val OUTLOOK_HEADING = Regex("(?i)weather\\s+outlook|lesser\\s+antilles")
  private val FORECAST_TITLE = Regex("(?i)\\bforecast\\s+for\\b")

  fun articleRoot(doc: Document): Element =
    doc.selectFirst("div[itemprop=articleBody]")
      ?: doc.selectFirst("article[itemtype*=Article]")
      ?: doc.selectFirst("article")
      ?: doc.body()

  /**
   * Scope for labeled fact paragraphs (synopsis, wind, tides, etc.).
   * Falls back to the full article when column wrappers are absent or renamed.
   */
  fun factsScope(root: Element): Element {
    root.selectFirst("div.col-sm-6:not(.outlook_da_la)")?.let { return it }
    root.selectFirst("div[class*=outlook]")?.previousElementSibling()?.let { return it }

    val synopsisParagraph = root.findParagraphWithLabel("Synopsis")
    synopsisParagraph?.parents()?.firstOrNull { parent ->
      parent != root && parent.select("p:has(strong)").size >= 3
    }?.let { return it }

    return root
  }

  /**
   * Scope for the regional outlook narrative (right column on the live site).
   */
  fun outlookScope(root: Element): Element? {
    root.selectFirst("div.outlook_da_la")?.let { return it }
    root.selectFirst("div[class*=outlook]")?.let { return it }

    root.select("h2, h3, h4, h5").firstOrNull { heading ->
      OUTLOOK_HEADING.containsMatchIn(heading.text())
    }?.let { heading ->
      heading.parent()?.takeIf { it.select("p").isNotEmpty() }?.let { return it }
      return heading
    }

    // Second block of narrative paragraphs after the facts column, if layout splits cleanly.
    val factParagraphs = factsScope(root).select("p:has(strong)")
    val outlookCandidate = root.select("p").firstOrNull { paragraph ->
      paragraph != factParagraphs.lastOrNull() &&
        !paragraph.hasStrongLabel() &&
        paragraph.text().length > 80 &&
        paragraph.elementSiblingIndex() > (factParagraphs.lastOrNull()?.elementSiblingIndex() ?: -1)
    }
    outlookCandidate?.parent()?.let { parent ->
      if (parent.select("p").size >= 2) return parent
    }

    return null
  }

  fun outlookTitle(scope: Element?): String? {
    if (scope == null) return null
    val heading = scope.select("h2, h3, h4, h5").firstOrNull { h ->
      OUTLOOK_HEADING.containsMatchIn(h.text())
    }
    heading?.text()?.norm()?.takeIf { it.isNotBlank() }?.let { return it }

  // Heading may sit just outside the scoped wrapper after a redesign.
    val siblingHeading = scope.previousElementSibling()
      ?.takeIf { it.tagName().matches(Regex("h[2-5]")) }
      ?.text()?.norm()
    if (!siblingHeading.isNullOrBlank() && OUTLOOK_HEADING.containsMatchIn(siblingHeading)) {
      return siblingHeading
    }

    return null
  }

  fun forecastContainer(root: Element, factsScope: Element): Element? {
    factsScope.selectFirst("div.forecast_for_today")?.let { return it }
    root.selectFirst("div.forecast_for_today")?.let { return it }
    return root.select("div[class*=forecast]").firstOrNull { div ->
      div.select("p:has(strong)").any { FORECAST_TITLE.containsMatchIn(it.text()) }
    }
  }

  private fun Element.findParagraphWithLabel(label: String): Element? =
    select("p:has(strong)").firstOrNull { paragraph ->
      paragraph.strongLabel()?.equals(label, ignoreCase = true) == true ||
        paragraph.strongLabel()?.startsWith("$label:", ignoreCase = true) == true
    }

  private fun Element.hasStrongLabel(): Boolean = selectFirst("strong") != null

  private fun Element.strongLabel(): String? =
    selectFirst("strong")?.text()?.norm()?.substringBefore(":")?.trim()

  private fun String.norm(): String = replace('\u00A0', ' ')
    .replace(Regex("\\s+"), " ")
    .trim()
}
