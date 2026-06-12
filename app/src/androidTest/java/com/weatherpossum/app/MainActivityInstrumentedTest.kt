package com.weatherpossum.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.weatherpossum.app.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launchesAndShowsNowTab() {
        composeRule.waitForIdle()
        val nowLabel = composeRule.activity.getString(R.string.nav_tab_now)
        composeRule.onNodeWithText(nowLabel).assertExists()
    }

    @Test
    fun canSwitchToExtrasTab() {
        composeRule.waitForIdle()
        val extrasLabel = composeRule.activity.getString(R.string.nav_tab_extras)
        composeRule.onNodeWithText(extrasLabel).performClick()
        composeRule.onNodeWithText(extrasLabel).assertExists()
    }

    @Test
    fun funFactsArrayHasEntries() {
        val facts = composeRule.activity.resources.getStringArray(R.array.fun_facts)
        assertEquals(30, facts.size)
    }
}
