package com.weatherpossum.app.data.model

data class HurricaneData(
    val activeStorms: List<Hurricane>,
    val tropicalOutlook: String? = null,
    val lastUpdated: Long,
    val isFromCache: Boolean = false
)

data class Hurricane(
    val id: String,
    val name: String,
    val category: Int,
    val status: String,
    val location: String,
    val windSpeed: Int,
    val pressure: Int,
    val lastUpdated: String
) {
    val categoryDescription: String
        get() = when (category) {
            1 -> "Category 1 (74-95 mph)"
            2 -> "Category 2 (96-110 mph)"
            3 -> "Category 3 (111-129 mph)"
            4 -> "Category 4 (130-156 mph)"
            5 -> "Category 5 (157+ mph)"
            else -> "Tropical Storm"
        }
    
    val displayName: String
        get() = if (name.isNotBlank()) name else "Unnamed Storm"
}
