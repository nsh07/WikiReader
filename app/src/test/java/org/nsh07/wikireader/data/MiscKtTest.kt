package org.nsh07.wikireader.data

import androidx.compose.ui.graphics.Color
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.pow

class MiscKtTest {
    @Test
    fun stringToColor_WhiteColorString_ReturnsWhite() {
        val colorStr = "Color(1.0, 1.0, 1.0, 1.0, sRGB IEC61966-2.1)"
        val color = colorStr.toColor()
        assertEquals(Color.Companion.White, color)
    }

    @Test
    fun stringToColor_RedColorString_ReturnsRed() {
        val colorStr = "Color(1.0, 0.0, 0.0, 1.0, sRGB IEC61966-2.1)"
        val color = colorStr.toColor()
        assertEquals(Color.Companion.Red, color)
    }

    @Test
    fun stringToColor_GreenColorString_ReturnsGreen() {
        val colorStr = "Color(0.0, 1.0, 0.0, 1.0, sRGB IEC61966-2.1)"
        val color = colorStr.toColor()
        assertEquals(Color.Companion.Green, color)
    }

    @Test
    fun stringToColor_BlueColorString_ReturnsBlue() {
        val colorStr = "Color(0.0, 0.0, 1.0, 1.0, sRGB IEC61966-2.1)"
        val color = colorStr.toColor()
        assertEquals(Color.Companion.Blue, color)
    }

    @Test
    fun stringToColor_BlackColorString_ReturnsBlack() {
        val colorStr = "Color(0.0, 0.0, 0.0, 1.0, sRGB IEC61966-2.1)"
        val color = colorStr.toColor()
        assertEquals(Color.Companion.Black, color)
    }

    @Test
    fun parseSections_returnsCorrectSections() {
        val inputStr = "Body text\n\n==Heading Without Space==\n\nContent 1\nHi\n\n" +
                "== Heading With Space ==\n\nHello\n\n=== Subheading ===\n\nHello"
        val sections = parseSections(inputStr)
        val expectedSections = listOf(
            "Body text\n",
            "Heading Without Space",
            "\nContent 1\nHi\n",
            " Heading With Space ",
            "\nHello\n\n=== Subheading ===\n\nHello"
        )
        assertEquals(expectedSections, sections)
    }

    @Test
    fun bytesToHumanReadableSize_bytes_returnsBytes() {
        val bytes = 900.0
        val humanReadableSize = bytesToHumanReadableSize(bytes)
        assertEquals("900.0 bytes", humanReadableSize)
    }

    @Test
    fun bytesToHumanReadableSize_kilobytes_returnsKb() {
        val bytesMin = 2.0.pow(10.0)
        val bytesMax = 2.0.pow(20.0) - 1
        val bytesOverMax = 2.0.pow(20.0)

        val humanReadableSizeMin = bytesToHumanReadableSize(bytesMin)
        val humanReadableSizeMax = bytesToHumanReadableSize(bytesMax)
        val humanReadableSizeOverMax = bytesToHumanReadableSize(bytesOverMax)

        assertEquals("1 kB", humanReadableSizeMin)
        assertEquals("1024 kB", humanReadableSizeMax)
        Assert.assertNotEquals("1024 kB", humanReadableSizeOverMax)
    }

    @Test
    fun bytesToHumanReadableSize_megabytes_returnsMb() {
        val bytesMin = 2.0.pow(20.0)
        val bytesMax = 2.0.pow(30.0) - 1
        val bytesOverMax = 2.0.pow(30.0)

        val humanReadableSizeMin = bytesToHumanReadableSize(bytesMin)
        val humanReadableSizeMax = bytesToHumanReadableSize(bytesMax)
        val humanReadableSizeOverMax = bytesToHumanReadableSize(bytesOverMax)

        assertEquals("1.0 MB", humanReadableSizeMin)
        assertEquals("1024.0 MB", humanReadableSizeMax)
        Assert.assertNotEquals("1024.0 MB", humanReadableSizeOverMax) // Should not be equal
    }

    @Test
    fun bytesToHumanReadableSize_gigabytes_returnsGb() {
        val bytes = 2.0.pow(30.0)
        val humanReadableSize = bytesToHumanReadableSize(bytes)
        assertEquals("1.0 GB", humanReadableSize)
    }

    @Test
    fun langCodeToName_allLanguages_returnsCorrectNames() {
        val langSize = LanguageData.langCodes.size
        for (i in 0 until langSize) {
            val langCode = LanguageData.langCodes[i]
            val langName = langCodeToName(langCode)
            assertEquals(LanguageData.langNames[i], langName)
        }
        val langCode = "gibberish" // An unknown language code
        val langName = langCodeToName(langCode)
        assertEquals(langCode, langName)
    }

    @Test
    fun langCodeToWikiName_allLanguages_returnsCorrectNames() {
        val langSize = LanguageData.langCodes.size
        for (i in 0 until langSize) {
            val langCode = LanguageData.langCodes[i]
            val wikiName = langCodeToWikiName(langCode)
            assertEquals(LanguageData.wikipediaNames[i], wikiName)
        }
        val langCode = "gibberish" // An unknown language code
        val wikiName = langCodeToName(langCode)
        assertEquals(langCode, wikiName)
    }

    @Test
    fun countryFlag_countries_returnsCorrectFlags() {
        assertEquals(countryFlag("in"), "🇮🇳")
        assertEquals(countryFlag("us"), "🇺🇸")
        assertEquals(countryFlag("gb"), "🇬🇧")
        assertEquals(countryFlag("de"), "🇩🇪")
    }

    @Test
    fun countryFlag_blankInput_returnsEmptyString() {
        assertEquals(countryFlag(""), "")
    }
}