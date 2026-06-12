package com.weatherpossum.app.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.weatherpossum.app.R
import com.weatherpossum.app.data.model.MoonData

@Composable
fun formatMoonPhaseLabel(phase: String): String = when (phase.uppercase()) {
    "NEW_MOON" -> stringResource(R.string.moon_phase_new)
    "WAXING_CRESCENT" -> stringResource(R.string.moon_phase_waxing_crescent)
    "FIRST_QUARTER" -> stringResource(R.string.moon_phase_first_quarter)
    "WAXING_GIBBOUS" -> stringResource(R.string.moon_phase_waxing_gibbous)
    "FULL_MOON" -> stringResource(R.string.moon_phase_full)
    "WANING_GIBBOUS" -> stringResource(R.string.moon_phase_waning_gibbous)
    "LAST_QUARTER" -> stringResource(R.string.moon_phase_last_quarter)
    "WANING_CRESCENT" -> stringResource(R.string.moon_phase_waning_crescent)
    else -> MoonData.formatMoonPhase(phase)
}
