package com.weatherpossum.app.widget

data class WidgetSnapshot(
    val userName: String?,
    val synopsis: String?,
    val windSummary: String? = null,
    val seaSummary: String? = null,
    val tideSummary: String? = null,
    val hasCoastalDetails: Boolean = false
)
