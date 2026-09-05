package org.nsh07.wikireader.data

import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import org.nsh07.wikireader.parser.cleanUpWikitext
import org.nsh07.wikireader.parser.parseInfobox
import org.nsh07.wikireader.parser.toWikitextAnnotatedString
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
    fun cleanUpWikitext_noIncludeTag_removesTag() {
        assertEquals("Content", cleanUpWikitext("</noinclude>Content"))
    }

    @Test
    fun cleanUpWikitext_onlyIncludeWrappedTable_removesWrapper() {
        val table = "{| class=\"wikitable\"\n| Cell\n|}"

        assertEquals(table, cleanUpWikitext("<onlyInclude>$table</onlyInclude>"))
    }

    @Test
    fun sectionTransclusion_matchesPageAndSection() {
        val match = sectionTransclusion.find("{{#section:Marvel Cinematic Universe: Phase One|Films}}")

        assertEquals("Marvel Cinematic Universe: Phase One", match?.groupValues?.get(1))
        assertEquals("Films", match?.groupValues?.get(2))
    }

    @Test
    fun expandSectionTransclusions_insertsRequestedTable() = runBlocking {
        val article = "==== Phase One ====\n{{#section:Marvel Cinematic Universe: Phase One|Films}}"
        val table = "{| class=\"wikitable\"\n| Iron Man\n|}"
        val phasePage = "Before<section begin=Films />$table<section end=Films />After"

        val result = expandSectionTransclusions(article) { phasePage }

        assertEquals("==== Phase One ====\n$table", result)
    }

    @Test
    fun extractSection_returnsMarkedContent() {
        val table = "{| class=\"wikitable\"\n| Cell\n|}"
        val source = "Before<section begin=Films />$table<section end=Films />After"

        assertEquals(table, source.extractSection("Films"))
    }

    @Test
    fun cleanUpWikitext_sectionMarkers_removesBothMarkers() {
        val table = "{| class=\"wikitable\"\n| Cell\n|}"

        assertEquals(table, cleanUpWikitext("<section begin=Films />$table<section end=Films />"))
    }

    @Test
    fun parseInfobox_nestedPriceTemplates_renderCurrencies() = runBlocking {
        val infobox = """{{Infobox computing device
            | currentowner = [[Microsoft]]
            | price = {{Unbulleted list
             | '''Base''' / '''Digital Edition''' / '''Pro'''
             | {{USD|499|link=yes}} / {{USD|399|link=yes}} / {{USD|699|link=yes}}
             | {{Euro|499|link=yes}} / {{Euro|399|link=yes}} / {{Euro|799|link=yes}}
             | {{JPY|49,980|link=yes}} / {{JPY|39,980|link=yes}} / {{JPY|119,980|link=yes}}
             }}
            }}""".trimIndent()

        val rows = parseInfobox(
            infobox,
            lightColorScheme(),
            Typography(),
            {},
            {},
            16
        )
        val price = rows.first { it.first.text == "Price" }.second.text

        assertEquals("Current owner", rows.first().first.text)
        assertEquals(true, price.contains("$499 / $399 / $699"))
        assertEquals(true, price.contains("€499 / €399 / €799"))
        assertEquals(true, price.contains("¥49,980 / ¥39,980 / ¥119,980"))
        assertEquals(false, price.contains("yes}}"))
    }

    @Test
    fun toWikitextAnnotatedString_mainWithLabel_rendersSingleLabelledLink() {
        val result = "{{main|Reacher season 1|l1=''Reacher'' season 1}}".toWikitextAnnotatedString(
            colorScheme = lightColorScheme(),
            typography = Typography(),
            loadPage = {},
            fontSize = 16,
            showRef = {}
        )

        assertEquals("Main article: Reacher season 1\n", result.text)
    }

    @Test
    fun toWikitextAnnotatedString_startDate_rendersReadableDate() {
        val result = "{{Start date|2022|2|4}}".toWikitextAnnotatedString(
            colorScheme = lightColorScheme(),
            typography = Typography(),
            loadPage = {},
            fontSize = 16,
            showRef = {}
        )

        assertEquals("February 4, 2022", result.text)
    }

    @Test
    fun cleanUpWikitext_episodeTable_convertsToWikitable() {
        val input = """<onlyinclude>{{Episode table |background=#3B3D3E |overall=5 |episodes=
            {{Episode list/sublist|Reacher season 1
            | EpisodeNumber   = 1
            | EpisodeNumber2  = 1
            | Title           = Welcome to Margrave
            | DirectedBy      = [[Thomas Vincent (director)|Thomas Vincent]]
            | WrittenBy       = [[Nick Santora]]
            | OriginalAirDate = {{Start date|2022|2|4}}
            | ShortSummary    = At midnight, a man is shot dead.
            | LineColor       = 3B3D3E
            }}
            }}</onlyinclude>""".trimIndent()

        val result = cleanUpWikitext(input)

        assertEquals(true, result.startsWith("{| class=\"wikitable\""))
        assertEquals(true, result.contains("! No. !! Title !! Directed by !! Written by !! Original air date"))
        assertEquals(true, result.contains("| \"Welcome to Margrave\""))
        assertEquals(true, result.contains("| [[Thomas Vincent (director)|Thomas Vincent]]"))
        assertEquals(true, result.contains("| {{Start date|2022|2|4}}"))
        assertEquals(false, result.contains("ShortSummary"))
        assertEquals(false, result.contains("Episode table"))
    }

    @Test
    fun expandPageTransclusions_insertsOnlyIncludeContent() = runBlocking {
        val article = "=== Season 1 (2022) ===\n{{:Reacher season 1}}"
        val table = "{| class=\"wikitable\"\n| Episode\n|}"
        val seasonPage = "==Episodes==\n<onlyinclude>$table</onlyinclude>\n==Cast=="

        val result = expandPageTransclusions(article) { seasonPage }

        assertEquals("=== Season 1 (2022) ===\n$table", result)
    }

    @Test
    fun toWikitextAnnotatedString_numberedMainLink_hidesParameterName() {
        val result = "{{main|1=Example}}".toWikitextAnnotatedString(
            colorScheme = lightColorScheme(),
            typography = Typography(),
            loadPage = {},
            fontSize = 16,
            showRef = {}
        )

        assertEquals("Main article: Example\n", result.text)
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