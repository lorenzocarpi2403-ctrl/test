package com.example

import com.example.ai.ReceiptAiService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReceiptDateParserTest {

    private val aiService = ReceiptAiService()

    @Test
    fun parseIsoDate_validFormats_detectedCorrectly() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ITALIAN)

        // ISO format YYYY-MM-DD
        val (time1, detected1) = aiService.parseIsoDateOrCurrent("2026-11-02")
        assertTrue(detected1)
        assertEquals("2026-11-02", sdf.format(Date(time1)))

        // Italian format DD/MM/YYYY
        val (time2, detected2) = aiService.parseIsoDateOrCurrent("02/11/2026")
        assertTrue(detected2)
        assertEquals("2026-11-02", sdf.format(Date(time2)))

        // Italian dash format DD-MM-YYYY
        val (time3, detected3) = aiService.parseIsoDateOrCurrent("02-11-2026")
        assertTrue(detected3)
        assertEquals("2026-11-02", sdf.format(Date(time3)))

        // Italian dot format DD.MM.YYYY
        val (time4, detected4) = aiService.parseIsoDateOrCurrent("02.11.2026")
        assertTrue(detected4)
        assertEquals("2026-11-02", sdf.format(Date(time4)))
    }

    @Test
    fun parseIsoDate_blankOrInvalid_defaultsToCurrentTimeAndFalse() {
        val before = System.currentTimeMillis() - 100
        val (timeBlank, detectedBlank) = aiService.parseIsoDateOrCurrent("")
        val after = System.currentTimeMillis() + 100

        assertFalse(detectedBlank)
        assertTrue(timeBlank in before..after)

        val (timeInvalid, detectedInvalid) = aiService.parseIsoDateOrCurrent("non-una-data")
        assertFalse(detectedInvalid)
        assertTrue(timeInvalid in before..after)
    }
}
