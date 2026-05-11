package org.nsh07.wikireader.parser

import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.runBlocking
import org.junit.Test

class ParseWikitableTest {

    @Test
    fun parseWikitable_withSingleRowspan_buildsCorrectGrid() =
        runBlocking {
            val table = """
                {| class="wikitable sortable plainrowheader"
                !scope="col" rowspan="2" | Header A
                | Cell A
                |-
                | Cell B
                |-
                |}
            """.trimIndent()

            val expectedTable = listOf(
                listOf("Header A", "Cell A"),
                listOf("", "Cell B")
            )

            val result = parseWikitable(
                table = table,
                colorScheme = darkColorScheme(),
                typography = Typography(),
                loadPage = { },
                showRef = { },
                fontSize = 18,
            )
            val resultTableText = result.second.map { row -> row.map { cell -> cell.text } }
            assertEquals(expectedTable, resultTableText)
        }

    @Test
    fun parseWikitable_withMultipleRowspans_buildsCorrectGrid() =
        runBlocking {
            val table =
                """
                {| class="wikitable sortable plainrowheaders"
                |+Video games
                !scope="col" rowspan="2" | Year
                !scope="col" rowspan="2" | Title
                !scope="col" colspan="4" | Platform(s)
                !scope="col" rowspan="2" | Ref(s)
                |-
                !scope="col"| PC
                !scope="col"| Console
                !scope="col"| Handheld
                !scope="col"| Mobile
                |-
                | 2020
                !scope="row"| ''Game 1''
                | -
                | Xbox
                | -
                | -
                | -
                |-
                | 2025
                !scope="row"| ''Game 2''
                | Windows
                | -
                | -
                | -
                | -
                |-
                |}
                """.trimIndent()
            val expectedCaption = "Video games"
            val expectedTable =
                listOf(
                    listOf("Year", "Title", "Platform(s)", "", "", "", "Ref(s)"),
                    listOf("", "", "PC", "Console", "Handheld", "Mobile", ""),
                    listOf("2020", "Game 1", "-", "Xbox", "-", "-", "-"),
                    listOf("2025", "Game 2", "Windows", "-", "-", "-", "-"),
                )

            val result =
                parseWikitable(
                    table = table,
                    colorScheme = darkColorScheme(),
                    typography = Typography(),
                    loadPage = { },
                    showRef = { },
                    fontSize = 18,
                )
            assertNotNull(result)

            val resultCaptionText = result.first.text
            val resultTableText = result.second.map { row -> row.map { cell -> cell.text } }
            assertEquals(expectedCaption, resultCaptionText)
            assertEquals(expectedTable, resultTableText)
        }
}
