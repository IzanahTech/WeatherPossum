package com.weatherpossum.app.data.repository

import com.weatherpossum.app.data.model.CurrentStormsDto
import com.weatherpossum.app.data.model.StormDto
import com.squareup.moshi.Moshi
import org.junit.Assert.*
import org.junit.Test
import java.nio.charset.StandardCharsets

class CurrentStormsParserTest {

    private val moshi = Moshi.Builder().build()
    private val stormsAdapter = moshi.adapter(CurrentStormsDto::class.java)

    private fun loadFixture(name: String): String {
        val url = javaClass.classLoader!!.getResource("fixtures/nhc/$name")
            ?: error("Fixture not found: $name")
        return url.readText(StandardCharsets.UTF_8)
    }

    @Test fun parseCurrentStorms_sample_json() {
        val json = loadFixture("CurrentStorms.sample.json")
        val dto = stormsAdapter.fromJson(json)
        
        assertNotNull("DTO should not be null", dto)
        assertEquals("Should have 2 storms", 2, dto!!.activeStorms.size)
        
        val franklin = dto.activeStorms.find { it.name == "Franklin" }
        assertNotNull("Should find Franklin", franklin)
        assertEquals("Franklin should be a Hurricane", "Hurricane", franklin!!.stormType)
        assertEquals("Franklin should have advisory 15", "15", franklin.adv)
        
        val gert = dto.activeStorms.find { it.name == "Gert" }
        assertNotNull("Should find Gert", gert)
        assertEquals("Gert should be a Tropical Storm", "Tropical Storm", gert!!.stormType)
    }

    @Test fun parseCurrentStorms_handles_unknown_fields() {
        val jsonWithExtraFields = """
        {
          "activeStorms": [
            {
              "id": "AL012024",
              "name": "Franklin",
              "basin": "AL",
              "adv": "15",
              "stormType": "Hurricane",
              "productLink": "https://example.com",
              "unknownField": "should be ignored",
              "extraData": {"nested": "ignored"}
            }
          ],
          "unknownTopLevel": "ignored"
        }
        """.trimIndent()
        
        val dto = stormsAdapter.fromJson(jsonWithExtraFields)
        assertNotNull("Should parse despite unknown fields", dto)
        assertEquals("Should have 1 storm", 1, dto!!.activeStorms.size)
        assertEquals("Should extract name correctly", "Franklin", dto.activeStorms[0].name)
    }

    @Test fun parseCurrentStorms_handles_empty_json() {
        val emptyJson = """{"activeStorms": []}"""
        val dto = stormsAdapter.fromJson(emptyJson)
        
        assertNotNull("Should parse empty JSON", dto)
        assertTrue("Should have empty storms list", dto!!.activeStorms.isEmpty())
    }

    @Test fun parseCurrentStorms_handles_null_fields() {
        val jsonWithNulls = """
        {
          "activeStorms": [
            {
              "id": null,
              "name": null,
              "basin": "AL",
              "adv": null,
              "stormType": null,
              "productLink": null
            }
          ]
        }
        """.trimIndent()
        
        val dto = stormsAdapter.fromJson(jsonWithNulls)
        assertNotNull("Should parse despite null fields", dto)
        assertEquals("Should have 1 storm", 1, dto!!.activeStorms.size)
        assertNull("ID should be null", dto.activeStorms[0].id)
        assertNull("Name should be null", dto.activeStorms[0].name)
    }
}
