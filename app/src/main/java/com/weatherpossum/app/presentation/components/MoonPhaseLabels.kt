package com.weatherpossum.app.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.weatherpossum.app.R
import com.weatherpossum.app.data.model.MoonData

@Composable
fun formatMoonPhaseLabel(phase: String): String = when (phase.uppercase()) {
    "NEW_MOON" -> stringResource(R.string.moon_phase_new)
    "FIRST_QUARTER" -> stringResource(R.string.moon_phase_first_quarter)
    "FULL_MOON" -> stringResource(R.string.moon_phase_full)
    "LAST_QUARTER" -> stringResource(R.string.moon_phase_last_quarter)
    else -> MoonData.formatMoonPhase(phase)
}
