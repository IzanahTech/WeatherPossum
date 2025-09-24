package com.weatherpossum.app.data.parser

import org.jsoup.Jsoup

object TwoTextParser {

    data class TwoResult(
        val issued: String?,                 // "800 AM EDT Tue Sep 23 2025"
        val forecaster: String?,             // "Forecaster Blake"
        val items: List<String>,             // numbered disturbances or Active Systems lines
        val cleaned: String                  // final pretty string for the card
    )

    fun parse(rawHtml: String): TwoResult {
        // 1) Convert HTML -> text
        val doc = Jsoup.parse(rawHtml)
        val text = doc.body().text().replace("\u00A0", " ").trim()

        // 2) Extract forecaster BEFORE cutting off "&&" section (forecaster comes after &&)
        val forecaster = Regex("Forecaster\\s+[A-Za-z\\-]+").find(text)?.value

        // 3) Cut off everything from "&&" onwards
        val cleanText = text.split("&&").firstOrNull()?.trim() ?: text

        // 4) Extract issuance time (best-effort)
        val issued = Regex("\\b\\d{1,2}:?\\d{0,2}\\s*(AM|PM)\\s*EDT\\s*\\w+\\s*\\w+\\s*\\d{1,2}\\s*\\d{4}")
            .find(cleanText)?.value

        // 5) Split numbered items (1. 2. …) OR "Active Systems:" lines
        val items = mutableListOf<String>()
        val numbered = Regex("(?=\\b\\d+\\.)").split(cleanText).map { it.trim() }.filter { it.matches(Regex("^\\d+\\..+")) }
        if (numbered.isNotEmpty()) items += numbered
        
        // 6) Parse specific sections for better separation
        val activeSystems = Regex("Active Systems:(.*?)(?=Central and Western Tropical Atlantic|Eastern Caribbean Sea|Eastern Tropical Atlantic|\\$\\$|$)", RegexOption.DOT_MATCHES_ALL)
            .find(cleanText)?.groupValues?.get(1)?.trim()
        if (!activeSystems.isNullOrBlank()) {
            items.add("Active Systems: $activeSystems")
        }
        
        val centralWestern = Regex("Central and Western Tropical Atlantic \\(AL93\\):(.*?)(?=Eastern Caribbean Sea|Eastern Tropical Atlantic|\\$\\$|$)", RegexOption.DOT_MATCHES_ALL)
            .find(cleanText)?.groupValues?.get(1)?.trim()
        if (!centralWestern.isNullOrBlank()) {
            items.add("Central and Western Tropical Atlantic (AL93): $centralWestern")
        }
        
        val easternCaribbean = Regex("Eastern Caribbean Sea \\(AL94\\):(.*?)(?=Eastern Tropical Atlantic|\\$\\$|$)", RegexOption.DOT_MATCHES_ALL)
            .find(cleanText)?.groupValues?.get(1)?.trim()
        if (!easternCaribbean.isNullOrBlank()) {
            items.add("Eastern Caribbean Sea (AL94): $easternCaribbean")
        }
        
        val easternTropical = Regex("Eastern Tropical Atlantic:(.*?)(?=\\$\\$|$)", RegexOption.DOT_MATCHES_ALL)
            .find(cleanText)?.groupValues?.get(1)?.trim()
        if (!easternTropical.isNullOrBlank()) {
            items.add("Eastern Tropical Atlantic: $easternTropical")
        }

        // 7) Build cleaned string for the card (exclude forecaster since it's shown in separate container)
        val header = issued?.let { "Issued: $it" } ?: "Atlantic Tropical Weather Outlook"
        val body = if (items.isNotEmpty()) items.joinToString("\n\n") else cleanText
        val cleaned = listOf(header, body).filter { it.isNotBlank() }.joinToString("\n\n")

        return TwoResult(issued = issued, forecaster = forecaster, items = items, cleaned = cleaned)
    }
}
