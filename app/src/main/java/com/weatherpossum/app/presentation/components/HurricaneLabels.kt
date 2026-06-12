package com.weatherpossum.app.presentation.components

import android.content.Context
import com.weatherpossum.app.R
import com.weatherpossum.app.data.model.Hurricane

object HurricaneLabels {
    fun stormStatus(context: Context, classification: String?, category: Int): String = when (classification) {
        "HU" -> context.getString(R.string.hurricane_status_hurricane, category)
        "TS" -> context.getString(R.string.hurricane_status_tropical_storm)
        "TD" -> context.getString(R.string.hurricane_status_tropical_depression)
        "PTC" -> context.getString(R.string.hurricane_status_post_tropical)
        null, "" -> context.getString(R.string.hurricane_status_unknown)
        else -> classification
    }

    fun location(context: Context, coordinates: String): String =
        coordinates.ifBlank { context.getString(R.string.hurricane_location_unavailable) }

    fun stormName(context: Context, name: String): String =
        name.ifBlank { context.getString(R.string.hurricane_unnamed_storm) }

    fun categoryDescription(context: Context, category: Int): String = when (category) {
        1 -> context.getString(R.string.hurricane_category_1)
        2 -> context.getString(R.string.hurricane_category_2)
        3 -> context.getString(R.string.hurricane_category_3)
        4 -> context.getString(R.string.hurricane_category_4)
        5 -> context.getString(R.string.hurricane_category_5)
        else -> context.getString(R.string.hurricane_status_tropical_storm)
    }

    fun formatStorm(context: Context, storm: Hurricane): String {
        val name = stormName(context, storm.name)
        val status = stormStatus(context, storm.classification, storm.category)
        val coords = location(context, storm.location)
        return context.getString(R.string.hurricane_storm_summary, name, status, coords)
    }

}
